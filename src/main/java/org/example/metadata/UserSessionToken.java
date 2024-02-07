package org.example.metadata;

import io.grpc.CallCredentials;
import io.grpc.Metadata;
import io.grpc.Status;

import java.util.concurrent.Executor;

/*
* CallCredentials - carries credential data that will be propagated to the server via request metadata for each RPC.
* */
public class UserSessionToken extends CallCredentials {

    private final String jwt;

    public UserSessionToken(String jwt) {
        this.jwt = jwt;
    }

    // applier also has fail() method to implement failure cases
    @Override
    public void applyRequestMetadata(RequestInfo requestInfo, Executor appExecutor, MetadataApplier applier) {
        appExecutor.execute(()-> {
            try {
                Metadata metadata = new Metadata();
                metadata.put(ServerConstants.USER_TOKEN, this.jwt);
                applier.apply(metadata);
            } catch (Exception e){
                e.printStackTrace();
                applier.fail(Status.PERMISSION_DENIED);
            }
        });

    }

    @Override
    public void thisUsesUnstableApi() {
        // may change in the future
    }

}
