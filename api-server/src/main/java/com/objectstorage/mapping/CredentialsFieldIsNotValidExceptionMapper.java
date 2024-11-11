package com.objectstorage.mapping;

import com.objectstorage.exception.CredentialsFieldIsNotValidException;
import com.objectstorage.exception.WorkspaceUnitDirectoryNotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/** Represents mapper for CredentialsFieldIsNotValidException exception. */
@Provider
public class CredentialsFieldIsNotValidExceptionMapper
        implements ExceptionMapper<CredentialsFieldIsNotValidException> {
    @Override
    public Response toResponse(CredentialsFieldIsNotValidException e) {
        return Response.status(Response.Status.BAD_REQUEST.getStatusCode())
                .entity(e.getMessage())
                .build();
    }
}
