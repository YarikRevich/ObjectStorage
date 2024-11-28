package com.objectstorage.mapping;

import com.objectstorage.exception.ProcessorContentWithdrawalFailureException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/** Represents mapper for ProcessorContentWithdrawalFailureException exception. */
@Provider
public class ProcessorContentWithdrawalFailureExceptionMapper
        implements ExceptionMapper<ProcessorContentWithdrawalFailureException> {
    @Override
    public Response toResponse(ProcessorContentWithdrawalFailureException e) {
        return Response.status(Response.Status.BAD_REQUEST.getStatusCode())
                .entity(e.getMessage())
                .build();
    }
}