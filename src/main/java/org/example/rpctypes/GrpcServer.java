package org.example.rpctypes;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.example.rpctypes.bidirectionalstreaming.TransferService;

import java.io.IOException;

public class GrpcServer {
    public static void main(String[] args) throws IOException, InterruptedException {

        Server server = ServerBuilder.forPort(6565)
                .addService(new BankService()) //
                .addService(new TransferService())// adding webservice to the server
                .build();

        server.start();

        // awaitTermination() - makes the server wait for the manual termination. Otherwise - the server will stop
        // listening once the main method completes execution
        server.awaitTermination();

    }
}
