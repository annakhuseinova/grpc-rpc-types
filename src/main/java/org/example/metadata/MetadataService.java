package org.example.metadata;

import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.protobuf.ProtoUtils;
import io.grpc.stub.StreamObserver;
import org.example.proto.*;
import org.example.rpctypes.AccountDatabase;
import org.example.rpctypes.CashStreamingRequestObserver;

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
        if (amount < 10 || (amount % 10) != 0){
            Metadata metadata = new Metadata();
            Metadata.Key<WithdrawalError> errorKey = ProtoUtils.keyForProto(WithdrawalError.getDefaultInstance());
            WithdrawalError error = WithdrawalError.newBuilder().setAmount(balance).setErrorMessage(ErrorMessage.ONLY_TEN_MULTIPLES).build();
            metadata.put(errorKey, error);
            responseObserver.onError(Status.FAILED_PRECONDITION.asRuntimeException(metadata));
        }

        //
        if (balance < amount){
            Metadata metadata = new Metadata();
            Metadata.Key<WithdrawalError> errorKey = ProtoUtils.keyForProto(WithdrawalError.getDefaultInstance());
            WithdrawalError error = WithdrawalError.newBuilder().setAmount(balance).setErrorMessage(ErrorMessage.INSUFFICIENT_BALANCE).build();
            metadata.put(errorKey, error);
            responseObserver.onError(Status.FAILED_PRECONDITION.asRuntimeException(metadata));
            return;
        }
        // all validations passed
        for (int i = 0; i < (amount/10); i++) {
            Money money = Money.newBuilder().setValue(10).build();
            responseObserver.onNext(money);
            AccountDatabase.deductBalance(accountNumber, 10);
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
