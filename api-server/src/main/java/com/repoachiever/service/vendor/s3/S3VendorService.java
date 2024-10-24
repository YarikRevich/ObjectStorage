package com.objectstorage.service.vendor.s3;

import com.objectstorage.entity.common.PropertiesEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;
import org.apache.http.HttpStatus;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Service used to represent S3 external service provider operations.
 */
@ApplicationScoped
public class S3VendorService {
    @Inject
    PropertiesEntity properties;

//    /**
//     * Checks if S3 bucket with the given name exists.
//     *
//     * @param name name of the S3 bucket.
//     * @return result of the check.
//     */
//    public Boolean isS3BucketExist(
//            String name, AWSCredentialsProvider awsCredentialsProvider, String region) {
//        return AmazonS3ClientBuilder.standard()
//                .withRegion(region)
//                .withCredentials(awsCredentialsProvider)
//                .build()
//                .doesBucketExistV2(name);
//    }

//    /**
//     * Creates S3 bucket with the given name.
//     *
//     * @param name name of the S3 bucket.
//     */
//    public void createS3Bucket(
//            String name, AWSCredentialsProvider awsCredentialsProvider, String region) {
//        AmazonS3 simpleStorage =
//                AmazonS3ClientBuilder.standard()
//                        .withRegion(region)
//                        .withCredentials(awsCredentialsProvider)
//                        .build();
//        simpleStorage.createBucket(name);
//
//        AmazonS3Waiters simpleStorageWaiter = simpleStorage.waiters();
//        simpleStorageWaiter.bucketExists().run(new WaiterParameters<>(new HeadBucketRequest(name)));
//    }

//    /**
//     * Removes S3 bucket with the given name
//     *
//     * @param name name of the S3 bucket.
//     */
//    public void removeS3Bucket(
//            String name, AWSCredentialsProvider awsCredentialsProvider, String region) {
//        AmazonS3 simpleStorage =
//                AmazonS3ClientBuilder.standard()
//                        .withRegion(region)
//                        .withCredentials(awsCredentialsProvider)
//                        .build();
//
//        ObjectListing objects = simpleStorage.listObjects(name);
//
//        do {
//            for (S3ObjectSummary summary : objects.getObjectSummaries()) {
//                simpleStorage.deleteObject(name, summary.getKey());
//            }
//
//            objects = simpleStorage.listNextBatchOfObjects(objects);
//        } while (objects.isTruncated());
//
//        simpleStorage.deleteBucket(name);
//
//        AmazonS3Waiters simpleStorageWaiter = simpleStorage.waiters();
//        simpleStorageWaiter.bucketNotExists().run(new WaiterParameters<>(new HeadBucketRequest(name)));
//    }

//    /**
//     * Checks if the selected credentials are valid.
//     *
//     * @return result of credentials validation.
//     */
//    public Boolean isCallerValid(S3Credentials awsCredentialsProvider, String region) {
//        try {
//            return !Objects.isNull(
//                    AWSSecurityTokenServiceClientBuilder.standard()
//                            .withRegion(region)
//                            .withCredentials(awsCredentialsProvider)
//                            .build()
//                            .getCallerIdentity(new GetCallerIdentityRequest())
//                            .getArn());
//        } catch (AWSSecurityTokenServiceException e) {
//            return false;
//        }
//    }
}
