package com.objectstorage.service.vendor.gcs;

import com.google.auth.Credentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.WriteChannel;
import com.google.cloud.storage.*;
import com.objectstorage.exception.GCPCredentialsInitializationFailureException;
import jakarta.enterprise.context.ApplicationScoped;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Service used to represent GCS external service provider operations.
 */
@ApplicationScoped
public class GCSVendorService {
    /**
     * Composes GCP credentials used by GCP SDK client.
     *
     * @param secrets given GCP client secrets.
     * @return composed GCP credentials.
     * @throws GCPCredentialsInitializationFailureException if credentials initialization fails.
     */
    public Credentials getCredentials(String secrets) throws GCPCredentialsInitializationFailureException {
        try {
            return ServiceAccountCredentials.fromStream(new ByteArrayInputStream(secrets.getBytes()));
        } catch (IOException e) {
            throw new GCPCredentialsInitializationFailureException(e);
        }
    }

    /**
     * Checks if GCS bucket with the given name exists.
     *
     * @param name given name of the GCS bucket.
     * @param credentials given credentials to be used for client configuration.
     * @return result of the check.
     */
    public Boolean isGCSBucketPresent(
            Credentials credentials,
            String name) {
        Storage storage = StorageOptions.newBuilder()
            .setCredentials(credentials)
            .build()
            .getService();

        return storage
                .get(name)
                .exists();
    }

    /**
     * Creates GCS bucket with the given name.
     *
     * @param credentials given credentials to be used for client configuration.
     * @param name given name of the GCS bucket.
     */
    public void createGCSBucket(
            Credentials credentials,
            String name) {
        Storage storage = StorageOptions.newBuilder()
                .setCredentials(credentials)
                .build()
                .getService();

        storage.create(BucketInfo.newBuilder(name).build());
    }

    /**
     * Removes GCS bucket with the given name.
     *
     * @param credentials given credentials to be used for client configuration.
     * @param name given name of the GCS bucket.
     */
    public void removeGCSBucket(
            Credentials credentials,
            String name) {
        Storage storage = StorageOptions.newBuilder()
                .setCredentials(credentials)
                .build()
                .getService();

        storage.delete(name);
    }

    /**
     * Uploads object to the GCS bucket with the given name.
     *
     * @param credentials given credentials to be used for client configuration.
     * @param bucketName given name of the GCS bucket.
     * @param fileName given name of the file to be uploaded.
     * @param inputStream given file input stream to be used for object upload.
     */
    public void uploadObjectToGCSBucket(
            Credentials credentials,
            String bucketName,
            String fileName,
            InputStream inputStream) {
        Storage storage = StorageOptions.newBuilder()
                .setCredentials(credentials)
                .build()
                .getService();

        try (WriteChannel writer = storage.writer(
                BlobInfo.newBuilder(
                        BlobId.of(bucketName, fileName)).build())) {
            writer.write(ByteBuffer.wrap(inputStream.readAllBytes()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Retrieves object from the GCS bucket with the given name.
     *
     * @param credentials given credentials to be used for client configuration.
     * @param bucketName given name of the GCS bucket.
     * @param fileName given name of the file to be retrieved.
     * @return retrieved object content.
     */
    public byte[] retrieveObjectFromGCSBucket(
            Credentials credentials,
            String bucketName,
            String fileName) {
        Storage storage = StorageOptions.newBuilder()
                .setCredentials(credentials)
                .build()
                .getService();

        Blob blob = storage.get(BlobId.of(bucketName, fileName));

        return blob.getContent();
    }

    /**
     * Remove object from the GCS bucket with the given name.
     *
     * @param credentials given credentials to be used for client configuration.
     * @param bucketName given name of the GCS bucket.
     * @param fileName given name of the file to be removed.
     */
    public void removeObjectFromGCSBucket(
            Credentials credentials,
            String bucketName,
            String fileName) {
        Storage storage = StorageOptions.newBuilder()
                .setCredentials(credentials)
                .build()
                .getService();

        storage.delete(BlobId.of(bucketName, fileName));
    }
}
