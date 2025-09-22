import http.servers.BlockingServerImpl;
import http.servers.ReactiveServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import http.servers.HTTPServer;

import java.util.concurrent.Executors;

public class Main {

    private static final int DEFAULT_PORT = 4221;
    private static final Logger LOGGER = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        LOGGER.info("Starting http server with {} cores", Runtime.getRuntime().availableProcessors());

        if (args.length == 0) {
            throw new IllegalArgumentException("Please specify server type!");
        }

        final int port = args.length > 1 ? Integer.parseInt(args[1]) : DEFAULT_PORT;
        final HTTPServer server;
        switch (args[0]) {
            case "ThreadPool" -> {
                LOGGER.info("Starting thread pool server!");
                server = new BlockingServerImpl(port, Executors.newCachedThreadPool());
            }
            case "Reactive" -> {
                LOGGER.info("Starting reactive thread pool server!");
                server = new ReactiveServer(port);
            }
            case "VirtualThread" -> {
                LOGGER.info("Starting virtual thread pool server!");
                server = new BlockingServerImpl(port, Executors.newVirtualThreadPerTaskExecutor());
            }
            default -> server = new BlockingServerImpl(port, Executors.newCachedThreadPool());
        }

        final Runnable serverTask = server::startServer;
        final var serverThread = new Thread(serverTask);
        serverThread.start();
    }
}
