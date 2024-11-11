package com.objectstorage.service.client.info.topology;

import com.objectstorage.ApiClient;
import com.objectstorage.api.InfoResourceApi;
import com.objectstorage.exception.ApiServerNotAvailableException;
import com.objectstorage.exception.ApiServerOperationFailureException;
import com.objectstorage.model.TopologyInfoApplication;
import com.objectstorage.model.TopologyInfoUnit;
import com.objectstorage.service.client.common.IClient;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.netty.http.client.HttpClient;

import java.util.List;

/** Represents implementation for v1InfoTopologyPost endpoint of InfoResourceApi. */
public class TopologyInfoClientService implements IClient<List<TopologyInfoUnit>, TopologyInfoApplication> {
    private final InfoResourceApi infoResourceApi;

    public TopologyInfoClientService(String host) {
        ApiClient apiClient = new ApiClient(WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(
                        HttpClient.create().followRedirect(true)))
                .build())
                .setBasePath(host);

        this.infoResourceApi = new InfoResourceApi(apiClient);
    }

    /**
     * @see IClient
     */
    public List<TopologyInfoUnit> process(TopologyInfoApplication input) throws ApiServerOperationFailureException {
        try {
            return infoResourceApi.v1InfoTopologyPost(input).collectList().block();
        } catch (WebClientResponseException e) {
            throw new ApiServerOperationFailureException(e.getResponseBodyAsString());
        } catch (WebClientRequestException e) {
            throw new ApiServerOperationFailureException(new ApiServerNotAvailableException(e.getMessage()).getMessage());
        }
    }
}