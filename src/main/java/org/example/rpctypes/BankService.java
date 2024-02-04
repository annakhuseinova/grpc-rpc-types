package org.example;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.example.proto.*;

public class BankService extends BankServiceGrpc.BankServiceImplBase {

    // responseObserver - allows to write either final response or throw an error
    @Override
    public void getBalance(BalanceCheckRequest request,
                           StreamObserver<Balance> responseObserver) {
        int accountNumber = request.getAccountNumber();
        Balance balance = Balance.newBuilder()
                .setAmount(AccountDatabase.getBalance(accountNumber))
                .build();
        responseObserver.onNext(balance);
        responseObserver.onCompleted();
    }

    // responseObserver - allows to write either final response or streaming message or throw an error
    @Override
    public void withdraw(WithdrawRequest request, StreamObserver<Money> responseObserver) {
        int accountNumber = request.getAccountNumber();
        int amount = request.getAmount();
        int balance = AccountDatabase.getBalance(accountNumber);

        //
        if (balance < amount){
            Status status = Status.FAILED_PRECONDITION.withDescription("Not enough money. You have only " + balance);
            responseObserver.onError(status.asRuntimeException());
            return;
        }
        // all validations passed
        for (int i = 0; i < (amount/10); i++) {
            Money money = Money.newBuilder().setValue(10).build();
            responseObserver.onNext(money);
            AccountDatabase.deductBalance(accountNumber, 10);
            responseObserver.onCompleted();
        }
    }

    // The method created to accept client streaming and return a single response should return
    // instance of the StreamObserver through which the client will push requests using onNext method.
    @Override
    public StreamObserver<DepositRequest> cashDeposit(StreamObserver<Balance> responseObserver) {
        return new CashStreamingRequestObserver(responseObserver);
    }
}
