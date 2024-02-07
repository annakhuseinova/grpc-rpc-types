package org.example.client.metadata;

import io.grpc.Deadline;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.MetadataUtils;
import io.grpc.stub.StreamObserver;
import org.example.interceptor.DeadlineInterceptor;
import org.example.metadata.UserSessionToken;
import org.example.proto.*;
import org.example.rpctypes.BalanceStreamObserver;
import org.example.rpctypes.MoneyStreamingObserver;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MetadataClientTest {
    private BankServiceGrpc.BankServiceBlockingStub blockingStub;
    private BankServiceGrpc.BankServiceStub asyncStub;

    @BeforeAll
    public void setUp(){
        // ManagedChannel is an object that represents HTTP2 connection between Client and Server
        ManagedChannel managedChannel = ManagedChannelBuilder.forAddress("localhost", 6565)
                .intercept(MetadataUtils.newAttachHeadersInterceptor(ClientConstants.getClientToken()))
                .intercept(new DeadlineInterceptor())
                .usePlaintext()
                .build();
        this.blockingStub = BankServiceGrpc.newBlockingStub(managedChannel);
        this.asyncStub = BankServiceGrpc.newStub(managedChannel);
    }

    // Unary client request with deadline
    @Test
    public void getBalanceUnaryBlockingClient(){
        BalanceCheckRequest request = BalanceCheckRequest.newBuilder()
                .setAccountNumber(5)
                .build();
        for (int i = 0; i < 20; i++) {
            int randomNumber = ThreadLocalRandom.current().nextInt(1, 4);
            try {
                Balance balance = this.blockingStub
                        .withCallCredentials(new UserSessionToken("user-secret-" + randomNumber + ":prime"))
                        .withDeadline(Deadline.after(2, TimeUnit.SECONDS))
                        .getBalance(request);
                System.out.println("Received balance: " + balance.getAmount());
            } catch (StatusRuntimeException e) {
                // 4 status code - DEADLINE_EXCEEDED - so we handle the case appropriately
                int value = e.getStatus().getCode().value();
                if (value == 4) {
                    return;
                }
            }
        }
    }

    // Server streaming blocking client
    @Test
    public void withdrawServerStreamingBlockingClient(){
        WithdrawRequest withdrawRequest = WithdrawRequest
                .newBuilder()
                .setAccountNumber(7)
                .setAmount(40)
                .build();
        try {
            this.blockingStub
                    .withDeadline(Deadline.after(4, TimeUnit.SECONDS))
                    .withdraw(withdrawRequest)
                    // here we define action for each received streaming message
                    .forEachRemaining(money -> System.out.println("Received : " + money.getValue()));
        } catch (StatusRuntimeException e){
            // 4 status code - DEADLINE_EXCEEDED - so we handle the case appropriately
            int value = e.getStatus().getCode().value();
            if (value == 4){
                return;
            }
        }
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
