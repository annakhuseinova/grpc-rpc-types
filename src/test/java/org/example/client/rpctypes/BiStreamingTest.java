package org.example.client.rpctypes;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.example.rpctypes.bidirectionalstreaming.TransferResponseStreamObserver;
import org.example.proto.TransferRequest;
import org.example.proto.TransferServiceGrpc;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BiStreamingTest {

    private TransferServiceGrpc.TransferServiceStub asyncStub;

    @BeforeAll
    void setUp(){
        // ManagedChannel is an object that represents HTTP2 connection between Client and Server
        ManagedChannel managedChannel = ManagedChannelBuilder.forAddress("localhost", 6565)
                .usePlaintext()
                .build();
        this.asyncStub = TransferServiceGrpc.newStub(managedChannel);
    }

    @Test
    void bidirectionalClient() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        TransferResponseStreamObserver responseObserver = new TransferResponseStreamObserver(latch);
        StreamObserver<TransferRequest> requestObserver = this.asyncStub.transfer(responseObserver);

        for (int i = 0; i < 100; i++) {
            TransferRequest transferRequest = TransferRequest.newBuilder()
                    .setFromAccount(ThreadLocalRandom.current().nextInt(1, 11))
                    .setToAccount(ThreadLocalRandom.current().nextInt(1, 11))
                    .setAmount(ThreadLocalRandom.current().nextInt(1, 21))
                    .build();
            requestObserver.onNext(transferRequest);
        }
        requestObserver.onCompleted();
        latch.await();
    }
}
