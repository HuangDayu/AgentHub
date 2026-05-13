package com.agenthub.infrastructure.tools.system_tools.base_tools;

import com.agenthub.infrastructure.tools.system_tools.annotations.AgentTools;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;

/**
 * @author huangdayu
 */
@RequiredArgsConstructor
@AgentTools(name = "UITools", description = "UI工具，提供浏览器和画布功能")
public class UITools {

    @Tool(description = "在浏览器中打开指定URL")
    public void browser(@ToolParam String url) throws IOException {
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().browse(URI.create(url));
        }
    }

    @Tool(description = "创建或操作画布")
    public String canvas(@ToolParam String operation, @ToolParam String content) {
        return "画布操作完成: " + operation + ", 内容: " + content;
    }
}
