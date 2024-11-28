package com.objectstorage.mapping;

import com.objectstorage.exception.ProviderIsNotConfiguredException;
import com.objectstorage.exception.RootIsNotValidException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/** Represents mapper for ProviderIsNotConfiguredException exception. */
@Provider
public class ProviderIsNotConfiguredExceptionMapper
        implements ExceptionMapper<ProviderIsNotConfiguredException> {
    @Override
    public Response toResponse(ProviderIsNotConfiguredException e) {
        return Response.status(Response.Status.BAD_REQUEST.getStatusCode())
                .entity(e.getMessage())
                .build();
    }
}
