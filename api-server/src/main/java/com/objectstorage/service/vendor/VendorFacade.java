package com.objectstorage.service.vendor;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.google.auth.Credentials;
import com.objectstorage.exception.GCPCredentialsInitializationFailureException;
import com.objectstorage.exception.SecretsConversionException;
import com.objectstorage.model.CredentialsFieldsExternal;
import com.objectstorage.model.Provider;
import com.objectstorage.service.vendor.gcs.GCSVendorService;
import com.objectstorage.service.vendor.s3.S3VendorService;
import com.objectstorage.converter.SecretsConverter;
import com.objectstorage.dto.AWSSecretsDto;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.io.InputStream;

/**
 * Provides high-level access to cloud vendor operations.
 */
@ApplicationScoped
public class VendorFacade {
    @Inject
    S3VendorService s3VendorService;

    @Inject
    GCSVendorService gcsVendorService;

    /**
     * Checks if bucket with the given name exists within the given service provider.
     *
     * @param provider given external provider name.
     * @param credentialsFieldExternal given external credentials.
     * @param name given name of the bucket.
     * @return result of the check.
     * @throws SecretsConversionException if secrets conversion fails or secrets are invalid.
     */
    public Boolean isBucketPresent(
            Provider provider,
            CredentialsFieldsExternal credentialsFieldExternal,
            String name) throws SecretsConversionException {
        return switch (provider) {
            case S3 -> {
                AWSSecretsDto secrets =
                        SecretsConverter.convert(AWSSecretsDto.class, credentialsFieldExternal.getFile());

                AWSCredentialsProvider awsCredentialsProvider =
                        s3VendorService.getCredentialsProvider(secrets);

                yield s3VendorService.isS3BucketPresent(
                        awsCredentialsProvider, name, credentialsFieldExternal.getRegion());
            }
            case GCS -> {
                Credentials credentials;

                try {
                    credentials = gcsVendorService.getCredentials(credentialsFieldExternal.getFile());
                } catch (GCPCredentialsInitializationFailureException e) {
                    throw new SecretsConversionException(e);
                }

                yield gcsVendorService.isGCSBucketPresent(credentials, name);
            }
        };
    }

    /**
     * Creates bucket with the given name.
     *
     * @param provider given external provider name.
     * @param credentialsFieldExternal given external credentials.
     * @param name given name of the bucket.
     * @throws SecretsConversionException if secrets conversion fails or secrets are invalid.
     */
    public void createBucket(
            Provider provider,
            CredentialsFieldsExternal credentialsFieldExternal,
            String name) throws SecretsConversionException {
        switch (provider) {
            case S3 -> {
                AWSSecretsDto secrets =
                        SecretsConverter.convert(AWSSecretsDto.class, credentialsFieldExternal.getFile());

                AWSCredentialsProvider awsCredentialsProvider =
                        s3VendorService.getCredentialsProvider(secrets);

                s3VendorService.createS3Bucket(
                        awsCredentialsProvider, name, credentialsFieldExternal.getRegion());
            }
            case GCS -> {
                Credentials credentials;

                try {
                    credentials = gcsVendorService.getCredentials(credentialsFieldExternal.getFile());
                } catch (GCPCredentialsInitializationFailureException e) {
                    throw new SecretsConversionException(e);
                }

                gcsVendorService.createGCSBucket(credentials, name);
            }
        }
    }

    /**
     * Removes bucket with the given name.
     *
     * @param provider given external provider name.
     * @param credentialsFieldExternal given external credentials.
     * @param name given name of the bucket.
     * @throws SecretsConversionException if secrets conversion fails or secrets are invalid.
     */
    public void removeBucket(
            Provider provider,
            CredentialsFieldsExternal credentialsFieldExternal,
            String name) throws SecretsConversionException {
        switch (provider) {
            case S3 -> {
                AWSSecretsDto secrets =
                        SecretsConverter.convert(AWSSecretsDto.class, credentialsFieldExternal.getFile());

                AWSCredentialsProvider awsCredentialsProvider =
                        s3VendorService.getCredentialsProvider(secrets);

                s3VendorService.removeS3Bucket(
                        awsCredentialsProvider, name, credentialsFieldExternal.getRegion());
            }
            case GCS -> {
                Credentials credentials;

                try {
                    credentials = gcsVendorService.getCredentials(credentialsFieldExternal.getFile());
                } catch (GCPCredentialsInitializationFailureException e) {
                    throw new SecretsConversionException(e);
                }

                gcsVendorService.removeGCSBucket(credentials, name);
            }
        }
    }

    /**
     * Uploads object to the bucket with the given name.
     *
     * @param provider given external provider name.
     * @param credentialsFieldExternal given external credentials.
     * @param bucketName given name of the bucket.
     * @param fileName given name of the file to be uploaded.
     * @param inputStream given file input stream to be used for object upload.
     * @throws SecretsConversionException if secrets conversion fails or secrets are invalid.
     */
    public void uploadObjectToBucket(
            Provider provider,
            CredentialsFieldsExternal credentialsFieldExternal,
            String bucketName,
            String fileName,
            InputStream inputStream) throws SecretsConversionException {
        switch (provider) {
            case S3 -> {
                AWSSecretsDto secrets =
                        SecretsConverter.convert(AWSSecretsDto.class, credentialsFieldExternal.getFile());

                AWSCredentialsProvider awsCredentialsProvider =
                        s3VendorService.getCredentialsProvider(secrets);

                s3VendorService.uploadObjectToS3Bucket(
                        awsCredentialsProvider,
                        bucketName,
                        credentialsFieldExternal.getRegion(),
                        fileName,
                        inputStream);
            }
            case GCS -> {
                Credentials credentials;

                try {
                    credentials = gcsVendorService.getCredentials(credentialsFieldExternal.getFile());
                } catch (GCPCredentialsInitializationFailureException e) {
                    throw new SecretsConversionException(e);
                }

                gcsVendorService.uploadObjectToGCSBucket(credentials, bucketName, fileName, inputStream);
            }
        }
    }

    /**
     * Retrieves object from the bucket with the given name.
     *
     * @param provider given external provider name.
     * @param credentialsFieldExternal given external credentials.
     * @param bucketName given name of the bucket.
     * @param fileName given name of the file to be uploaded.
     * @throws SecretsConversionException if secrets conversion fails or secrets are invalid.
     */
    public byte[] retrieveObjectFromBucket(
            Provider provider,
            CredentialsFieldsExternal credentialsFieldExternal,
            String bucketName,
            String fileName) throws SecretsConversionException {
        return switch (provider) {
            case S3 -> {
                AWSSecretsDto secrets =
                        SecretsConverter.convert(AWSSecretsDto.class, credentialsFieldExternal.getFile());

                AWSCredentialsProvider awsCredentialsProvider =
                        s3VendorService.getCredentialsProvider(secrets);

                yield s3VendorService.retrieveObjectFromS3Bucket(
                        awsCredentialsProvider,
                        bucketName,
                        credentialsFieldExternal.getRegion(),
                        fileName);
            }
            case GCS -> {
                Credentials credentials;

                try {
                    credentials = gcsVendorService.getCredentials(credentialsFieldExternal.getFile());
                } catch (GCPCredentialsInitializationFailureException e) {
                    throw new SecretsConversionException(e);
                }

                yield gcsVendorService.retrieveObjectFromGCSBucket(credentials, bucketName, fileName);
            }
        };
    }

    /**
     * Converts given raw credentials according to the selected provider, according to the given provider name.
     *
     * @param provider                 given external provider name.
     * @param credentialsFieldExternal given external credentials.
     * @return result of the check.
     * @throws SecretsConversionException if secrets conversion fails or secrets are invalid.
     */
    public Boolean areCredentialsValid(
            Provider provider, CredentialsFieldsExternal credentialsFieldExternal) throws SecretsConversionException {
        return switch (provider) {
            case S3 -> {
                AWSSecretsDto secrets =
                        SecretsConverter.convert(AWSSecretsDto.class, credentialsFieldExternal.getFile());

                AWSCredentialsProvider awsCredentialsProvider =
                        s3VendorService.getCredentialsProvider(secrets);

                yield s3VendorService.isCallerValid(
                        awsCredentialsProvider, credentialsFieldExternal.getRegion());
            }
            case GCS -> {
                Credentials credentials;

                try {
                    credentials = gcsVendorService.getCredentials(credentialsFieldExternal.getFile());
                } catch (GCPCredentialsInitializationFailureException e) {
                    throw new SecretsConversionException(e);
                }

                yield gcsVendorService.isCallerValid(credentials);
            }
        };
    }
}
