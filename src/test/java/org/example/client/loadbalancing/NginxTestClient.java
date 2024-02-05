package org.example.client.loadbalancing;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.example.proto.Balance;
import org.example.proto.BalanceCheckRequest;
import org.example.proto.BankServiceGrpc;
import org.example.proto.DepositRequest;
import org.example.rpctypes.BalanceStreamObserver;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.concurrent.CountDownLatch;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class NginxTestClient {

    private BankServiceGrpc.BankServiceBlockingStub blockingStub;
    private BankServiceGrpc.BankServiceStub asyncStub;

    @BeforeAll
    public void setUp(){
        // ManagedChannel is an object that represents HTTP2 connection between Client and Server
        ManagedChannel managedChannel = ManagedChannelBuilder.forAddress("localhost", 8585)
                .usePlaintext()
                .build();
        this.blockingStub = BankServiceGrpc.newBlockingStub(managedChannel);
        this.asyncStub = BankServiceGrpc.newStub(managedChannel);
    }

    // Unary client request
    @Test
    public void getBalanceUnaryBlockingClient(){
        for (int i = 1; i < 11; i++) {
            BalanceCheckRequest request = BalanceCheckRequest.newBuilder()
                    .setAccountNumber(i)
                    .build();
            Balance balance = this.blockingStub.getBalance(request);
            System.out.println("Received balance: " + balance.getAmount());
        }
    }

    // Client-streaming is only possible with the async client.
    // So basically, the client passes the logic (StreamObserver) it will apply to the incoming balance message (including
    // onNext, onError, onCompleted actions) to the server. The server returns the StreamObserver on which the client
    // will perform actions needed to fill the stream with data (onNext and then onCompleted) or send error signal (onError)
    @Test
    public void cashDepositRequestClientStreamingAsyncClient() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        StreamObserver<DepositRequest> observer = this.asyncStub.cashDeposit(new BalanceStreamObserver(latch));
        for (int i = 0; i < 10; i++) {
            DepositRequest request = DepositRequest.newBuilder()
                    .setAccountNumber(8)
                    .setAmount(10)
                    .build();
            // through the StreamObserver we send each request as part of the client stream
            observer.onNext(request);
        }
        // and then we complete the stream
        observer.onCompleted();
        latch.await();
    }
}
