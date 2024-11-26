package com.objectstorage.mapping;

import com.objectstorage.exception.SecretsAreEmptyException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/** Represents mapper for SecretsAreEmptyException exception. */
@Provider
public class SecretsAreEmptyExceptionMapper
        implements ExceptionMapper<SecretsAreEmptyException> {
    @Override
    public Response toResponse(SecretsAreEmptyException e) {
        return Response.status(Response.Status.BAD_REQUEST.getStatusCode())
                .entity(e.getMessage())
                .build();
    }
}