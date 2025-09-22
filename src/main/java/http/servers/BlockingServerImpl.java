package http.servers;

import java.util.concurrent.ExecutorService;

public class BlockingServerImpl extends BlockingServer {

    public BlockingServerImpl(int port, ExecutorService executor) {
        super(port, executor);
    }
}
