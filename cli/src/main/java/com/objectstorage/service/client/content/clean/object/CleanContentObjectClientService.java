package com.objectstorage.service.client.content.clean.object;

import com.objectstorage.ApiClient;
import com.objectstorage.api.ContentResourceApi;
import com.objectstorage.dto.ContentCleanupRequestDto;
import com.objectstorage.exception.ApiServerNotAvailableException;
import com.objectstorage.exception.ApiServerOperationFailureException;
import com.objectstorage.service.client.common.IClient;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.netty.http.client.HttpClient;

/** Represents implementation for v1ContentObjectCleanDelete endpoint of ContentResourceApi. */
public class CleanContentObjectClientService implements IClient<Void, ContentCleanupRequestDto> {
    private final ContentResourceApi contentResourceApi;

    public CleanContentObjectClientService(String host) {
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
    public Void process(ContentCleanupRequestDto input)
            throws ApiServerOperationFailureException {
        try {
            return contentResourceApi
                    .v1ContentObjectCleanDelete(
                            input.getAuthorization(),
                            input.getContentCleanup())
                    .block();
        } catch (WebClientResponseException e) {
            throw new ApiServerOperationFailureException(e.getResponseBodyAsString());
        } catch (WebClientRequestException e) {
            throw new ApiServerOperationFailureException(new ApiServerNotAvailableException(e.getMessage()).getMessage());
        }
    }
}
