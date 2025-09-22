package http.handlers;

import http.exceptions.RequestParseException;
import http.message.HTTPRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class BlockingHTTPHandler implements HTTPHandler<Socket> {

    private static final Logger LOGGER = LogManager.getLogger(BlockingHTTPHandler.class);

    public void handle(Socket clientSocket) {
        if (clientSocket.isClosed()) return;
        try (clientSocket;
             final BufferedReader requestReader = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream()));
             final BufferedWriter responseWriter = new BufferedWriter(
                     new OutputStreamWriter(clientSocket.getOutputStream()))
        ) {
            clientSocket.setSoTimeout(3000);
            final HTTPRequest request = HTTPRequest
                    .parse(requestReader)
                    .orElseThrow(() -> new RequestParseException("Unable to parse request"));

            var response = HTTPConstants.HTTP_OK;
            switch (request.getTarget()) {
                case "/" -> {
                    LOGGER.info("Simple HTTP request received.");
                }
                case "/io" -> {
                    IOTask();
                    LOGGER.info("IO task successfully completed.");
                }
                case "/compute" -> {
                    final int computeOutput = computeTask();
                    LOGGER.info("Compute task result: {}", computeOutput);
                }
                default -> {
                    LOGGER.info("Invalid request target: {}", request.getTarget());
                    response = HTTPConstants.RESPONSE_NOT_FOUND;
                }
            }
            responseWriter.write(response);
        } catch (SocketTimeoutException e) {
            LOGGER.error("Request timed out from client {}", clientSocket.getRemoteSocketAddress());
            try (BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(clientSocket.getOutputStream()))) {
                writer.write(HTTPConstants.RESPONSE_SERVICE_UNAVAILABLE);
            } catch (IOException ignored) {}
        } catch (IOException | RequestParseException e) {
            LOGGER.error(e);
        }
    }

    private void IOTask() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            LOGGER.error("DB connection interrupted");
        }
    }
}
