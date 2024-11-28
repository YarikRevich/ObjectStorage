package com.objectstorage.mapping;

import com.objectstorage.exception.ProcessorContentRemovalFailureException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/** Represents mapper for ProcessorContentRemovalFailureException exception. */
@Provider
public class ProcessorContentRemovalFailureExceptionMapper
        implements ExceptionMapper<ProcessorContentRemovalFailureException> {
    @Override
    public Response toResponse(ProcessorContentRemovalFailureException e) {
        return Response.status(Response.Status.BAD_REQUEST.getStatusCode())
                .entity(e.getMessage())
                .build();
    }
}