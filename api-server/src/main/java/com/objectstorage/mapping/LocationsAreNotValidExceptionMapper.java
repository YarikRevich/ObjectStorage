package com.objectstorage.mapping;

import com.objectstorage.exception.ProvidersAreNotValidException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/** Represents mapper for LocationsAreNotValidException exception. */
@Provider
public class LocationsAreNotValidExceptionMapper
        implements ExceptionMapper<ProvidersAreNotValidException> {
    @Override
    public Response toResponse(ProvidersAreNotValidException e) {
        return Response.status(Response.Status.BAD_REQUEST.getStatusCode())
                .entity(e.getMessage())
                .build();
    }
}
