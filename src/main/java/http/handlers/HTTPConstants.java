package http.handlers;

import java.nio.charset.StandardCharsets;

public final class HTTPConstants {
    private HTTPConstants() {}

    public static final String HTTP_OK = "HTTP/1.1 200 OK\r\n";
    public static final String RESPONSE_NOT_FOUND = "HTTP/1.1 404 Not Found\r\n\r\n";
    public static final String RESPONSE_INTERNAL_ERROR = "HTTP/1.1 500 Internal Server Error\r\n\r\n";
    public static final String RESPONSE_SERVICE_UNAVAILABLE= "HTTP/1.1 503 Service Unavailable\r\n\r\n";
    public static final String CONTENT_TYPE_JSON = "application/json";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String CONTENT_LENGTH = "Content-Length";
    public static final String JSON_RESPONSE = "{\"status\":200,\"message\":\"success\"}";
    public static final byte[] RESPONSE_BYTES = JSON_RESPONSE.getBytes(StandardCharsets.UTF_8);
}
