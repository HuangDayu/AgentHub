#!/usr/bin/env node
/**
 * Scans .ts and .vue files in src/ for methods/functions exceeding 10 lines
 * (excluding comments and blank lines), ignoring line continuations.
 *
 * Detects:
 *  - function declarations: function name(...) { ... }
 *  - arrow functions: const name = (...) => { ... }
 *  - class methods: name(...) { ... }
 *  - object methods: name(...) { ... }
 *  - setup() and other Vue composition functions
 *
 * Output: list of file:method with line counts, sorted by line count desc.
 */

import { readFileSync, readdirSync, statSync } from 'fs';
import { join } from 'path';

const ROOT = 'src';
const MAX_LINES = 10;

/**
 * Strip line comments, block comments, strings, template literals (single-line only).
 */
function stripLiterals(line) {
  let i = 0;
  let inStr = null;
  while (i < line.length) {
    const c = line[i];
    if (inStr) {
      if (c === '\\') { i += 2; continue; }
      if (c === inStr) inStr = null;
      i++;
      continue;
    }
    if (c === '"' || c === "'") { inStr = c; i++; continue; }
    if (c === '`') { inStr = '`'; i++; continue; }
    if (c === '/' && line[i + 1] === '/') {
      return line.slice(0, i).trim();
    }
    if (c === '/' && line[i + 1] === '*') {
      const end = line.indexOf('*/', i + 2);
      if (end >= 0) {
        return line.slice(0, i) + line.slice(end + 2);
      }
      return line.slice(0, i);
    }
    i++;
  }
  return line;
}

/**
 * Extract the <script> block content from a .vue file.
 */
function extractVueScript(content) {
  const match = content.match(/<script\b[^>]*>([\s\S]*?)<\/script>/);
  return match ? match[1] : content;
}

/**
 * Parse the file to find all top-level and nested function definitions.
 * Returns array of { name, startLine, endLine, code }.
 */
function findFunctions(lines, fileName) {
  const functions = [];
  const stack = [];
  // patterns to match
  const funcDeclRe = /^\s*(?:export\s+)?(?:async\s+)?function\s*\*?\s*([A-Za-z_$][\w$]*)\s*\(/;
  const arrowAssignRe = /^\s*(?:export\s+)?(?:const|let|var)\s+([A-Za-z_$][\w$]*)\s*(?::\s*[^=]+)?\s*=\s*(?:async\s+)?\(/;
  const arrowAssignParenRe = /^\s*(?:export\s+)?(?:const|let|var)\s+([A-Za-z_$][\w$]*)\s*(?::\s*[^=]+)?\s*=\s*(?:async\s+)?\(/;
  const methodRe = /^\s*(?:public\s+|private\s+|protected\s+|static\s+|async\s+|readonly\s+)*([A-Za-z_$][\w$]*)\s*(<[^>]*>)?\s*\(/;
  const singleLineMethodRe = /^\s*(?:public\s+|private\s+|protected\s+|static\s+|async\s+|readonly\s+)*([A-Za-z_$][\w$]*)\s*(?:\([^)]*\))?\s*(?::\s*[^{]+)?\s*\{([^{}]*)\}\s*;?\s*$/;
  const getSetRe = /^\s*(?:get|set)\s+([A-Za-z_$][\w$]*)\s*\(/;
  const constructorRe = /^\s*constructor\s*\(/;

  // We do a brace-depth-based scan
  let depth = 0;
  let pendingName = null;
  let pendingStartLine = -1;
  let pendingHasOpenBrace = false;

  for (let i = 0; i < lines.length; i++) {
    const raw = lines[i];
    const line = stripLiterals(raw).trim();
    if (!line) continue;

    // Try to detect function/method declarations on this line
    let m;
    if (depth === 0 || pendingName === null) {
      if ((m = line.match(funcDeclRe))) {
        pendingName = m[1];
        pendingStartLine = i + 1;
        pendingHasOpenBrace = line.includes('{');
        if (pendingHasOpenBrace) {
          // open on same line; close when depth returns
        }
      } else if ((m = line.match(arrowAssignRe))) {
        pendingName = m[1];
        pendingStartLine = i + 1;
        // arrow may have (params) => { body } or single expression
        if (line.includes('=>')) {
          if (line.endsWith(';')) {
            // single line, skip
            pendingName = null;
            continue;
          }
          if (line.includes('{')) {
            pendingHasOpenBrace = true;
          } else {
            // arrow without braces, single line
            pendingName = null;
            continue;
          }
        }
      } else if ((m = line.match(getSetRe))) {
        pendingName = m[1];
        pendingStartLine = i + 1;
        if (line.includes('{')) pendingHasOpenBrace = true;
      } else if ((m = line.match(constructorRe))) {
        pendingName = 'constructor';
        pendingStartLine = i + 1;
        if (line.includes('{')) pendingHasOpenBrace = true;
      } else if ((m = line.match(methodRe)) && !line.startsWith('//') && !line.startsWith('*') && /[\w$]\s*\(/.test(line)) {
        // Be careful: don't match `if (`, `for (`, `while (`, `switch (`, `return (`
        // Check for control flow
        const controlRe = /^\s*(if|for|while|switch|return|else|catch|do|throw|new|typeof|in|of)\b/;
        if (!controlRe.test(line) && /[\w$]\s*\(/.test(line)) {
          // exclude type annotations like `name: string`
          if (!line.match(/^\s*[A-Za-z_$][\w$]*\s*:\s*/)) {
            pendingName = m[1];
            pendingStartLine = i + 1;
            if (line.includes('{')) pendingHasOpenBrace = true;
          }
        }
      } else if (line.match(/^\s*(?:async\s+)?[A-Za-z_$][\w$]*\s*\([^)]*\)\s*(?::\s*[^{]+)?\s*\{/)) {
        // generic method: name(params): ReturnType {
        const mm = line.match(/^\s*(?:async\s+)?([A-Za-z_$][\w$]*)\s*\(/);
        if (mm && !line.match(/^\s*(if|for|while|switch|return|else|catch|do|throw|new|typeof|in|of)\b/)) {
          pendingName = mm[1];
          pendingStartLine = i + 1;
          pendingHasOpenBrace = true;
        }
      }
    }

    // Track brace depth relative to the function
    if (pendingName) {
      // Count braces in this line, but only outside strings (we already stripped)
      let lineDepth = 0;
      for (const c of line) {
        if (c === '{') lineDepth++;
        else if (c === '}') lineDepth--;
      }

      if (pendingHasOpenBrace || lineDepth > 0) {
        depth += lineDepth;
        if (depth <= 0) {
          // Function ends
          functions.push({
            name: pendingName,
            startLine: pendingStartLine,
            endLine: i + 1,
            code: lines.slice(pendingStartLine - 1, i + 1).join('\n'),
          });
          pendingName = null;
          pendingStartLine = -1;
          pendingHasOpenBrace = false;
          depth = 0;
        }
      }
    }
  }

  return functions;
}

/**
 * Count non-blank, non-comment lines in the code.
 */
function countLines(code) {
  const lines = code.split('\n');
  let count = 0;
  for (const raw of lines) {
    const stripped = stripLiterals(raw);
    if (stripped.trim()) count++;
  }
  return count;
}

function walk(dir) {
  const out = [];
  for (const entry of readdirSync(dir)) {
    if (entry === 'node_modules') continue;
    const full = join(dir, entry);
    const st = statSync(full);
    if (st.isDirectory()) {
      out.push(...walk(full));
    } else if (/\.(ts|vue)$/.test(entry) && !entry.endsWith('.d.ts')) {
      out.push(full);
    }
  }
  return out;
}

const files = walk(ROOT);
const violations = [];

for (const file of files) {
  const content = readFileSync(file, 'utf8');
  const scriptContent = file.endsWith('.vue') ? extractVueScript(content) : content;
  const lines = scriptContent.split('\n');
  const functions = findFunctions(lines, file);

  for (const fn of functions) {
    const count = countLines(fn.code);
    if (count > MAX_LINES) {
      violations.push({
        file: file.replace(/\\/g, '/'),
        name: fn.name,
        startLine: fn.startLine,
        endLine: fn.endLine,
        count,
      });
    }
  }
}

violations.sort((a, b) => b.count - a.count);

console.log(`\n=== Frontend Methods > ${MAX_LINES} lines (${violations.length} violations) ===\n`);
for (const v of violations) {
  console.log(`${v.file}:${v.startLine} ${v.name}() - ${v.count} lines`);
}
