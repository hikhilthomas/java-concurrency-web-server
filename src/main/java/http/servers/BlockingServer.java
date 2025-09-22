package http.servers;

import http.handlers.HTTPConstants;
import http.handlers.HTTPHandler;
import http.handlers.BlockingHTTPHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class BlockingServer implements HTTPServer {
    private ServerSocket serverSocket;
    private final int PORT;
    private volatile boolean running;
    private final HTTPHandler<Socket> httpHandler;
    private final ExecutorService executor;
    private final ScheduledExecutorService scheduledExecutor = Executors
            .newScheduledThreadPool(2);

    private static final Logger LOGGER = LogManager.getLogger(BlockingServer.class);

    public BlockingServer(int port, ExecutorService executorService) {
        httpHandler = new BlockingHTTPHandler();
        executor = executorService;
        LOGGER.info("Server listening on port {}", port);
        PORT = port;
        running = true;
    }

    @Override
    public void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            serverSocket.setReuseAddress(true);
            this.serverSocket = serverSocket;
            while (running) {
                final var clientSocket = serverSocket.accept();
                try {
                    final Future<?> future = executor.submit(() -> httpHandler.handle(clientSocket));
                    scheduledExecutor.schedule(() -> {
                                if (!future.isDone()) {
                                    future.cancel(true);
                                    gracefullyExitClientConnection(clientSocket);
                                }
                            },
                            3,
                            TimeUnit.SECONDS
                    );
                } catch (RejectedExecutionException e) {
                    LOGGER.error("RejectedExecutionException, queue size : {}", executor);
                    handleQueueOverflow(clientSocket);
                }
            }
        } catch (SocketException e) {
            if (!running) {
                LOGGER.info("Socket closed, server shutting down gracefully");
            } else {
                LOGGER.error("Server error", e);
            }
        } catch (IOException e) {
            LOGGER.error("Error starting server on port {}", PORT, e);
        }
    }

    private void handleQueueOverflow(Socket clientSocket) {
        LOGGER.error("Request rejected: queue already full");
        gracefullyExitClientConnection(clientSocket);
    }

    private void gracefullyExitClientConnection(Socket clientSocket) {
        if (clientSocket.isClosed()) return;
        // Gracefully inform the client instead of just dropping connection
        try (BufferedWriter writer = new BufferedWriter(
                     new OutputStreamWriter(clientSocket.getOutputStream()))) {
            writer.write(HTTPConstants.RESPONSE_SERVICE_UNAVAILABLE);
        } catch (IOException closeEx) {
            LOGGER.error("Error closing rejected client socket", closeEx);
        }
    }

    @Override
    public void stopServer() {
        LOGGER.info("Shutting down server!");
        running = false;
        executor.shutdown();
        try {
            serverSocket.close();
        } catch (IOException e) {
            LOGGER.error("Unable to close Server socket", e);
        }
    }
}
