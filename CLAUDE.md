# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.


## Behavioral Guidelines

Behavioral guidelines to reduce common LLM coding mistakes. Merge with project-specific instructions as needed.

**Tradeoff:** These guidelines bias toward caution over speed. For trivial tasks, use judgment.

### 1. Think Before Coding

**Don't assume. Don't hide confusion. Surface tradeoffs.**

Before implementing:
- State your assumptions explicitly. If uncertain, ask.
- If multiple interpretations exist, present them - don't pick silently.
- If a simpler approach exists, say so. Push back when warranted.
- If something is unclear, stop. Name what's confusing. Ask.

### 2. Simplicity First

**Minimum code that solves the problem. Nothing speculative.**

- No features beyond what was asked.
- No abstractions for single-use code.
- No "flexibility" or "configurability" that wasn't requested.
- No error handling for impossible scenarios.
- If you write 200 lines and it could be 50, rewrite it.

Ask yourself: "Would a senior engineer say this is overcomplicated?" If yes, simplify.

### 3. Surgical Changes

**Touch only what you must. Clean up only your own mess.**

When editing existing code:
- Don't "improve" adjacent code, comments, or formatting.
- Don't refactor things that aren't broken.
- Match existing style, even if you'd do it differently.
- If you notice unrelated dead code, mention it - don't delete it.

When your changes create orphans:
- Remove imports/variables/functions that YOUR changes made unused.
- Don't remove pre-existing dead code unless asked.

The test: Every changed line should trace directly to the user's request.

### 4. Goal-Driven Execution

**Define success criteria. Loop until verified.**

Transform tasks into verifiable goals:
- "Add validation" → "Write tests for invalid inputs, then make them pass"
- "Fix the bug" → "Write a test that reproduces it, then make it pass"
- "Refactor X" → "Ensure tests pass before and after"

For multi-step tasks, state a brief plan:
```
1. [Step] → verify: [check]
2. [Step] → verify: [check]
3. [Step] → verify: [check]
```

Strong success criteria let you loop independently. Weak criteria ("make it work") require constant clarification.

---

**These guidelines are working if:** fewer unnecessary changes in diffs, fewer rewrites due to overcomplication, and clarifying questions come before implementation rather than after mistakes.


## Project Overview

AgentHub is an enterprise AI Agent lifecycle management platform built with **Clean Architecture** (4-layer) and **TDD**. The backend is Java 21 + Spring Boot 4.1.0-M2 + Spring AI 2.0.0-M4, frontend is Vue 3.5 + TypeScript + Vite.

## Build & Run

```bash
# Backend - build entire project
gradle clean build

# Backend - run all tests
gradle test

# Backend - run specific test class
gradle test --tests "*WorkflowEngineTest*"

# Backend - run architecture tests (ArchUnit)
gradle test --tests "*ArchTest*"

# Backend - run a single test method
gradle test --tests "*WorkflowEngineTest#shouldExecuteInTopologicalOrder*"

# Backend - run application
gradle bootRun

# Frontend - dev server (port 5173)
cd src/main/web && npm run dev

# Frontend - build
cd src/main/web && npm run build

# Frontend - run tests
cd src/main/web && npm run test
```

## Architecture

### 4-Layer Clean Architecture (enforced by ArchUnit)

```
api (Controller/DTO) → application (UseCase/Port) → domain (Model)
                                                          ↑
infrastructure (agents/auth/store/etl/rag/tools/vector) ──┘
```

- **api**: REST controllers, request/response DTOs, exception handlers. Controllers are thin — delegate to UseCases.
- **application**: UseCases orchestrate business logic, define `port` interfaces (in/out). No framework dependencies in business logic.
- **domain**: Pure Java models, enums, events, exceptions. Zero Spring/infra annotations. Domain objects have behavior (e.g. `Workflow.publish()`).
- **infrastructure**: Implements `application/port/out` interfaces. Contains MyBatis repositories, AI agents, vector stores, ETL pipeline, workflow engine, auth (JWT).

### Key Patterns

- **Hexagonal / Port-Adapter**: `application/port/out` defines interfaces (e.g. `WorkflowRepository`), `infrastructure/store/db` implements them (e.g. `MybatisWorkflowRepository`).
- **UseCase pattern**: One class per use case, injected with port interfaces, returns application-layer DTOs (`*Output`). Controllers convert between DTOs and API responses via `BeanUtil.copyProperties`.
- **Workflow Engine**: DAG-based, uses jgrapht `DirectedAcyclicGraph`, topology sort execution, `NodeProcessor` strategy pattern (12 processor implementations), Reactor/WebFlux for streaming (`Flux<NodeResult>`), Redis-backed state management via `WorkflowStateManager`.
- **Agent Framework**: Built on Spring AI Alibaba Agent Framework + AgentScope, with `AbstractReActAgent` base class and `AliReActAgent` implementation.

### Database

PostgreSQL 14+ with schema in `sql/schema.sql`, seed data in `sql/init.sql`. MyBatis-Plus for ORM with entities in `infrastructure/store/db/entity/` and mappers in `infrastructure/store/db/mapper/`.

### Naming Conventions

| Type | Pattern | Location |
|------|---------|----------|
| Controller | `*Controller` | `api/controller/` |
| UseCase | `*UseCase` | `application/usecase/` |
| Repository interface | `*Repository` | `application/port/out/repositories/` |
| Repository impl | `Mybatis*Repository` | `infrastructure/store/db/repository/` |
| MyBatis Entity | `*Entity` | `infrastructure/store/db/entity/` |
| MyBatis Mapper | `*Mapper` | `infrastructure/store/db/mapper/` |
| Request DTO | `*Request` | `api/dto/` |
| Response DTO | `*Response` | `api/dto/` |
| Application DTO | `*Output` | `application/dto/` |
| Command object | `*Command` | `application/command/` |
| Port interface | `*Port` or `*Repository` | `application/port/` |
| Domain model | domain noun | `domain/model/` |

### Layer Dependency Rules (ArchUnit enforced)

- `domain` must NOT depend on any framework (Spring, JPA, Kafka, etc.)
- `application` calls `domain` only
- `api` depends on `application`
- `infrastructure` implements `application/port/*` and `domain` model

### Key Infrastructure Modules

- **agents/**: AI agent implementations (AliReActAgent, agent factories)
- **auth/**: JWT token provider, refresh token generator, authorization adapter
- **etl/**: Document ETL pipeline (parse → clean → chunk → vectorize)
- **rag/**: RAG pipeline (query rewrite → dual retrieval → re-rank)
- **store/**: DB repositories, cache (Redis), file storage (MinIO)
- **tools/**: HTTP tools, MCP tools, system tools, skills
- **vector/**: Multiple vector store factories (Qdrant, Milvus, Weaviate, Chroma, PGVector)
- **workflow/**: DAG workflow engine (topological execution, state persistence, node processors)

### API Conventions

- Base path: `/api/v1`
- Multi-tenant via `X-Tenant-Id` header
- Workspace-scoped: `/api/v1/workspaces/{workspaceId}/...`
- Response status: POST returns 201, DELETE returns 204
- `@RequiredArgsConstructor` + `private final` for DI (no `@Autowired`)
- `BeanUtil.copyProperties` for DTO ↔ domain conversions

## Development Conventions

- **TDD**: Write tests first. Controller integration tests must cover 100%.
- **Code style**: Methods ≤ 10 lines, ≤ 3 params, Javadoc on every method, use Lombok (`@Data`, `@RequiredArgsConstructor`), **never** use Java `record` classes.
- **ArchUnit**: All architecture rules are tested — any cross-layer violation fails the build.
- **Reactive**: Workflow engine uses Reactor (Flux/Mono). Most other code is imperative.
- **DB changes**: Add SQL migration scripts in `sql/` directory.
