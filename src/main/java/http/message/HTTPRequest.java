package http.message;

import http.exceptions.RequestParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class HTTPRequest {
    private final String method;
    private final String target;
    private final String version;
    private final Map<String, String> headers;
    private final String body;

    public HTTPRequest(String method, String target, String version,
                       Map<String, String> headers, String body) {
        this.method = method;
        this.target = target;
        this.version = version;
        this.headers = headers;
        this.body = body;
    }

    public String getMethod() { return method; }
    public String getTarget() { return target; }
    public String getVersion() { return version; }
    public Map<String, String> getHeaders() { return headers; }
    public String getBody() { return body; }

    public static Optional<HTTPRequest> parse(BufferedReader reader) throws IOException {
        final String requestLine = reader.readLine();
        if (requestLine == null || requestLine.isEmpty()) {
            throw new RequestParseException("Request line is empty");
        }
        final String[] parts = requestLine.split(" ");
        if (parts.length != 3) {
            throw new RequestParseException("Invalid request line");
        }
        final String method = parts[0],
            target = parts[1],
            version = parts[2];

        final var headers = new HashMap<String, String>();
        String line;
        while (!(line = reader.readLine()).isEmpty()) {
            final int colonIndex = line.indexOf(":");
            if (colonIndex == -1) {
                throw new RequestParseException("Malformed header: " + line);
            }
            final String name = line.substring(0, colonIndex).trim();
            final String value = line.substring(colonIndex + 1).trim();
            headers.put(name, value);
        }

        String body = null;
        if (headers.containsKey("Content-Length")) {
            final int contentLength = Integer.parseInt(headers.get("Content-Length"));
            if (contentLength > 0) {
                final char[] bodyChars = new char[contentLength];
                int totalRead = 0;
                while (totalRead < contentLength) {
                    final int read = reader.read(bodyChars, totalRead, contentLength - totalRead);
                    if (read == -1) break;
                    totalRead += read;
                }
                if (totalRead < contentLength) {
                    // Body shorter than Content-Length
                    throw new RequestParseException("Malformed request content-length: " + totalRead);
                }
                body = new String(bodyChars);
            }
        }
        return Optional.of(new HTTPRequest(method, target, version, headers, body));
    }
}