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
import com.objectstorage.dto.VendorObjectListingDto;
import com.objectstorage.exception.S3BucketObjectRetrievalFailureException;
import com.objectstorage.exception.VendorOperationFailureException;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
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
     * @throws VendorOperationFailureException if vendor operation fails.
     */
    public Boolean isS3BucketPresent(
            AWSCredentialsProvider awsCredentialsProvider, String name, String region) throws VendorOperationFailureException {
        try {
            return AmazonS3ClientBuilder.standard()
                    .withRegion(region)
                    .withCredentials(awsCredentialsProvider)
                    .build()
                    .doesBucketExistV2(name);
        } catch (Exception e) {
            throw new VendorOperationFailureException(e.getMessage());
        }
    }

    /**
     * Creates S3 bucket with the given name.
     *
     * @param awsCredentialsProvider given providers to be used for client configuration.
     * @param name given name of the S3 bucket.
     * @param region given region to be used for client configuration.
     * @throws VendorOperationFailureException if vendor operation fails.
     */
    public void createS3Bucket(
            AWSCredentialsProvider awsCredentialsProvider, String name, String region) throws VendorOperationFailureException {
        AmazonS3 simpleStorage =
                AmazonS3ClientBuilder.standard()
                        .withRegion(region)
                        .withCredentials(awsCredentialsProvider)
                        .build();

        try {
            simpleStorage.createBucket(name);
        } catch (Exception e) {
            throw new VendorOperationFailureException(e.getMessage());
        }

        AmazonS3Waiters simpleStorageWaiter = simpleStorage.waiters();

        try {
            simpleStorageWaiter.bucketExists().run(new WaiterParameters<>(new HeadBucketRequest(name)));
        } catch (Exception e) {
            throw new VendorOperationFailureException(e.getMessage());
        }
    }

    /**
     * Removes S3 bucket with the given name.
     *
     * @param awsCredentialsProvider given providers to be used for client configuration.
     * @param name given name of the S3 bucket.
     * @param region given region to be used for client configuration.
     * @throws VendorOperationFailureException if vendor operation fails.
     */
    public void removeS3Bucket(
            AWSCredentialsProvider awsCredentialsProvider, String name, String region) throws VendorOperationFailureException {
        AmazonS3 simpleStorage =
                AmazonS3ClientBuilder.standard()
                        .withRegion(region)
                        .withCredentials(awsCredentialsProvider)
                        .build();

        ObjectListing objects;

        try {
            objects = simpleStorage.listObjects(name);
        } catch (Exception e) {
            throw new VendorOperationFailureException(e.getMessage());
        }

        do {
            for (S3ObjectSummary summary : objects.getObjectSummaries()) {
                simpleStorage.deleteObject(name, summary.getKey());
            }

            try {
                objects = simpleStorage.listNextBatchOfObjects(objects);
            } catch (Exception e) {
                throw new VendorOperationFailureException(e.getMessage());
            }
        } while (objects.isTruncated());

        try {
            simpleStorage.deleteBucket(name);
        } catch (Exception e) {
            throw new VendorOperationFailureException(e.getMessage());
        }

        AmazonS3Waiters simpleStorageWaiter = simpleStorage.waiters();

        try {
            simpleStorageWaiter.bucketNotExists().run(new WaiterParameters<>(new HeadBucketRequest(name)));
        } catch (Exception e) {
            throw new VendorOperationFailureException(e.getMessage());
        }
    }

    /**
     * Uploads object to the S3 bucket with the given name.
     *
     * @param awsCredentialsProvider given providers to be used for client configuration.
     * @param bucketName given name of the S3 bucket.
     * @param region given region to be used for client configuration.
     * @param fileName given name of the file to be uploaded.
     * @param inputStream given file input stream to be used for object upload.
     * @throws VendorOperationFailureException if vendor operation fails.
     */
    public void uploadObjectToS3Bucket(
            AWSCredentialsProvider awsCredentialsProvider,
            String bucketName,
            String region,
            String fileName,
            InputStream inputStream) throws VendorOperationFailureException {
        AmazonS3 simpleStorage =
                AmazonS3ClientBuilder.standard()
                        .withRegion(region)
                        .withCredentials(awsCredentialsProvider)
                        .build();

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("text/plain");
        metadata.setUserMetadata(new HashMap<>() {
            {
                put("objectstorage", "true");
            }
        });
        try {
            metadata.setContentLength(inputStream.available());
        } catch (IOException e) {
            throw new VendorOperationFailureException(e.getMessage());
        }

        PutObjectRequest request = new PutObjectRequest(bucketName, fileName, inputStream, metadata);

        try {
            simpleStorage.putObject(request);
        } catch (Exception e) {
            throw new VendorOperationFailureException(e.getMessage());
        }

        AmazonS3Waiters simpleStorageWaiter = simpleStorage.waiters();

        try {
            simpleStorageWaiter.objectExists().run(
                    new WaiterParameters<>(new GetObjectMetadataRequest(bucketName, fileName)));
        } catch (Exception e) {
            throw new VendorOperationFailureException(e.getMessage());
        }
    }

    /**
     * Checks if object exists in the S3 bucket with the given name.
     *
     * @param awsCredentialsProvider given providers to be used for client configuration.
     * @param bucketName given name of the S3 bucket.
     * @param region given region to be used for client configuration.
     * @param fileName given name of the file to be retrieved.
     * @return result of the check.
     * @throws VendorOperationFailureException if vendor operation fails.
     */
    public Boolean isObjectPresentInBucket(
            AWSCredentialsProvider awsCredentialsProvider,
            String bucketName,
            String region,
            String fileName) throws VendorOperationFailureException {
        AmazonS3 simpleStorage =
                AmazonS3ClientBuilder.standard()
                        .withRegion(region)
                        .withCredentials(awsCredentialsProvider)
                        .build();

        try {
            return simpleStorage.doesObjectExist(bucketName, fileName);
        } catch (Exception e) {
            throw new VendorOperationFailureException(e.getMessage());
        }
    }

    /**
     * Retrieves object from the S3 bucket with the given name.
     *
     * @param awsCredentialsProvider given providers to be used for client configuration.
     * @param bucketName given name of the S3 bucket.
     * @param region given region to be used for client configuration.
     * @param fileName given name of the file to be retrieved.
     * @return retrieved object content.
     * @throws S3BucketObjectRetrievalFailureException if s3 bucket object retrieval fails.
     * @throws VendorOperationFailureException if vendor operation fails.
     */
    public byte[] retrieveObjectFromS3Bucket(
            AWSCredentialsProvider awsCredentialsProvider,
            String bucketName,
            String region,
            String fileName)
            throws S3BucketObjectRetrievalFailureException, VendorOperationFailureException {
        AmazonS3 simpleStorage =
                AmazonS3ClientBuilder.standard()
                        .withRegion(region)
                        .withCredentials(awsCredentialsProvider)
                        .build();

        S3Object object;

        try {
            object = simpleStorage.getObject(bucketName, fileName);
        } catch (Exception e) {
            throw new VendorOperationFailureException(e.getMessage());
        }

        try {
            return IOUtils.toByteArray(object.getObjectContent());
        } catch (IOException e) {
            throw new S3BucketObjectRetrievalFailureException(e.getMessage());
        }
    }

    /**
     * Lists all the objects from the S3 bucket with the given name.
     *
     * @param awsCredentialsProvider given providers to be used for client configuration.
     * @param bucketName given name of the S3 bucket.
     * @param region given region to be used for client configuration.
     * @return listed objects.
     * @throws VendorOperationFailureException if vendor operation fails.
     */
    public List<VendorObjectListingDto> listObjectsFromS3Bucket(
            AWSCredentialsProvider awsCredentialsProvider,
            String bucketName,
            String region) throws VendorOperationFailureException {
        AmazonS3 simpleStorage =
                AmazonS3ClientBuilder.standard()
                        .withRegion(region)
                        .withCredentials(awsCredentialsProvider)
                        .build();

        try {
            return simpleStorage
                    .listObjects(bucketName)
                    .getObjectSummaries()
                    .stream().map(element -> VendorObjectListingDto.of(
                            element.getKey(), element.getLastModified().getTime()))
                    .toList();
        } catch (Exception e) {
            throw new VendorOperationFailureException(e.getMessage());
        }
    }

    /**
     * Removes object from the S3 bucket with the given name.
     *
     * @param awsCredentialsProvider given providers to be used for client configuration.
     * @param bucketName given name of the S3 bucket.
     * @param region given region to be used for client configuration.
     * @param fileName given name of the file to be removed.
     * @throws VendorOperationFailureException if vendor operation fails.
     */
    public void removeObjectFromS3Bucket(
            AWSCredentialsProvider awsCredentialsProvider,
            String bucketName,
            String region,
            String fileName) throws VendorOperationFailureException {
        AmazonS3 simpleStorage =
                AmazonS3ClientBuilder.standard()
                        .withRegion(region)
                        .withCredentials(awsCredentialsProvider)
                        .build();

        try {
            simpleStorage.deleteObject(bucketName, fileName);
        } catch (Exception e) {
            throw new VendorOperationFailureException(e.getMessage());
        }

        AmazonS3Waiters simpleStorageWaiter = simpleStorage.waiters();

        try {
            simpleStorageWaiter.objectNotExists().run(
                    new WaiterParameters<>(new GetObjectMetadataRequest(bucketName, fileName)));
        } catch (Exception e) {
            throw new VendorOperationFailureException(e.getMessage());
        }
    }

    /**
     * Removes all objects from the S3 bucket with the given name.
     *
     * @param awsCredentialsProvider given providers to be used for client configuration.
     * @param bucketName given name of the S3 bucket.
     * @param region given region to be used for client configuration.
     * @throws VendorOperationFailureException if vendor operation fails.
     */
    public void removeAllObjectsFromS3Bucket(
            AWSCredentialsProvider awsCredentialsProvider,
            String bucketName,
            String region) throws VendorOperationFailureException {
        AmazonS3 simpleStorage =
                AmazonS3ClientBuilder.standard()
                        .withRegion(region)
                        .withCredentials(awsCredentialsProvider)
                        .build();

        ObjectListing objectListing;

        try {
            objectListing = simpleStorage.listObjects(bucketName);
        } catch (Exception e) {
            throw new VendorOperationFailureException(e.getMessage());
        }

        while (true) {
            for (S3ObjectSummary s3ObjectSummary : objectListing.getObjectSummaries()) {
                simpleStorage.deleteObject(bucketName, s3ObjectSummary.getKey());
            }

            if (objectListing.isTruncated()) {
                try {
                    objectListing = simpleStorage.listNextBatchOfObjects(objectListing);
                } catch (Exception e) {
                    throw new VendorOperationFailureException(e.getMessage());
                }
            } else {
                break;
            }
        }
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
