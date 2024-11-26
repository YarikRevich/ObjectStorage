package com.objectstorage.mapping;

import com.objectstorage.exception.ProcessorContentApplicationFailureException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/** Represents mapper for ProcessorContentApplicationFailureException exception. */
@Provider
public class ProcessorContentApplicationFailureExceptionMapper
        implements ExceptionMapper<ProcessorContentApplicationFailureException> {
    @Override
    public Response toResponse(ProcessorContentApplicationFailureException e) {
        return Response.status(Response.Status.BAD_REQUEST.getStatusCode())
                .entity(e.getMessage())
                .build();
    }
}