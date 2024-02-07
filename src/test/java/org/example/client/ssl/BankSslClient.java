package org.example.client.ssl;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import io.grpc.stub.StreamObserver;
import org.example.proto.*;
import org.example.rpctypes.BalanceStreamObserver;
import org.example.rpctypes.MoneyStreamingObserver;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import javax.net.ssl.SSLException;
import java.io.File;
import java.util.concurrent.CountDownLatch;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BankSslClient {
    private BankServiceGrpc.BankServiceBlockingStub blockingStub;
    private BankServiceGrpc.BankServiceStub asyncStub;

    @BeforeAll
    public void setUp() throws SSLException {
        SslContext sslContext = GrpcSslContexts.forClient()
                .trustManager(new File("/path/to/public-cert/of/CA/that/signed/our/self-signed/cert"))
                .build();
        // For https connection we do not need to use .usePlaintext()
        // Here also NettyChannelBuilder is needed for sslContext setting. NettyChannelBuilder is the default
        // implementation of ManagedChannelBuilder
        ManagedChannel managedChannel = NettyChannelBuilder.forAddress("localhost", 6565)
                .sslContext(sslContext)
          //      .usePlaintext()
                .build();
        this.blockingStub = BankServiceGrpc.newBlockingStub(managedChannel);
        this.asyncStub = BankServiceGrpc.newStub(managedChannel);
    }

    // Unary client request
    @Test
    public void getBalanceUnaryBlockingClient(){
        BalanceCheckRequest request = BalanceCheckRequest.newBuilder()
                .setAccountNumber(5)
                .build();
        Balance balance = this.blockingStub
                .getBalance(request);
        System.out.println("Received balance: " + balance.getAmount());
    }

    // Server streaming blocking client
    @Test
    public void withdrawServerStreamingBlockingClient(){
        WithdrawRequest withdrawRequest = WithdrawRequest
                .newBuilder()
                .setAccountNumber(7)
                .setAmount(40)
                .build();
        this.blockingStub.withdraw(withdrawRequest)
                // here we define action for each received streaming message
                .forEachRemaining(money -> System.out.println("Received : " + money.getValue()));
    }

    // Server-streaming async client
    // Async client requires the implementation of StreamObserver to act on next message/completion/failure
    @Test
    public void withdrawServerStreamingAsyncClient() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        WithdrawRequest withdrawRequest = WithdrawRequest.newBuilder()
                .setAccountNumber(10)
                .setAmount(50)
                .build();

        this.asyncStub.withdraw(withdrawRequest, new MoneyStreamingObserver(latch));
        latch.await();
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
