package dev.rykrax.rkverse.common.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.File;
import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class R2StorageService implements IR2StorageService {

    private final S3Client s3Client;

    @Value("${cloudflare.r2.bucket-name}")
    private String bucketName;

    @Value("${cloudflare.r2.public-domain}")
    private String publicDomain;

    @Override
    public String uploadBytesWithKey(byte[] bytes, String objectKey, String contentType) {
        if (bytes == null || bytes.length == 0) {
            throw new IllegalArgumentException("Dữ liệu byte không được để trống");
        }

        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .contentType(contentType != null ? contentType : "image/webp")
                    .contentLength((long) bytes.length)
                    .build();

            s3Client.putObject(request, RequestBody.fromBytes(bytes));
            log.info("Upload byte[] lên R2 thành công với key: {}", objectKey);

            return getPublicUrl(objectKey);
        } catch (S3Exception e) {
            log.error("Lỗi S3/R2 khi upload key {}: {}", objectKey, e.awsErrorDetails().errorMessage());
            throw new RuntimeException("Lỗi upload lên Cloudflare R2", e);
        }
    }

    @Override
    public String uploadCoverImage(MultipartFile file, Long comicId) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Ảnh bìa không được để trống");
        }
        String objectKey = String.format("comics/%d/cover.webp", comicId);

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .contentType(file.getContentType() != null ? file.getContentType() : "image/webp")
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            log.info("Upload ảnh bìa thành công cho Comic ID: {}, key: {}", comicId, objectKey);

            return getPublicUrl(objectKey);
        } catch (IOException e) {
            log.error("Lỗi khi đọc file ảnh bìa comic ID {}: {}", comicId, e.getMessage());
            throw new RuntimeException("Không thể đọc dữ liệu ảnh bìa", e);
        } catch (S3Exception e) {
            log.error("Lỗi Cloudflare R2 khi upload ảnh bìa key {}: {}", objectKey, e.awsErrorDetails().errorMessage());
            throw new RuntimeException("Lỗi lưu trữ ảnh bìa lên Cloudflare R2", e);
        }
    }

    @Override
    public String uploadFileWithKey(File file, String objectKey, String contentType) {
        if (file == null || !file.exists()) {
            throw new IllegalArgumentException("File upload không tồn tại");
        }

        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .contentType(contentType != null ? contentType : "image/webp")
                    .contentLength(file.length())
                    .build();

            // Stream trực tiếp từ file trên ổ cứng lên R2, không load vào RAM
            s3Client.putObject(request, RequestBody.fromFile(file));
            log.info("Upload File lên R2 thành công với key: {}", objectKey);

            return getPublicUrl(objectKey);
        } catch (S3Exception e) {
            log.error("Lỗi S3/R2 khi upload file key {}: {}", objectKey, e.awsErrorDetails().errorMessage());
            throw new RuntimeException("Lỗi upload file lên Cloudflare R2", e);
        }
    }
//    @Override
//    public void deleteFile(String fileKeyOrUrl) {
//        if (fileKeyOrUrl == null || fileKeyOrUrl.isBlank()) {
//            return;
//        }
//
//        String objectKey = extractKey(fileKeyOrUrl);
//        try {
//            DeleteObjectRequest request = DeleteObjectRequest.builder()
//                    .bucket(bucketName)
//                    .key(objectKey)
//                    .build();
//
//            s3Client.deleteObject(request);
//            log.info("Xóa file trên R2 thành công: {}", objectKey);
//        } catch (S3Exception e) {
//            log.error("Lỗi S3/R2 khi xóa key {}: {}", objectKey, e.awsErrorDetails().errorMessage());
//            throw new RuntimeException("Lỗi xóa file trên Cloudflare R2", e);
//        }
//    }

//    @Override
//    public void deleteFolderByPrefix(String folderPrefix) {
//        if (folderPrefix == null || folderPrefix.isBlank()) {
//            return;
//        }
//
//        String prefix = folderPrefix.startsWith("/") ? folderPrefix.substring(1) : folderPrefix;
//        if (!prefix.endsWith("/")) {
//            prefix += "/";
//        }
//
//        try {
//            ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
//                    .bucket(bucketName)
//                    .prefix(prefix)
//                    .build();
//
//            ListObjectsV2Response listResponse = s3Client.listObjectsV2(listRequest);
//            List<ObjectIdentifier> keysToDelete = listResponse.contents().stream()
//                    .map(s3Object -> ObjectIdentifier.builder().key(s3Object.key()).build())
//                    .toList();
//
//            if (keysToDelete.isEmpty()) {
//                return;
//            }
//
//            DeleteObjectsRequest deleteRequest = DeleteObjectsRequest.builder()
//                    .bucket(bucketName)
//                    .delete(Delete.builder().objects(keysToDelete).build())
//                    .build();
//
//            s3Client.deleteObjects(deleteRequest);
//            log.info("Đã dọn dẹp {} file trong prefix '{}' trên R2", keysToDelete.size(), prefix);
//        } catch (S3Exception e) {
//            log.error("Lỗi S3/R2 khi xóa folder prefix {}: {}", prefix, e.awsErrorDetails().errorMessage());
//            throw new RuntimeException("Lỗi dọn dẹp folder trên Cloudflare R2", e);
//        }
//    }

    private String getPublicUrl(String objectKey) {
        String cleanDomain = publicDomain.endsWith("/")
                ? publicDomain.substring(0, publicDomain.length() - 1)
                : publicDomain;
        String cleanKey = objectKey.startsWith("/")
                ? objectKey.substring(1)
                : objectKey;

        return cleanDomain + "/" + cleanKey;
    }
}