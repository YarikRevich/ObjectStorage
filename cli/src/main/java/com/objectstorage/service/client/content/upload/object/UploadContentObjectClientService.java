package com.objectstorage.service.client.content.upload.object;

import com.objectstorage.ApiClient;
import com.objectstorage.api.ContentResourceApi;
import com.objectstorage.dto.ContentUploadObjectRequestDto;
import com.objectstorage.exception.ApiServerNotAvailableException;
import com.objectstorage.exception.ApiServerOperationFailureException;
import com.objectstorage.service.client.common.IClient;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.netty.http.client.HttpClient;

/**
 * Represents implementation for v1ContentObjectUploadPost endpoint of ContentResourceApi.
 */
public class UploadContentObjectClientService implements IClient<Void, ContentUploadObjectRequestDto> {
    private final ContentResourceApi contentResourceApi;

    public UploadContentObjectClientService(String host) {
        ApiClient apiClient = new ApiClient(WebClient.builder()
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(codecs -> codecs.defaultCodecs()
                                .maxInMemorySize(-1))
                        .build())
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
    public Void process(ContentUploadObjectRequestDto input) throws ApiServerOperationFailureException {
        try {
            return contentResourceApi
                    .v1ContentObjectUploadPost(
                            input.getAuthorization(),
                            input.getLocation(),
                            input.getFile())
                    .block();
        } catch (WebClientResponseException e) {
            throw new ApiServerOperationFailureException(e.getResponseBodyAsString());
        } catch (WebClientRequestException e) {
            throw new ApiServerOperationFailureException(new ApiServerNotAvailableException(e.getMessage()).getMessage());
        }
    }
}