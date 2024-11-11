package com.objectstorage.service.client.content.withdraw;

import com.objectstorage.ApiClient;
import com.objectstorage.api.ContentResourceApi;
import com.objectstorage.exception.ApiServerNotAvailableException;
import com.objectstorage.exception.ApiServerOperationFailureException;
import com.objectstorage.model.ContentRetrievalApplication;
import com.objectstorage.model.ContentRetrievalResult;
import com.objectstorage.model.ContentWithdrawal;
import com.objectstorage.service.client.common.IClient;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.netty.http.client.HttpClient;

/**
 * Represents implementation for v1ContentPost endpoint of ContentResourceApi.
 */
public class WithdrawContentClientService implements IClient<Void, ContentWithdrawal> {
    private final ContentResourceApi contentResourceApi;

    public WithdrawContentClientService(String host) {
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
    public Void process(ContentWithdrawal input) throws ApiServerOperationFailureException {
        try {
            return contentResourceApi.v1ContentWithdrawDelete(input).block();
        } catch (WebClientResponseException e) {
            throw new ApiServerOperationFailureException(e.getResponseBodyAsString());
        } catch (WebClientRequestException e) {
            throw new ApiServerOperationFailureException(new ApiServerNotAvailableException(e.getMessage()).getMessage());
        }
    }
}