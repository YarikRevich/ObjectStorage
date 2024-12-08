package com.objectstorage.service.client.content.download.backup;

import com.objectstorage.ApiClient;
import com.objectstorage.api.ContentResourceApi;
import com.objectstorage.dto.ContentDownloadBackupRequestDto;
import com.objectstorage.exception.ApiServerNotAvailableException;
import com.objectstorage.exception.ApiServerOperationFailureException;
import com.objectstorage.service.client.common.helper.ClientConfigurationHelper;
import com.objectstorage.service.client.common.IClient;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.netty.http.client.HttpClient;

/**
 * Represents implementation for v1ContentBackupDownloadPost endpoint of ContentResourceApi.
 */
public class DownloadContentBackupClientService implements IClient<byte[], ContentDownloadBackupRequestDto> {
    private final ContentResourceApi contentResourceApi;

    public DownloadContentBackupClientService(String host) {
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
    public byte[] process(ContentDownloadBackupRequestDto input) throws ApiServerOperationFailureException {
        try {
            return contentResourceApi
                    .v1ContentBackupDownloadPost(
                            ClientConfigurationHelper.getWrappedToken(input.getAuthorization()),
                            input.getContentBackupDownload())
                    .block();
        } catch (WebClientResponseException e) {
            throw new ApiServerOperationFailureException(e.getResponseBodyAsString());
        } catch (WebClientRequestException e) {
            throw new ApiServerOperationFailureException(new ApiServerNotAvailableException(e.getMessage()).getMessage());
        }
    }
}