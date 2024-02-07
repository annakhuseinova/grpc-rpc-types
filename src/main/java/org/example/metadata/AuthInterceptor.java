package org.example.metadata;

import io.grpc.*;

import java.util.Objects;

import static org.example.metadata.ServerConstants.USER_TOKEN;

public class AuthInterceptor implements ServerInterceptor {

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call,
                                                                 Metadata headers,
                                                                 ServerCallHandler<ReqT, RespT> next) {
        String clientToken = headers.get(USER_TOKEN);
        if (this.validate(clientToken)){
            UserRole userRole = this.extractUserRole(clientToken);
            Context context = Context.current().withValue(
                    ServerConstants.CTX_USER_ROLE, userRole
            );
            return Contexts.interceptCall(context, call, headers, next);
            //return next.startCall(call, headers);
        } else {
            Status status = Status.UNAUTHENTICATED.withDescription("invalid token/expired token");
            call.close(status, headers);
        }
        // We return an empty object, not null (as recommended by the documentation)
        return new ServerCall.Listener<>() {};
    }

    public boolean validate(String token){
        return Objects.nonNull(token) && (token.startsWith("user-secret-3") || token.startsWith("user-secret-2"));
    }

    private UserRole extractUserRole(String jwt){
        return jwt.endsWith("prime") ? UserRole.PRIME : UserRole.STANDARD;
    }
}
