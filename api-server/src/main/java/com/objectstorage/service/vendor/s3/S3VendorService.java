package com.objectstorage.service.vendor.s3;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.services.s3.waiters.AmazonS3Waiters;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.amazonaws.services.securitytoken.model.AWSSecurityTokenServiceException;
import com.amazonaws.services.securitytoken.model.GetCallerIdentityRequest;
import com.amazonaws.waiters.WaiterParameters;
import com.objectstorage.dto.AWSSecretsDto;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * Service used to represent S3 external service provider operations.
 */
@ApplicationScoped
public class S3VendorService {
    /**
     * Composes AWS Credentials Provider used by AWS SDK client.
     *
     * @param secrets given AWS client secrets.
     * @return composed AWS Credentials Provider.
     */
    public AWSCredentialsProvider getCredentialsProvider(AWSSecretsDto secrets) {
        return new AWSStaticCredentialsProvider(
                new BasicAWSCredentials(secrets.getAccessKey(), secrets.getSecretKey()));
    }

    /**
     * Checks if S3 bucket with the given name exists.
     *
     * @param awsCredentialsProvider given providers to be used for client configuration.
     * @param name given name of the S3 bucket.
     * @param region given region to be used for client configuration.
     * @return result of the check.
     */
    public Boolean isS3BucketPresent(
            AWSCredentialsProvider awsCredentialsProvider, String name, String region) {
        return AmazonS3ClientBuilder.standard()
                .withRegion(region)
                .withCredentials(awsCredentialsProvider)
                .build()
                .doesBucketExistV2(name);
    }

    /**
     * Creates S3 bucket with the given name.
     *
     * @param awsCredentialsProvider given providers to be used for client configuration.
     * @param name given name of the S3 bucket.
     * @param region given region to be used for client configuration.
     */
    public void createS3Bucket(
            AWSCredentialsProvider awsCredentialsProvider, String name, String region) {
        AmazonS3 simpleStorage =
                AmazonS3ClientBuilder.standard()
                        .withRegion(region)
                        .withCredentials(awsCredentialsProvider)
                        .build();

        simpleStorage.createBucket(name);

        AmazonS3Waiters simpleStorageWaiter = simpleStorage.waiters();
        simpleStorageWaiter.bucketExists().run(new WaiterParameters<>(new HeadBucketRequest(name)));
    }

    /**
     * Removes S3 bucket with the given name.
     *
     * @param awsCredentialsProvider given providers to be used for client configuration.
     * @param name given name of the S3 bucket.
     * @param region given region to be used for client configuration.
     */
    public void removeS3Bucket(
            AWSCredentialsProvider awsCredentialsProvider, String name, String region) {
        AmazonS3 simpleStorage =
                AmazonS3ClientBuilder.standard()
                        .withRegion(region)
                        .withCredentials(awsCredentialsProvider)
                        .build();

        ObjectListing objects = simpleStorage.listObjects(name);

        do {
            for (S3ObjectSummary summary : objects.getObjectSummaries()) {
                simpleStorage.deleteObject(name, summary.getKey());
            }

            objects = simpleStorage.listNextBatchOfObjects(objects);
        } while (objects.isTruncated());

        simpleStorage.deleteBucket(name);

        AmazonS3Waiters simpleStorageWaiter = simpleStorage.waiters();
        simpleStorageWaiter.bucketNotExists().run(new WaiterParameters<>(new HeadBucketRequest(name)));
    }

    /**
     * Uploads object to the S3 bucket with the given name.
     *
     * @param awsCredentialsProvider given providers to be used for client configuration.
     * @param bucketName given name of the S3 bucket.
     * @param region given region to be used for client configuration.
     * @param fileName given name of the file to be uploaded.
     * @param inputStream given file input stream to be used for object upload.
     */
    public void uploadObjectToS3Bucket(
            AWSCredentialsProvider awsCredentialsProvider,
            String bucketName,
            String region,
            String fileName,
            InputStream inputStream) {
        AmazonS3 simpleStorage =
                AmazonS3ClientBuilder.standard()
                        .withRegion(region)
                        .withCredentials(awsCredentialsProvider)
                        .build();

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("text/plain");

        PutObjectRequest request = new PutObjectRequest(bucketName, fileName, inputStream, metadata);

        simpleStorage.putObject(request);

        AmazonS3Waiters simpleStorageWaiter = simpleStorage.waiters();
        simpleStorageWaiter.objectExists().run(
                new WaiterParameters<>(new GetObjectMetadataRequest(bucketName, fileName)));
    }

    /**
     * Retrieves object from the S3 bucket with the given name.
     *
     * @param awsCredentialsProvider given providers to be used for client configuration.
     * @param bucketName given name of the S3 bucket.
     * @param region given region to be used for client configuration.
     * @param fileName given name of the file to be retrieved.
     * @return retrieved object content.
     */
    public byte[] retrieveObjectFromS3Bucket(
            AWSCredentialsProvider awsCredentialsProvider,
            String bucketName,
            String region,
            String fileName) {
        AmazonS3 simpleStorage =
                AmazonS3ClientBuilder.standard()
                        .withRegion(region)
                        .withCredentials(awsCredentialsProvider)
                        .build();

        S3Object object = simpleStorage.getObject(bucketName, fileName);
        try {
            return IOUtils.toByteArray(object.getObjectContent());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Remove object from the S3 bucket with the given name.
     *
     * @param awsCredentialsProvider given providers to be used for client configuration.
     * @param bucketName given name of the S3 bucket.
     * @param region given region to be used for client configuration.
     * @param fileName given name of the file to be removed.
     */
    public void removeObjectFromS3Bucket(
            AWSCredentialsProvider awsCredentialsProvider,
            String bucketName,
            String region,
            String fileName) {
        AmazonS3 simpleStorage =
                AmazonS3ClientBuilder.standard()
                        .withRegion(region)
                        .withCredentials(awsCredentialsProvider)
                        .build();

        simpleStorage.deleteObject(bucketName, fileName);

        AmazonS3Waiters simpleStorageWaiter = simpleStorage.waiters();
        simpleStorageWaiter.objectNotExists().run(
                new WaiterParameters<>(new GetObjectMetadataRequest(bucketName, fileName)));
    }

    /**
     * Checks if the selected credentials are valid.
     *
     * @param awsCredentialsProvider given providers to be used for client configuration.
     * @param region given region to be used for client configuration.
     * @return result of credentials validation.
     */
    public Boolean isCallerValid(AWSCredentialsProvider awsCredentialsProvider, String region) {
        try {
            return !Objects.isNull(
                    AWSSecurityTokenServiceClientBuilder.standard()
                            .withRegion(region)
                            .withCredentials(awsCredentialsProvider)
                            .build()
                            .getCallerIdentity(new GetCallerIdentityRequest())
                            .getArn());
        } catch (AWSSecurityTokenServiceException e) {
            return false;
        }
    }
}
