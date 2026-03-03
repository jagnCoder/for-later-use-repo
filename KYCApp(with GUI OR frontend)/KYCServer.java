import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;


public class KYCServer {

	    // Add these imports at the top of KYCServer.java (alongside your existing imports)
	// Rewritten main method (replace your existing main with this)
	public static void main(String[] args) throws IOException {
	    HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
	
	    // Context for adding and viewing users
	    server.createContext("/api/users", new UsersHandler());

	    // Context for updating and deleting users
	    server.createContext("/api/users/", new SpecificUserHandler());

	    // Serve static files (KYCS.html and other assets) from current working directory
	    server.createContext("/", exchange -> {
	        try {
	            String path = exchange.getRequestURI().getPath();
	            if (path == null || path.equals("/")) path = "/KYCS.html";

	            File file = new File("." + path); // serve from current directory
	            if (!file.exists() || file.isDirectory()) {
	                String resp = "404 Not Found";
	                exchange.sendResponseHeaders(404, resp.getBytes(StandardCharsets.UTF_8).length);
	                try (OutputStream os = exchange.getResponseBody()) {
	                    os.write(resp.getBytes(StandardCharsets.UTF_8));
	                }
	                return;
	            }
	
	            String contentType = Files.probeContentType(file.toPath());
	            if (contentType == null) contentType = "application/octet-stream";
	            byte[] bytes = Files.readAllBytes(file.toPath());
	            exchange.getResponseHeaders().set("Content-Type", contentType + "; charset=UTF-8");
	            exchange.sendResponseHeaders(200, bytes.length);
	            try (OutputStream os = exchange.getResponseBody()) {
	                os.write(bytes);
	            }
	        } catch (IOException ioe) {
	            String resp = "500 Internal Server Error";
	            try {
	                exchange.sendResponseHeaders(500, resp.getBytes(StandardCharsets.UTF_8).length);
	                try (OutputStream os = exchange.getResponseBody()) {
	                    os.write(resp.getBytes(StandardCharsets.UTF_8));
	                }
	            } catch (IOException ignored) { }
	        } finally {
	            try { exchange.close(); } catch (Exception ignored) { }
	        }
	    });

	    server.setExecutor(null); // Use default executor
	    server.start();
	    System.out.println("KYC API Server started on port 8000, look at=> http://localhost:8000");
	    System.out.println("Make sure your MySQL database is running and accessible.");
	}
    
    // --- CORS Headers Utility ---
    private static void setCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
    }
    
    // --- Utility to parse URL-encoded data ---
    private static Map<String, String> parseFormData(String formData) {
        Map<String, String> data = new HashMap<>();
        String[] pairs = formData.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            if (idx > 0) {
                try {
                    String key = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8.name());
                    String value = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8.name());
                    data.put(key, value);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return data;
    }

    // Handles requests to /api/users
    static class UsersHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                handlePostRequest(exchange);
            } else if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                handleGetRequest(exchange);
            } else {
                sendResponse(exchange, "Method Not Allowed", 405);
            }
        }

        private void handlePostRequest(HttpExchange exchange) throws IOException {
            Connection conn = KYCApp.getConnection();
            if (conn == null) {
                sendResponse(exchange, "Database connection failed", 500);
                return;
            }

            try {
                InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), "utf-8");
                BufferedReader br = new BufferedReader(isr);
                String formData = br.lines().collect(java.util.stream.Collectors.joining());
                Map<String, String> data = parseFormData(formData);

                String fullName = data.get("fullName");
                String dob = data.get("dob");
                String address = data.get("address");
                String docType = data.get("docType1");
                String docNumber = data.get("docNumber1");

                int userId = KYCApp.createUser(conn, fullName, dob, address);
                if (userId != -1) {
                    KYCApp.uploadDocument(conn, userId, docType, docNumber);
                    String response = "User created successfully with ID: " + userId;
                    sendResponse(exchange, response, 201);
                } else {
                    sendResponse(exchange, "Failed to create user", 500);
                }
            } catch (Exception e) {
                e.printStackTrace();
                sendResponse(exchange, "Invalid request format", 400);
            } finally {
                KYCApp.closeConnection(conn);
            }
        }

        private void handleGetRequest(HttpExchange exchange) throws IOException {
            Connection conn = KYCApp.getConnection();
            if (conn == null) {
                sendResponse(exchange, "Database connection failed", 500);
                return;
            }

            try {
                // This is a simplified way to get all users. In a real app, you would
                // read the data and format it into a string.
                sendResponse(exchange, "Data retrieved successfully. Check your Java console for details.", 200);
            } finally {
                KYCApp.closeConnection(conn);
            }
        }
    }

    // Handles requests to /api/users/{id}
    static class SpecificUserHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            
            String path = exchange.getRequestURI().getPath();
            int userId;
            try {
                userId = Integer.parseInt(path.substring(path.lastIndexOf('/') + 1));
            } catch (NumberFormatException e) {
                sendResponse(exchange, "Invalid user ID", 400);
                return;
            }

            Connection conn = KYCApp.getConnection();
            if (conn == null) {
                sendResponse(exchange, "Database connection failed", 500);
                return;
            }
            
            try {
                if ("PUT".equalsIgnoreCase(exchange.getRequestMethod())) {
                    InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), "utf-8");
                    BufferedReader br = new BufferedReader(isr);
                    String formData = br.lines().collect(java.util.stream.Collectors.joining());
                    Map<String, String> data = parseFormData(formData);
                    String newAddress = data.get("address");
                    KYCApp.updateUserAddress(conn, userId, newAddress);
                    sendResponse(exchange, "User updated successfully", 200);
                } else if ("DELETE".equalsIgnoreCase(exchange.getRequestMethod())) {
                    KYCApp.deleteUser(conn, userId);
                    sendResponse(exchange, "User deleted successfully", 200);
                } else {
                    sendResponse(exchange, "Method Not Allowed", 405);
                }
            } catch (Exception e) {
                e.printStackTrace();
                sendResponse(exchange, "An error occurred", 500);
            } finally {
                KYCApp.closeConnection(conn);
            }
        }
    }

    private static void sendResponse(HttpExchange exchange, String response, int statusCode) throws IOException {
        exchange.sendResponseHeaders(statusCode, response.length());
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}
