package org.example.client.metadata;

import io.grpc.Metadata;

public class ClientConstants {

    /*
    * Metadata basically represents Http header
    * */
    private static final Metadata METADATA = new Metadata();

    static {
        METADATA.put(Metadata.Key.of("client-token", Metadata.ASCII_STRING_MARSHALLER), "bank-client-secret");
    }

    public static Metadata getClientToken(){
        return METADATA;
    }
}
