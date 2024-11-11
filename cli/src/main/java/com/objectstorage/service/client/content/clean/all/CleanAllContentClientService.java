package com.objectstorage.service.client.content.clean.all;

import com.objectstorage.ApiClient;
import com.objectstorage.api.ContentResourceApi;
import com.objectstorage.exception.ApiServerNotAvailableException;
import com.objectstorage.exception.ApiServerOperationFailureException;
import com.objectstorage.model.ContentCleanup;
import com.objectstorage.model.ContentCleanupAll;
import com.objectstorage.service.client.common.IClient;
import com.objectstorage.service.config.ConfigService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.netty.http.client.HttpClient;

/** Represents implementation for v1ContentCleanAllDelete endpoint of ContentResourceApi. */
public class CleanAllContentClientService implements IClient<Void, ContentCleanupAll> {
    private final ContentResourceApi contentResourceApi;

    public CleanAllContentClientService(String host) {
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
    public Void process(ContentCleanupAll input)
            throws ApiServerOperationFailureException {
        try {
            return contentResourceApi
                    .v1ContentCleanAllDelete(input)
                    .block();
        } catch (WebClientResponseException e) {
            throw new ApiServerOperationFailureException(e.getResponseBodyAsString());
        } catch (WebClientRequestException e) {
            throw new ApiServerOperationFailureException(new ApiServerNotAvailableException(e.getMessage()).getMessage());
        }
    }
}
