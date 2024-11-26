package com.objectstorage.mapping;

import com.objectstorage.exception.ProcessorContentUploadFailureException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/** Represents mapper for ProcessorContentUploadFailureException exception. */
@Provider
public class ProcessorContentUploadFailureExceptionMapper
        implements ExceptionMapper<ProcessorContentUploadFailureException> {
    @Override
    public Response toResponse(ProcessorContentUploadFailureException e) {
        return Response.status(Response.Status.BAD_REQUEST.getStatusCode())
                .entity(e.getMessage())
                .build();
    }
}