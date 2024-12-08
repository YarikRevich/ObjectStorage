package com.objectstorage.service.client.validation;

import com.objectstorage.ApiClient;
import com.objectstorage.api.ValidationResourceApi;
import com.objectstorage.exception.ApiServerNotAvailableException;
import com.objectstorage.exception.ApiServerOperationFailureException;
import com.objectstorage.model.ValidationSecretsApplication;
import com.objectstorage.model.ValidationSecretsApplicationResult;
import com.objectstorage.service.client.common.IClient;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.netty.http.client.HttpClient;

/**
 * Represents implementation for v1SecretsAcquirePost endpoint of ContentResourceApi.
 */
public class AcquireSecretsClientService implements IClient<
        ValidationSecretsApplicationResult, ValidationSecretsApplication> {
    private final ValidationResourceApi validationResourceApi;

    public AcquireSecretsClientService(String host) {
        ApiClient apiClient = new ApiClient(WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(
                        HttpClient.create().followRedirect(true)))
                .build())
                .setBasePath(host);

        this.validationResourceApi = new ValidationResourceApi(apiClient);
    }

    /**
     * @see IClient
     */
    @Override
    public ValidationSecretsApplicationResult process(ValidationSecretsApplication validationSecretsApplication)
            throws ApiServerOperationFailureException {
        try {
            return validationResourceApi.v1SecretsAcquirePost(validationSecretsApplication).block();
        } catch (WebClientResponseException e) {
            throw new ApiServerOperationFailureException(e.getResponseBodyAsString());
        } catch (WebClientRequestException e) {
            throw new ApiServerOperationFailureException(new ApiServerNotAvailableException(e.getMessage()).getMessage());
        }
    }
}