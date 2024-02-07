package org.example.tls;

import io.grpc.Server;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContextBuilder;

import java.io.File;
import java.io.IOException;

public class GrpcServer {
    public static void main(String[] args) throws IOException, InterruptedException {

        SslContext sslContext = GrpcSslContexts.configure(
                SslContextBuilder.forServer(
                        // path to the certificate file
                    new File(""),
                        // path to the .pem private key file
                        new File("")
                )
        ).build();
        // When we used ServerBuilder - we basically used NettyServerBuilder. So here
        // we use it explicitly to set the SslContext
        Server server = NettyServerBuilder.forPort(6565)
                .sslContext(sslContext)
                .addService(new BankService()) //
                .build();

        server.start();

        // awaitTermination() - makes the server wait for the manual termination. Otherwise - the server will stop
        // listening once the main method completes execution
        server.awaitTermination();

    }
}
