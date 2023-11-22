package org.example;

import io.grpc.stub.StreamObserver;
import org.example.proto.Balance;
import org.example.proto.BalanceCheckRequest;
import org.example.proto.BankServiceGrpc;

public class BankService extends BankServiceGrpc.BankServiceImplBase {
    @Override
    public void getBalance(BalanceCheckRequest request, StreamObserver<Balance> responseObserver) {
        int accountNumber = request.getAccountNumber();
        Balance balance = Balance.newBuilder()
                .setAmount(AccountDatabase.getBalance(accountNumber))
                .build();
        responseObserver.onNext(balance);
        responseObserver.onCompleted();
    }
}
