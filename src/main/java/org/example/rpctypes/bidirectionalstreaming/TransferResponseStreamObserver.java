package org.example.rpctypes.bidirectionalstreaming;

import io.grpc.stub.StreamObserver;
import org.example.proto.TransferResponse;

import java.util.concurrent.CountDownLatch;

public class TransferResponseStreamObserver implements StreamObserver<TransferResponse> {

    private final CountDownLatch latch;

    public TransferResponseStreamObserver(CountDownLatch latch) {
        this.latch = latch;
    }

    @Override
    public void onNext(TransferResponse transferResponse) {
        System.out.println("Status: " + transferResponse.getStatus());
        transferResponse.getAccountsList()
                .stream()
                .map(account -> account.getAccountNumber() + " : " + account.getAmount())
                .forEach(System.out::println);

    }

    @Override
    public void onError(Throwable t) {
        this.latch.countDown();
    }

    @Override
    public void onCompleted() {
        System.out.println("All transfers are done!");
        this.latch.countDown();
    }
}
