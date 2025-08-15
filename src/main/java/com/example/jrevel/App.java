package com.example.jrevel;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Minimal example "JRevel" project.
 * Starts an HTTP server and serves an index page with an input field.
 */
public class App {
    private static HttpServer server;

    public static void main(String[] args) throws IOException {
        int port = getPortFromEnvOrDefault();
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
        } catch (java.net.BindException be) {
            System.err.println("Port " + port + " is in use, falling back to a random available port.");
            server = HttpServer.create(new InetSocketAddress(0), 0);
        }

        // Root path serving index page with input
        server.createContext("/", new IndexHandler());

        // Simple echo handler to demonstrate form submission
        server.createContext("/echo", new EchoHandler());

        server.setExecutor(null); // default executor
        server.start();
        int actualPort = server.getAddress().getPort();
        System.out.println("JRevel example server started on http://localhost:" + actualPort);

        // Optional: graceful shutdown on JVM exit
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                server.stop(0);
            } catch (Exception ignored) { }
        }));
    }

    private static int getPortFromEnvOrDefault() {
        String env = System.getenv("PORT");
        if (env != null) {
            try {
                return Integer.parseInt(env);
            } catch (NumberFormatException ignored) { }
        }
        return 8080;
    }

    static class IndexHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            if (!"GET".equalsIgnoreCase(method)) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            String html = "" +
                    "<!doctype html>" +
                    "<html lang=\"en\">" +
                    "<head>" +
                    "  <meta  charset=\"utf-8\">" +
                    "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">" +
                    "  <title>JRevel Example</title>" +
                    "  <style>body{font-family:Arial,Helvetica,sans-serif;margin:2rem;}input,button{font-size:1rem;padding:.5rem;margin:.25rem 0;}</style>" +
                    "</head>" +
                    "<body>" +
                    "  <h1>Welcome to JRevel</h1>" +
                    "  <p>This is a simple example project. The index page contains an input field.</p>" +
                    "  <form method=\"GET\" action=\"/echo\">" +
                    "    <label for=\"name\">Your name:</label><br>" +
                    "    <input id=\"name\" name=\"name\" type=\"text\" placeholder=\"Enter your name\" required>" +
                    "    <button type=\"submit\">Say hi</button>" +
                    "  </form>" +
                    "</body>" +
                    "</html>";
            byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }

    static class EchoHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            URI uri = exchange.getRequestURI();
            Map<String, String> params = queryToMap(uri.getRawQuery());
            String name = params.getOrDefault("name", "Anonymous");
            String html = "<html><body><h2>Hello, " + escapeHtml(name) + "!</h2><p><a href=\"/\">Back</a></p></body></html>";
            byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }

        private static Map<String, String> queryToMap(String query) {
            Map<String, String> map = new LinkedHashMap<>();
            if (query == null || query.isEmpty()) return map;
            for (String pair : query.split("&")) {
                int idx = pair.indexOf('=');
                String key = idx > 0 ? decode(pair.substring(0, idx)) : decode(pair);
                String value = idx > 0 && pair.length() > idx + 1 ? decode(pair.substring(idx + 1)) : "";
                map.put(key, value);
            }
            return map;
        }

        private static String decode(String s) {
            return URLDecoder.decode(s, StandardCharsets.UTF_8);
        }

        private static String escapeHtml(String s) {
            return s.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#x27;");
        }
    }
}
