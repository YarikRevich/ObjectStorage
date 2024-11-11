package com.objectstorage.mapping;

import com.objectstorage.exception.ProviderIsNotAvailableException;
import com.objectstorage.exception.RepositoryContentApplicationFailureException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Represents mapper for ProviderIsNotAvailableException exception.
 */
@Provider
public class ProviderIsNotAvailableExceptionMapper
        implements ExceptionMapper<ProviderIsNotAvailableException> {
    @Override
    public Response toResponse(ProviderIsNotAvailableException e) {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
                .entity(e.getMessage())
                .build();
    }
}
