package org.example.rpctypes;

import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.example.proto.Money;
import org.example.proto.WithdrawalError;

import java.util.concurrent.CountDownLatch;

// Defines the action of the async grpc client on receiving a message/completion/failure
public class MoneyStreamingObserver implements StreamObserver<Money> {

    private final CountDownLatch latch;

    public MoneyStreamingObserver(CountDownLatch latch) {
        this.latch = latch;
    }

    @Override
    public void onNext(Money money) {
        System.out.println("Received async: " + money.getValue());
    }

    @Override
    public void onError(Throwable throwable) {
        Status status = Status.fromThrowable(throwable);
        Metadata metadata = Status.trailersFromThrowable(throwable);
        WithdrawalError withdrawalError = metadata.get(ClientConstants.WITHDRAWAL_ERROR_KEY);
        System.out.println(withdrawalError.getAmount() + " : " + withdrawalError.getErrorMessage());
        System.out.println(throwable.getMessage());
        latch.countDown();
    }

    @Override
    public void onCompleted() {
        System.out.println("Server is done!");
        latch.countDown();
    }
}
