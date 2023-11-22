package org.example.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.example.proto.Balance;
import org.example.proto.BalanceCheckRequest;
import org.example.proto.BankServiceGrpc;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BankClientTest {
    private BankServiceGrpc.BankServiceBlockingStub blockingStub;

    @BeforeAll
    public void setUp(){
        ManagedChannel managedChannel = ManagedChannelBuilder.forAddress("localhost", 6565)
                .usePlaintext()
                .build();
        this.blockingStub = BankServiceGrpc.newBlockingStub(managedChannel);
    }

    @Test
    public void getBalanceTest(){
        BalanceCheckRequest request = BalanceCheckRequest.newBuilder()
                .setAccountNumber(5)
                .build();
        Balance balance = this.blockingStub.getBalance(request);
        System.out.println("Received balance: " + balance.getAmount());
    }
}
