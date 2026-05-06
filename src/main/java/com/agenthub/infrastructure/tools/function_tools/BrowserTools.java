package com.agenthub.infrastructure.tools.function_tools;

import com.agenthub.infrastructure.tools.function_tools.annotations.AgentTools;
import org.springframework.ai.tool.annotation.Tool;

import java.util.ArrayList;
import java.util.List;

@AgentTools(name = "BrowserTools", description = "浏览器操作工具，提供网页导航、点击、输入、截图、Cookie管理等浏览器自动化功能")
public class BrowserTools {

    private String currentUrl = "";
    private String currentPage = "";
    private final List<String> history = new ArrayList<>();

    @Tool(name = "browser_navigate", description = "Navigate to URL")
    public String browserNavigate(String url) {
        history.add(url);
        currentUrl = url;
        return "Navigated to: " + url;
    }

    @Tool(name = "browser_back", description = "Go back in browser history")
    public String browserBack() {
        if (history.size() > 1) {
            history.remove(history.size() - 1);
            currentUrl = history.get(history.size() - 1);
            return "Back to: " + currentUrl;
        }
        return "No history to go back to";
    }

    @Tool(name = "browser_forward", description = "Go forward in browser history")
    public String browserForward() {
        return "Forward navigation not implemented";
    }

    @Tool(name = "browser_refresh", description = "Refresh current page")
    public String browserRefresh() {
        return "Refreshed: " + currentUrl;
    }

    @Tool(name = "browser_click", description = "Click element by selector")
    public String browserClick(String selector) {
        return "Clicked element: " + selector;
    }

    @Tool(name = "browser_type", description = "Type text into element")
    public String browserType(String selector, String text) {
        return "Typed '" + text + "' into: " + selector;
    }

    @Tool(name = "browser_select", description = "Select option in dropdown")
    public String browserSelect(String selector, String value) {
        return "Selected '" + value + "' in: " + selector;
    }

    @Tool(name = "browser_check", description = "Check checkbox")
    public String browserCheck(String selector) {
        return "Checked: " + selector;
    }

    @Tool(name = "browser_uncheck", description = "Uncheck checkbox")
    public String browserUncheck(String selector) {
        return "Unchecked: " + selector;
    }

    @Tool(name = "browser_screenshot", description = "Take screenshot")
    public String browserScreenshot(String outputPath) {
        return "Screenshot saved to: " + outputPath;
    }

    @Tool(name = "browser_get_text", description = "Get text from element")
    public String browserGetText(String selector) {
        return "Text from " + selector + ": [content]";
    }

    @Tool(name = "browser_get_attribute", description = "Get attribute from element")
    public String browserGetAttribute(String selector, String attribute) {
        return "Attribute '" + attribute + "' from " + selector + ": [value]";
    }

    @Tool(name = "browser_wait_for", description = "Wait for element to appear")
    public String browserWaitFor(String selector, int timeoutMs) {
        return "Waited for: " + selector;
    }

    @Tool(name = "browser_execute_js", description = "Execute JavaScript")
    public String browserExecuteJs(String script) {
        return "Executed: " + script;
    }

    @Tool(name = "browser_get_cookies", description = "Get all cookies")
    public String browserGetCookies() {
        return "Cookies: []";
    }

    @Tool(name = "browser_set_cookie", description = "Set cookie")
    public String browserSetCookie(String name, String value) {
        return "Set cookie: " + name + "=" + value;
    }

    @Tool(name = "browser_clear_cookies", description = "Clear all cookies")
    public String browserClearCookies() {
        return "Cookies cleared";
    }

    @Tool(name = "browser_get_url", description = "Get current URL")
    public String browserGetUrl() {
        return currentUrl;
    }

    @Tool(name = "browser_get_title", description = "Get page title")
    public String browserGetTitle() {
        return "Page title: [title]";
    }

    @Tool(name = "browser_close", description = "Close browser")
    public String browserClose() {
        currentUrl = "";
        currentPage = "";
        history.clear();
        return "Browser closed";
    }
}
