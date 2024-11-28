package com.objectstorage.mapping;

import com.objectstorage.exception.RootIsNotValidException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/** Represents mapper for RootIsNotValidException exception. */
@Provider
public class RootIsNotValidExceptionMapper
        implements ExceptionMapper<RootIsNotValidException> {
    @Override
    public Response toResponse(RootIsNotValidException e) {
        return Response.status(Response.Status.BAD_REQUEST.getStatusCode())
                .entity(e.getMessage())
                .build();
    }
}
