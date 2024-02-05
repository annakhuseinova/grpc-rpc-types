package org.example.client.loadbalancing;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.NameResolverRegistry;
import io.grpc.stub.StreamObserver;
import org.example.loadbalancing.ServiceRegistry;
import org.example.loadbalancing.TempNameResolverProvider;
import org.example.proto.Balance;
import org.example.proto.BalanceCheckRequest;
import org.example.proto.BankServiceGrpc;
import org.example.proto.DepositRequest;
import org.example.rpctypes.BalanceStreamObserver;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ClientSideLoadBalancingTest {

    private BankServiceGrpc.BankServiceBlockingStub blockingStub;
    private BankServiceGrpc.BankServiceStub asyncStub;

    @BeforeAll
    public void setUp(){
        List<String> instances = new ArrayList<>();
        instances.add("localhost:6565");
        instances.add("localhost:7575");
        ServiceRegistry.register("bank-service", instances);

        // We provide custom NameResolverProvider which resolves the service by name
        // using service registry
        NameResolverRegistry.getDefaultRegistry().register(new TempNameResolverProvider());

        // ManagedChannel is an object that represents HTTP2 connection between Client and Server
        ManagedChannel managedChannel = ManagedChannelBuilder
                // now we can just give the name of the service which will be resolved to a concrete address
                .forTarget("bank-service")
                //.forAddress("localhost", 8585)
                // by default "first pick" policy is used (will send all requests to just one instance)
                // We switch to round-robin to distribute the load evenly. With this, multiple subchannels
                // for each instance will be created
                .defaultLoadBalancingPolicy("round-robin")
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
