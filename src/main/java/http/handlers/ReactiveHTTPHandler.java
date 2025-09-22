package http.handlers;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.TimeUnit;

public class ReactiveHTTPHandler implements HTTPHandler<HttpServerRequest> {

    final Vertx vertx;
    private static final Logger LOGGER = LogManager.getLogger(ReactiveHTTPHandler.class);

    public ReactiveHTTPHandler(Vertx vertx) {
        this.vertx = vertx;
    }

    @Override
    public void handle(HttpServerRequest request) {
        String path = request.path();

        switch (path) {
            case "/" -> {
                LOGGER.info("Simple HTTP request received.");
                request.response()
                        .setStatusCode(200)
                        .end("OK");
            }
            case "/io" -> {
                var timeout = vertx.setTimer(3000, tid -> {
                    if (!request.response().ended()) {
                        request.response().setStatusCode(500).end("Internal Server Error");
                    }
                });
                vertx.setTimer(500, id -> {
                    vertx.cancelTimer(timeout);
                    LOGGER.info("IO task successfully completed.");
                    request.response()
                            .setStatusCode(200)
                            .end("OK");
                });
            }
            case "/compute" -> {
                vertx.executeBlocking(() -> {
                    int computeOutput = computeTask();
                    LOGGER.info("Compute task result: {}", computeOutput);
                    return "OK";
                }).timeout(3, TimeUnit.SECONDS)
                        .onComplete(result -> {
                            if (result.succeeded()) {
                                request.response().setStatusCode(200).end(result.result());
                            } else {
                                request.response().setStatusCode(500).end("Internal Server Error");
                            }
                        });
            }
            default -> {
                LOGGER.info("Invalid request target: {}", path);
                request.response()
                        .setStatusCode(404)
                        .end("Not Found");
            }
        }
    }

}
