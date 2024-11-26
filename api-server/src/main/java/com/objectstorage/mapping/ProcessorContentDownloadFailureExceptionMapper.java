package com.objectstorage.mapping;

import com.objectstorage.exception.ProcessorContentDownloadFailureException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/** Represents mapper for ProcessorContentDownloadFailureException exception. */
@Provider
public class ProcessorContentDownloadFailureExceptionMapper
        implements ExceptionMapper<ProcessorContentDownloadFailureException> {
    @Override
    public Response toResponse(ProcessorContentDownloadFailureException e) {
        return Response.status(Response.Status.BAD_REQUEST.getStatusCode())
                .entity(e.getMessage())
                .build();
    }
}