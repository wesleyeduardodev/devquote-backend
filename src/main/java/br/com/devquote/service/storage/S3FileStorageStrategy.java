package br.com.devquote.service.storage;
import br.com.devquote.service.SystemParameterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.List;

@Service
@Slf4j
public class S3FileStorageStrategy implements FileStorageStrategy {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final SystemParameterService systemParameterService;

    private String bucketName;
    private String region;

    public S3FileStorageStrategy(SystemParameterService systemParameterService) {

        this.systemParameterService = systemParameterService;

        this.bucketName = systemParameterService.getString("AWS_S3_BUCKET_NAME", "devquote-storage");
        this.region = systemParameterService.getString("AWS_S3_REGION", "us-east-1");

        String accessKey = systemParameterService.getString("AWS_ACCESS_KEY_ID");
        String secretKey = systemParameterService.getString("AWS_SECRET_ACCESS_KEY");

        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKey, secretKey);

        this.s3Client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .httpClient(UrlConnectionHttpClient.builder().build())
                .build();

        this.s3Presigner = S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .build();

        log.info("S3FileStorageStrategy initialized for region: {} and bucket: {}", region, bucketName);
    }

    @Override
    public String uploadFile(MultipartFile file, String path) throws IOException {
        try {
            String key = buildKey(path);
            
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            
            log.info("File uploaded successfully to S3: {}", key);
            return key;
            
        } catch (Exception e) {
            log.error("Error uploading file to S3: {}", e.getMessage(), e);
            throw new IOException("Failed to upload file to S3", e);
        }
    }

    @Override
    public String getFileUrl(String filePath) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(filePath)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofHours(1))
                    .getObjectRequest(getObjectRequest)
                    .build();

            String url = s3Presigner.presignGetObject(presignRequest).url().toString();
            log.debug("Generated presigned URL for file: {}", filePath);
            return url;
            
        } catch (Exception e) {
            log.error("Error generating presigned URL for file: {}", filePath, e);
            return null;
        }
    }

    @Override
    public InputStream getFileStream(String filePath) throws IOException {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(filePath)
                    .build();

            return s3Client.getObject(getObjectRequest);
            
        } catch (Exception e) {
            log.error("Error getting file stream from S3: {}", filePath, e);
            throw new IOException("Failed to get file stream from S3", e);
        }
    }

    @Override
    public boolean deleteFile(String filePath) {
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(filePath)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            log.info("File deleted successfully from S3: {}", filePath);
            return true;
            
        } catch (Exception e) {
            log.error("Error deleting file from S3: {}", filePath, e);
            return false;
        }
    }

    @Override
    public boolean deleteFolder(String folderPath) {
        try {

            if (!folderPath.endsWith("/")) {
                folderPath += "/";
            }

            ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .prefix(folderPath)
                    .build();

            ListObjectsV2Response listResponse = s3Client.listObjectsV2(listRequest);
            
            if (listResponse.contents().isEmpty()) {
                log.info("No objects found in folder: {}", folderPath);
                return true;
            }

            List<ObjectIdentifier> objectsToDelete = listResponse.contents().stream()
                    .map(s3Object -> ObjectIdentifier.builder().key(s3Object.key()).build())
                    .collect(java.util.stream.Collectors.toList());

            Delete delete = Delete.builder()
                    .objects(objectsToDelete)
                    .quiet(false)
                    .build();

            DeleteObjectsRequest deleteRequest = DeleteObjectsRequest.builder()
                    .bucket(bucketName)
                    .delete(delete)
                    .build();

            DeleteObjectsResponse deleteResponse = s3Client.deleteObjects(deleteRequest);
            
            int deletedCount = deleteResponse.deleted().size();
            log.info("Successfully deleted {} objects from folder: {}", deletedCount, folderPath);
            
            if (!deleteResponse.errors().isEmpty()) {
                log.warn("Some objects failed to delete from folder {}: {}", folderPath, deleteResponse.errors());
            }
            
            return deleteResponse.errors().isEmpty();
            
        } catch (Exception e) {
            log.error("Error deleting folder from S3: {}", folderPath, e);
            return false;
        }
    }

    @Override
    public boolean fileExists(String filePath) {
        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(filePath)
                    .build();

            s3Client.headObject(headObjectRequest);
            return true;
            
        } catch (NoSuchKeyException e) {
            return false;
        } catch (Exception e) {
            log.error("Error checking file existence in S3: {}", filePath, e);
            return false;
        }
    }

    private String buildKey(String path) {

        return path.replaceAll("//+", "/").replaceAll("^/+", "");
    }
}