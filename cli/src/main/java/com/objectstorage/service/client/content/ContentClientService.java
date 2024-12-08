package com.objectstorage.service.client.content;

import com.objectstorage.ApiClient;
import com.objectstorage.api.ContentResourceApi;
import com.objectstorage.exception.ApiServerOperationFailureException;
import com.objectstorage.exception.ApiServerNotAvailableException;
import com.objectstorage.model.ContentRetrievalResult;
import com.objectstorage.service.client.common.helper.ClientConfigurationHelper;
import com.objectstorage.service.client.common.IClient;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.netty.http.client.HttpClient;

/**
 * Represents implementation for v1ContentGet endpoint of ContentResourceApi.
 */
public class ContentClientService implements IClient<ContentRetrievalResult, String> {
    private final ContentResourceApi contentResourceApi;

    public ContentClientService(String host) {
        ApiClient apiClient = new ApiClient(WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(
                        HttpClient.create().followRedirect(true)))
                .build())
                .setBasePath(host);

        this.contentResourceApi = new ContentResourceApi(apiClient);
    }

    /**
     * @see IClient
     */
    @Override
    public ContentRetrievalResult process(String authorization) throws ApiServerOperationFailureException {
        try {
            return contentResourceApi
                    .v1ContentGet(
                            ClientConfigurationHelper.getWrappedToken(authorization))
                    .block();
        } catch (WebClientResponseException e) {
            throw new ApiServerOperationFailureException(e.getResponseBodyAsString());
        } catch (WebClientRequestException e) {
            throw new ApiServerOperationFailureException(new ApiServerNotAvailableException(e.getMessage()).getMessage());
        }
    }
}