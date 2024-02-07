package org.example.metadata;

import com.google.common.util.concurrent.Uninterruptibles;
import io.grpc.Context;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.example.proto.*;
import org.example.rpctypes.AccountDatabase;
import org.example.rpctypes.CashStreamingRequestObserver;

import java.util.concurrent.TimeUnit;

public class MetadataService extends BankServiceGrpc.BankServiceImplBase {

    // responseObserver - allows to write either final response or throw an error
    @Override
    public void getBalance(BalanceCheckRequest request,
                           StreamObserver<Balance> responseObserver) {
        int accountNumber = request.getAccountNumber();
        int amount = AccountDatabase.getBalance(accountNumber);
        UserRole userRole = ServerConstants.CTX_USER_ROLE.get();
        amount = UserRole.PRIME.equals(userRole) ? amount : (amount - 15);
        Balance balance = Balance.newBuilder()
                .setAmount(amount)
                .build();

        responseObserver.onNext(balance);
        responseObserver.onCompleted();
    }

    // Server-streaming
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

            // Simulating time-consuming call ...
            Uninterruptibles.sleepUninterruptibly(3, TimeUnit.SECONDS);
            // Context object represents information about the current rpc.
            if (!Context.current().isCancelled()){
                responseObserver.onNext(money);
                System.out.println("Delivered 10$");
                AccountDatabase.deductBalance(accountNumber, 10);
            } else {
                break;
            }
        }
        System.out.println("Completed!");
        responseObserver.onCompleted();
    }

    // The method created to accept client streaming and return a single response should return
    // instance of the StreamObserver through which the client will push requests using onNext method.
    @Override
    public StreamObserver<DepositRequest> cashDeposit(StreamObserver<Balance> responseObserver) {
        return new CashStreamingRequestObserver(responseObserver);
    }
}
