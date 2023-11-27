package org.example;

import io.grpc.stub.StreamObserver;
import org.example.proto.Account;
import org.example.proto.TransferRequest;
import org.example.proto.TransferResponse;
import org.example.proto.TransferStatus;

public class TransferRequestStreamObserver implements StreamObserver<TransferRequest> {

    private StreamObserver<TransferResponse> transferResponseObserver;

    public TransferRequestStreamObserver(StreamObserver<TransferResponse> transferResponseObserver) {
        this.transferResponseObserver = transferResponseObserver;
    }

    @Override
    public void onNext(TransferRequest transferRequest) {
        int fromAccount = transferRequest.getFromAccount();
        int toAccount = transferRequest.getToAccount();
        int amount = transferRequest.getAmount();
        int balance = AccountDatabase.getBalance(fromAccount);
        TransferStatus status = TransferStatus.FAILED;
        if (balance >= amount && fromAccount != toAccount){
            AccountDatabase.deductBalance(fromAccount, amount);
            AccountDatabase.addBalance(toAccount, amount);
            status = TransferStatus.SUCCESS;
        }
        Account fromAccountInfo = Account
                .newBuilder()
                .setAccountNumber(fromAccount)
                .setAmount(AccountDatabase.getBalance(fromAccount))
                .build();
        Account toAccountInfo = Account
                .newBuilder()
                .setAccountNumber(toAccount)
                .setAmount(AccountDatabase.getBalance(toAccount))
                .build();
        TransferResponse transferResponse = TransferResponse.newBuilder()
                .addAccounts(fromAccountInfo)
                .addAccounts(toAccountInfo)
                .setStatus(status)
                .build();
        transferResponseObserver.onNext(transferResponse);
    }

    @Override
    public void onError(Throwable t) {

    }

    @Override
    public void onCompleted() {
        AccountDatabase.printAccountDetails();
        this.transferResponseObserver.onCompleted();
    }
}
