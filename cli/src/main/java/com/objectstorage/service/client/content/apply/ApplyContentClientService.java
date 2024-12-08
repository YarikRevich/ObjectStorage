package com.objectstorage.service.client.content.apply;

import com.objectstorage.ApiClient;
import com.objectstorage.api.ContentResourceApi;
import com.objectstorage.dto.ContentApplicationRequestDto;
import com.objectstorage.exception.ApiServerOperationFailureException;
import com.objectstorage.exception.ApiServerNotAvailableException;
import com.objectstorage.service.client.common.helper.ClientConfigurationHelper;
import com.objectstorage.service.client.common.IClient;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import reactor.netty.http.client.HttpClient;

/**
 * Represents implementation for v1ContentApplyPost endpoint of ContentResourceApi.
 */
public class ApplyContentClientService implements IClient<Void, ContentApplicationRequestDto> {
    private final ContentResourceApi contentResourceApi;

    public ApplyContentClientService(String host) {
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
    public Void process(ContentApplicationRequestDto input)
            throws ApiServerOperationFailureException {
        try {
            return contentResourceApi
                    .v1ContentApplyPost(
                            ClientConfigurationHelper.getWrappedToken(input.getAuthorization()),
                            input.getContentApplication())
                    .block();
        } catch (WebClientResponseException e) {
            throw new ApiServerOperationFailureException(e.getResponseBodyAsString());
        } catch (WebClientRequestException e) {
            throw new ApiServerOperationFailureException(new ApiServerNotAvailableException(e.getMessage()).getMessage());
        }
    }
}
