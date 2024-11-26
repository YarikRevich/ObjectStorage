package com.objectstorage.mapping;

import com.objectstorage.exception.ProcessorContentRetrievalFailureException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/** Represents mapper for ProcessorContentRetrievalFailureExceptionMapper exception. */
@Provider
public class ProcessorContentRetrievalFailureExceptionMapper
        implements ExceptionMapper<ProcessorContentRetrievalFailureException> {
    @Override
    public Response toResponse(ProcessorContentRetrievalFailureException e) {
        return Response.status(Response.Status.BAD_REQUEST.getStatusCode())
                .entity(e.getMessage())
                .build();
    }
}