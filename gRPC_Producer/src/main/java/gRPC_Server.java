import Sources.UserService;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class gRPC_Server {
    private static final Logger logger = Logger.getLogger(gRPC_Server.class.getName());

    public static void main(String[] args) throws IOException, InterruptedException {
        Server server = ServerBuilder.forPort(8080).addService(new UserService()).build();

        server.start();

        logger.info("Server Started at port no : " + server.getPort());
        server.awaitTermination();
    }
}

