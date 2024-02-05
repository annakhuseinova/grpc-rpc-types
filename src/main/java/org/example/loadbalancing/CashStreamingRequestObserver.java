package org.example.loadbalancing;

import io.grpc.stub.StreamObserver;
import org.example.proto.Balance;
import org.example.proto.DepositRequest;
import org.example.rpctypes.AccountDatabase;

// For client-streaming example.
// Defines the actions the server will perform when having received client streaming
public class CashStreamingRequestObserver implements StreamObserver<DepositRequest> {

    private final StreamObserver<Balance> balanceStreamObserver;
    private int accountBalance;

    public CashStreamingRequestObserver(StreamObserver<Balance> balanceStreamObserver) {
        this.balanceStreamObserver = balanceStreamObserver;
    }

    @Override
    public void onNext(DepositRequest depositRequest) {
        int accountNumber = depositRequest.getAccountNumber();
        System.out.println("Received cash deposit for: " + accountNumber);
        int amount = depositRequest.getAmount();
        this.accountBalance = AccountDatabase.addBalance(accountNumber, amount);
    }

    @Override
    public void onError(Throwable throwable) {
        System.out.println(throwable.getMessage());
    }

    // Once the client completes the requests, this method is invoked. Here we are sending back
    // the final balance and sends the complete signal.
    @Override
    public void onCompleted() {
        Balance balance = Balance.newBuilder().setAmount(this.accountBalance).build();
        this.balanceStreamObserver.onNext(balance);
        this.balanceStreamObserver.onCompleted();
    }
}
