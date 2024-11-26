package com.objectstorage.mapping;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import javax.net.ssl.SSLException;

/**
 * Represents mapper for SSLExceptionMapper exception.
 */
@Provider
public class SSLExceptionMapper implements ExceptionMapper<SSLException> {
    @Override
    public Response toResponse(SSLException e) {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
                .entity(e.getMessage())
                .build();
    }
}