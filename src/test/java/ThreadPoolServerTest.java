import http.servers.BlockingServerImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class ThreadPoolServerTest {

    private static final int TEST_PORT = 8085;
    private BlockingServerImpl server;
    private ExecutorService executor;
    private Thread serverThread;

    @BeforeEach
    void setUp() throws Exception {
        executor = Executors.newFixedThreadPool(4);
        server = new BlockingServerImpl(TEST_PORT, executor);

        serverThread = new Thread(server::startServer);
        serverThread.start();

        waitForPortOpen(Duration.ofSeconds(5));
    }

    @AfterEach
    void tearDown() throws Exception {
        server.stopServer();
        if (serverThread != null && serverThread.isAlive()) {
            serverThread.join(5000); // wait up to 5 seconds
        }
        executor.shutdownNow();
    }

    private void waitForPortOpen(Duration timeout) throws Exception {
        long deadline = System.nanoTime() + timeout.toNanos();
        while (System.nanoTime() < deadline) {
            try (Socket s = new Socket()) {
                s.connect(new InetSocketAddress("localhost", ThreadPoolServerTest.TEST_PORT), 200); // 200ms connect timeout
                return; // success
            } catch (IOException e) {
                Thread.sleep(100);
            }
        }
        fail("Server did not start and bind to port " + ThreadPoolServerTest.TEST_PORT + " within timeout");
    }

    private String sendHttpRequest(String requestPath) throws IOException {
        try (Socket socket = new Socket("localhost", TEST_PORT);
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            writer.write("GET " + requestPath + " HTTP/1.1\r\n");
            writer.write("Host: localhost\r\n");
            writer.write("\r\n");
            writer.flush();

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line).append("\n");
                if (line.isEmpty()) break;
            }
            return response.toString();
        }
    }

    @Test
    void testRootEndpointReturnsOk() throws IOException {
        String response = sendHttpRequest("/");
        assertTrue(response.contains("200 OK"), "Expected 200 OK but got: " + response);
    }

    @Test
    void testIoEndpointReturnsOk() throws IOException {
        String response = sendHttpRequest("/io");
        assertTrue(response.contains("200 OK"), "Expected 200 OK but got: " + response);
    }

    @Test
    void testComputeEndpointReturnsOk() throws IOException {
        String response = sendHttpRequest("/compute");
        assertTrue(response.contains("200 OK"), "Expected 200 OK but got: " + response);
    }

    @Test
    void testInvalidEndpointReturns404() throws IOException {
        String response = sendHttpRequest("/invalid");
        assertTrue(response.contains("404 Not Found"), "Expected 404 Not Found but got: " + response);
    }

    @Test
    void testRequestTimeoutReturns503() throws Exception {
        try (Socket socket = new Socket("localhost", TEST_PORT);
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            writer.write("GET /compute HTTP/1.1\r\n");
            writer.flush();

            socket.setSoTimeout(4000);
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line).append("\n");
                if (line.isEmpty()) break;
            }

            assertTrue(response.toString().contains("503") || response.toString().isEmpty(),
                    "Expected 503 or empty response for timeout, but got: " + response);
        }
    }

    @Test
    void testServerStartAndStop() throws InterruptedException {
        assertTrue(serverThread.isAlive(), "Server thread should be running");
        server.stopServer();

        serverThread.join(5000);
        assertFalse(serverThread.isAlive(), "Server thread should stop after stopServer()");
    }
}
