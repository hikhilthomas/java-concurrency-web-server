package http.exceptions;

public class RequestParseException extends RuntimeException {
    public RequestParseException(String message) {
        super(message);
    }
}
