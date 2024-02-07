package org.example.rpctypes;

import io.grpc.Metadata;
import io.grpc.protobuf.ProtoUtils;
import org.example.proto.WithdrawalError;

public class ClientConstants {

    public static final Metadata.Key<WithdrawalError> WITHDRAWAL_ERROR_KEY = ProtoUtils.keyForProto(WithdrawalError.getDefaultInstance());
}
