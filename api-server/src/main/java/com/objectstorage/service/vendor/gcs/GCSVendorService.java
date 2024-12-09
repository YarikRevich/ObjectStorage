package com.objectstorage.service.vendor.gcs;

import com.google.api.gax.paging.Page;
import com.google.auth.Credentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.auth.oauth2.UserCredentials;
import com.google.cloud.WriteChannel;
import com.google.cloud.resourcemanager.ResourceManager;
import com.google.cloud.resourcemanager.ResourceManagerException;
import com.google.cloud.resourcemanager.ResourceManagerOptions;
import com.google.cloud.resourcemanager.Project;
import com.google.cloud.storage.*;
import com.objectstorage.dto.VendorObjectListingDto;
import com.objectstorage.exception.GCPCredentialsInitializationFailureException;
import com.objectstorage.exception.GCSBucketObjectUploadFailureException;
import com.objectstorage.exception.VendorOperationFailureException;
import jakarta.enterprise.context.ApplicationScoped;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Objects;
import java.util.stream.StreamSupport;

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
            throw new GCPCredentialsInitializationFailureException(e.getMessage());
        } catch (ClassCastException e) {
            throw new GCPCredentialsInitializationFailureException();
        }
    }

    /**
     * Checks if GCS bucket with the given name exists.
     *
     * @param name given name of the GCS bucket.
     * @param credentials given credentials to be used for client configuration.
     * @return result of the check.
     * @throws VendorOperationFailureException if vendor operation fails.
     */
    public Boolean isGCSBucketPresent(
            Credentials credentials,
            String name) throws VendorOperationFailureException {
        Storage storage = StorageOptions.newBuilder()
            .setCredentials(credentials)
            .build()
            .getService();

        Bucket bucket;

        try {
            bucket = storage.get(name);
        } catch (StorageException e) {
            throw new VendorOperationFailureException(e.getMessage());
        }

        try {
            return Objects.nonNull(bucket) && bucket.exists();
        } catch (StorageException e) {
            throw new VendorOperationFailureException(e.getMessage());
        }
    }

    /**
     * Creates GCS bucket with the given name.
     *
     * @param credentials given credentials to be used for client configuration.
     * @param name given name of the GCS bucket.
     * @throws VendorOperationFailureException if vendor operation fails.
     */
    public void createGCSBucket(
            Credentials credentials,
            String name) throws VendorOperationFailureException {
        Storage storage = StorageOptions.newBuilder()
                .setCredentials(credentials)
                .build()
                .getService();

        try {
            storage.create(BucketInfo.newBuilder(name).build());
        } catch (StorageException e) {
            throw new VendorOperationFailureException(e.getMessage());
        }
    }

    /**
     * Removes GCS bucket with the given name.
     *
     * @param credentials given credentials to be used for client configuration.
     * @param name given name of the GCS bucket.
     * @throws VendorOperationFailureException if vendor operation fails.
     */
    public void removeGCSBucket(
            Credentials credentials,
            String name) throws VendorOperationFailureException {
        Storage storage = StorageOptions.newBuilder()
                .setCredentials(credentials)
                .build()
                .getService();

        try {
            storage.delete(name);
        } catch (StorageException e) {
            throw new VendorOperationFailureException(e.getMessage());
        }
    }

    /**
     * Uploads object to the GCS bucket with the given name.
     *
     * @param credentials given credentials to be used for client configuration.
     * @param bucketName given name of the GCS bucket.
     * @param fileName given name of the file to be uploaded.
     * @param inputStream given file input stream to be used for object upload.
     * @throws GCSBucketObjectUploadFailureException if GCS bucket object upload fails.
     */
    public void uploadObjectToGCSBucket(
            Credentials credentials,
            String bucketName,
            String fileName,
            InputStream inputStream) throws GCSBucketObjectUploadFailureException {
        Storage storage = StorageOptions.newBuilder()
                .setCredentials(credentials)
                .build()
                .getService();

        try (WriteChannel writer = storage.writer(
                BlobInfo.newBuilder(
                        BlobId.of(bucketName, fileName)).build())) {
            writer.write(ByteBuffer.wrap(inputStream.readAllBytes()));
        } catch (IOException e) {
            throw new GCSBucketObjectUploadFailureException(e.getMessage());
        }
    }

    /**
     * Checks if object exists in the GCS bucket with the given name.
     *
     * @param credentials given credentials to be used for client configuration.
     * @param bucketName given name of the GCS bucket.
     * @param fileName given name of the file to be retrieved.
     * @return result of the check.
     * @throws VendorOperationFailureException if vendor operation fails.
     */
    public Boolean isObjectPresentInBucket(
            Credentials credentials,
            String bucketName,
            String fileName) throws VendorOperationFailureException {
        Storage storage = StorageOptions.newBuilder()
                .setCredentials(credentials)
                .build()
                .getService();

        Blob blob;

        try {
            blob = storage.get(BlobId.of(bucketName, fileName));
        } catch (StorageException e) {
            throw new VendorOperationFailureException(e.getMessage());
        }

        try {
            return Objects.nonNull(blob) && blob.exists();
        } catch (StorageException e) {
            throw new VendorOperationFailureException(e.getMessage());
        }
    }

    /**
     * Retrieves object from the GCS bucket with the given name.
     *
     * @param credentials given credentials to be used for client configuration.
     * @param bucketName given name of the GCS bucket.
     * @param fileName given name of the file to be retrieved.
     * @return retrieved object content.
     * @throws VendorOperationFailureException if vendor operation fails.
     */
    public byte[] retrieveObjectFromGCSBucket(
            Credentials credentials,
            String bucketName,
            String fileName) throws VendorOperationFailureException {
        Storage storage = StorageOptions.newBuilder()
                .setCredentials(credentials)
                .build()
                .getService();

        Blob blob;

        try {
            blob = storage.get(BlobId.of(bucketName, fileName));
        } catch (StorageException e) {
            throw new VendorOperationFailureException(e.getMessage());
        }

        try {
            return blob.getContent();
        } catch (StorageException e) {
            throw new VendorOperationFailureException(e.getMessage());
        }
    }

    /**
     * Lists objects from the GCS bucket with the given name.
     *
     * @param credentials given credentials to be used for client configuration.
     * @param bucketName given name of the GCS bucket.
     * @return listed objects.
     * @throws VendorOperationFailureException if vendor operation fails.
     */
    public List<VendorObjectListingDto> listObjectsFromGCSBucket(
            Credentials credentials,
            String bucketName) throws VendorOperationFailureException {
        Storage storage = StorageOptions.newBuilder()
                .setCredentials(credentials)
                .build()
                .getService();

        try {
            Page<Blob> blobs = storage.list(bucketName);

            return StreamSupport.stream(blobs.iterateAll().spliterator(), false)
                    .map(element -> VendorObjectListingDto.of(
                            element.getBlobId().getName(),
                            element.getUpdateTimeOffsetDateTime().toEpochSecond()))
                    .toList();
        } catch (StorageException e) {
            throw new VendorOperationFailureException(e.getMessage());
        }
    }

    /**
     * Removes object from the GCS bucket with the given name.
     *
     * @param credentials given credentials to be used for client configuration.
     * @param bucketName given name of the GCS bucket.
     * @param fileName given name of the file to be removed.
     * @throws VendorOperationFailureException if vendor operation fails.
     */
    public void removeObjectFromGCSBucket(
            Credentials credentials,
            String bucketName,
            String fileName) throws VendorOperationFailureException {
        Storage storage = StorageOptions.newBuilder()
                .setCredentials(credentials)
                .build()
                .getService();

        try {
            storage.delete(BlobId.of(bucketName, fileName));
        } catch (StorageException e) {
            throw new VendorOperationFailureException(e.getMessage());
        }
    }

    /**
     * Removes all objects from the GCS bucket with the given name.
     *
     * @param credentials given credentials to be used for client configuration.
     * @param bucketName given name of the GCS bucket.
     * @throws VendorOperationFailureException if vendor operation fails.
     */
    public void removeAllObjectsFromGCSBucket(
            Credentials credentials,
            String bucketName) throws VendorOperationFailureException {
        Storage storage = StorageOptions.newBuilder()
                .setCredentials(credentials)
                .build()
                .getService();

        try {
            Page<Blob> blobs = storage.list(bucketName);

            for (Blob blob : blobs.iterateAll()) {
                blob.delete(Blob.BlobSourceOption.generationMatch());
            }
        } catch (StorageException e) {
            throw new VendorOperationFailureException(e.getMessage());
        }
    }

    /**
     * Checks if the selected credentials are valid.
     *
     * @param credentials given credentials to be used for client configuration.
     * @return result of credentials validation.
     */
    public Boolean isCallerValid(Credentials credentials) {
        ResourceManager resourceManager = ResourceManagerOptions.newBuilder()
                .setCredentials(credentials)
                .build()
                .getService();

        try {
            for (Project project : resourceManager.list().iterateAll()) {
                return true;
            }
        } catch (ResourceManagerException e) {
            return false;
        }

        return false;
    }
}
