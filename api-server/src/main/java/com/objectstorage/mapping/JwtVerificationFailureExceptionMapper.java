package com.objectstorage.mapping;

import com.objectstorage.exception.JwtVerificationFailureException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/** Represents mapper for ExporterFieldIsNotValidException exception. */
@Provider
public class JwtVerificationFailureExceptionMapper
        implements ExceptionMapper<JwtVerificationFailureException> {
    @Override
    public Response toResponse(JwtVerificationFailureException e) {
        return Response.status(Response.Status.UNAUTHORIZED.getStatusCode())
                .entity(e.getMessage())
                .build();
    }
}