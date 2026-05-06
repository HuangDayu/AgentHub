package com.agenthub.infrastructure.tools.function_tools.base_tools;

import com.agenthub.infrastructure.tools.function_tools.annotations.AgentTools;
import org.springframework.ai.tool.annotation.Tool;

import java.net.*;
import java.util.Enumeration;

@AgentTools(name = "NetworkTools", description = "网络工具，提供IP地址获取、主机名解析、端口检测、URL编解码、网络接口信息等网络相关功能", defaultEnable = false)
public class NetworkTools {

    @Tool(name = "network_local_ip", description = "Get local IP address")
    public String getLocalIp() throws Exception {
        return InetAddress.getLocalHost().getHostAddress();
    }

    @Tool(name = "network_local_hostname", description = "Get local hostname")
    public String getLocalHostname() throws Exception {
        return InetAddress.getLocalHost().getHostName();
    }

    @Tool(name = "network_resolve_host", description = "Resolve hostname to IP")
    public String resolveHost(String hostname) throws Exception {
        return InetAddress.getByName(hostname).getHostAddress();
    }

    @Tool(name = "network_reverse_dns", description = "Reverse DNS lookup for IP")
    public String reverseDns(String ip) throws Exception {
        return InetAddress.getByName(ip).getHostName();
    }

    @Tool(name = "network_is_reachable", description = "Check if host is reachable")
    public boolean isReachable(String host, int timeout) throws Exception {
        return InetAddress.getByName(host).isReachable(timeout);
    }

    @Tool(name = "network_ping", description = "Ping host with timeout")
    public String ping(String host, int timeoutMs) throws Exception {
        boolean reachable = InetAddress.getByName(host).isReachable(timeoutMs);
        return reachable ? "Host is reachable" : "Host is not reachable";
    }

    @Tool(name = "network_all_ips", description = "Get all local IP addresses")
    public String getAllIps() throws Exception {
        StringBuilder sb = new StringBuilder();
        Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
        while (nets.hasMoreElements()) {
            NetworkInterface ni = nets.nextElement();
            Enumeration<InetAddress> addrs = ni.getInetAddresses();
            while (addrs.hasMoreElements()) {
                sb.append(addrs.nextElement().getHostAddress()).append("\n");
            }
        }
        return sb.toString();
    }

    @Tool(name = "network_interface_names", description = "Get network interface names")
    public String getInterfaceNames() throws Exception {
        StringBuilder sb = new StringBuilder();
        Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
        while (nets.hasMoreElements()) {
            sb.append(nets.nextElement().getName()).append("\n");
        }
        return sb.toString();
    }

    @Tool(name = "network_mac_address", description = "Get MAC address of interface")
    public String getMacAddress(String interfaceName) throws Exception {
        NetworkInterface ni = NetworkInterface.getByName(interfaceName);
        byte[] mac = ni.getHardwareAddress();
        StringBuilder sb = new StringBuilder();
        for (byte b : mac) sb.append(String.format("%02X:", b));
        return sb.substring(0, sb.length() - 1);
    }

    @Tool(name = "network_url_encode", description = "URL encode a string")
    public String urlEncode(String text) throws Exception {
        return URLEncoder.encode(text, "UTF-8");
    }

    @Tool(name = "network_url_decode", description = "URL decode a string")
    public String urlDecode(String text) throws Exception {
        return URLDecoder.decode(text, "UTF-8");
    }

    @Tool(name = "network_parse_url", description = "Parse URL into components")
    public String parseUrl(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        return String.format("Protocol: %s\nHost: %s\nPort: %d\nPath: %s",
                url.getProtocol(), url.getHost(), url.getPort(), url.getPath());
    }

    @Tool(name = "network_get_content", description = "Get content from URL")
    public String getUrlContent(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        var conn = url.openConnection();
        return new String(conn.getInputStream().readAllBytes());
    }

    @Tool(name = "network_http_port_check", description = "Check if port is open on host")
    public boolean checkPort(String host, int port, int timeout) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), timeout);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
