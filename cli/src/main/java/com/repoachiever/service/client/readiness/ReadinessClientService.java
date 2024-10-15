package com.objectstorage.service.client.readiness;

import com.objectstorage.ApiClient;
import com.objectstorage.api.HealthResourceApi;
import com.objectstorage.exception.ApiServerOperationFailureException;
import com.objectstorage.exception.ApiServerNotAvailableException;
import com.objectstorage.model.ReadinessCheckResult;
import com.objectstorage.service.client.common.IClient;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.netty.http.client.HttpClient;

/**
 * Represents implementation for v1ReadinessPost endpoint of HealthResourceApi.
 */
public class ReadinessClientService implements IClient<ReadinessCheckResult, Void> {
    private final HealthResourceApi healthResourceApi;

    public ReadinessClientService(String host) {
        ApiClient apiClient = new ApiClient(WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(
                        HttpClient.create().followRedirect(true)))
                .build())
                .setBasePath(host);

        this.healthResourceApi = new HealthResourceApi(apiClient);
    }

    /**
     * @see IClient
     */
    public ReadinessCheckResult process(Void input) throws ApiServerOperationFailureException {
        try {
            return healthResourceApi.v1ReadinessPost(null).block();
        } catch (WebClientResponseException e) {
            throw new ApiServerOperationFailureException(e.getResponseBodyAsString());
        } catch (WebClientRequestException e) {
            throw new ApiServerOperationFailureException(new ApiServerNotAvailableException(e.getMessage()).getMessage());
        }
    }
}