import http.servers.ReactiveServer;
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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class ReactiveServerTest {

    private static final int TEST_PORT = 8090;
    private ReactiveServer server;

    @BeforeEach
    void setUp() throws Exception {
        server = new ReactiveServer(TEST_PORT);
        server.startServer();

        waitForPortOpen("localhost", TEST_PORT, Duration.ofSeconds(5));
    }

    @AfterEach
    void tearDown() throws Exception {
        server.stopServer();
        Thread.sleep(200); // give Vert.x time to release port
    }

    private void waitForPortOpen(String host, int port, Duration timeout) throws Exception {
        long deadline = System.nanoTime() + timeout.toNanos();
        while (System.nanoTime() < deadline) {
            try (Socket s = new Socket()) {
                s.connect(new InetSocketAddress(host, port), 200);
                return; // success
            } catch (IOException e) {
                Thread.sleep(100);
            }
        }
        fail("Server did not start on port " + port + " within timeout");
    }

    private String sendHttpRequest(String path) throws IOException {
        try (Socket socket = new Socket("localhost", TEST_PORT);
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            writer.write("GET " + path + " HTTP/1.1\r\n");
            writer.write("Host: localhost\r\n");
            writer.write("\r\n");
            writer.flush();

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line).append("\n");
                if (line.isEmpty()) break; // stop after headers
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
}
