package org.example.interceptor;

import io.grpc.*;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class DeadlineInterceptor implements ClientInterceptor {

    /*
    * Whenever you make a stub call - that call will be intercepted and then sent to the server
    * */
    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method,
                                                               CallOptions callOptions,
                                                               Channel next) {
        Deadline deadline = callOptions.getDeadline();
        if (Objects.isNull(deadline)){
            callOptions = callOptions.withDeadline(Deadline.after(4, TimeUnit.SECONDS));
        }
        // here we set a global deadline for all client calls.
        // Then we can remove all individual withDeadline configs of stubs
        return next.newCall(method, callOptions);
    }
}
