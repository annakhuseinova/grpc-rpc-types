package org.example.rpctypes.bidirectionalstreaming;

import io.grpc.stub.StreamObserver;
import org.example.proto.TransferRequest;
import org.example.proto.TransferResponse;
import org.example.proto.TransferServiceGrpc;

public class TransferService extends TransferServiceGrpc.TransferServiceImplBase {

    @Override
    public StreamObserver<TransferRequest> transfer(StreamObserver<TransferResponse> responseObserver) {
        return new TransferRequestStreamObserver(responseObserver);
    }
}
