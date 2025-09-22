package http.servers;

import http.handlers.HTTPHandler;
import http.handlers.ReactiveHTTPHandler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ReactiveServer implements HTTPServer {

    private final int PORT;
    private final Vertx vertx;
    private HttpServer vertxHttpServer;
    private final HTTPHandler<HttpServerRequest>  handler;
    private static final Logger LOGGER = LogManager.getLogger(ReactiveServer.class);

    public ReactiveServer(int port) {
        PORT = port;
        LOGGER.info("Server listening on port {}", port);
        vertx = Vertx.vertx();
        handler = new ReactiveHTTPHandler(vertx);
    }

    @Override
    public void startServer() {
        vertxHttpServer =  vertx.createHttpServer();
        vertxHttpServer.requestHandler(handler::handle)
                .listen(PORT, result -> {
                    if (result.succeeded()) {
                        LOGGER.info("Reactive server started on port {}", PORT);
                    } else {
                        LOGGER.error("Failed to start reactive server", result.cause());
                    }
                });
    }

    @Override
    public void stopServer() {
        if (vertxHttpServer != null) {
            vertxHttpServer.close(result -> {
                if (result.succeeded()) {
                    LOGGER.info("Reactive server gracefully stopped!");
                } else  {
                    LOGGER.error("Failed to stop reactive server", result.cause());
                }
            });
        }
        vertx.close();
    }
}
