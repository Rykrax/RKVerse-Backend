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


//    @Override
//    public String uploadFile(MultipartFile file, String folderPath) {
//        if (file == null || file.isEmpty()) {
//            throw new IllegalArgumentException("File upload không được để trống");
//        }
//
//        String originalFilename = file.getOriginalFilename();
//        String extension = "";
//        if (originalFilename != null && originalFilename.contains(".")) {
//            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
//        }
//
//        String sanitizedFolder = sanitizeFolder(folderPath);
//        String objectKey = sanitizedFolder + UUID.randomUUID() + extension;
//
//        try {
//            PutObjectRequest request = PutObjectRequest.builder()
//                    .bucket(bucketName)
//                    .key(objectKey)
//                    .contentType(file.getContentType())
//                    .contentLength(file.getSize())
//                    .build();
//
//            s3Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
//            log.info("Upload file lên R2 thành công: {}", objectKey);
//
//            return getPublicUrl(objectKey);
//        } catch (IOException e) {
//            log.error("Lỗi đọc dữ liệu từ file upload: {}", e.getMessage());
//            throw new RuntimeException("Lỗi hệ thống khi đọc file upload", e);
//        } catch (S3Exception e) {
//            log.error("Lỗi S3/R2 khi upload file: {}", e.awsErrorDetails().errorMessage());
//            throw new RuntimeException("Lỗi upload lên Cloudflare R2", e);
//        }
//    }
//
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
//
//    @Override
//    public String uploadBytes(byte[] bytes, String fileName, String contentType, String folderPath) {
//        String sanitizedFolder = sanitizeFolder(folderPath);
//        String objectKey = sanitizedFolder + UUID.randomUUID() + "_" + fileName;
//        return uploadBytesWithKey(bytes, objectKey, contentType);
//    }
//
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
//
//    @Override
//    public void deleteFiles(List<String> fileKeysOrUrls) {
//        if (fileKeysOrUrls == null || fileKeysOrUrls.isEmpty()) {
//            return;
//        }
//
//        List<ObjectIdentifier> keysToDelete = fileKeysOrUrls.stream()
//                .filter(url -> url != null && !url.isBlank())
//                .map(this::extractKey)
//                .map(key -> ObjectIdentifier.builder().key(key).build())
//                .toList();
//
//        if (keysToDelete.isEmpty()) {
//            return;
//        }
//
//        try {
//            DeleteObjectsRequest request = DeleteObjectsRequest.builder()
//                    .bucket(bucketName)
//                    .delete(Delete.builder().objects(keysToDelete).build())
//                    .build();
//
//            s3Client.deleteObjects(request);
//            log.info("Xóa thành công {} files trên R2", keysToDelete.size());
//        } catch (S3Exception e) {
//            log.error("Lỗi S3/R2 khi xóa nhiều files: {}", e.awsErrorDetails().errorMessage());
//            throw new RuntimeException("Lỗi xóa files trên Cloudflare R2", e);
//        }
//    }
//
    private String getPublicUrl(String objectKey) {
        String cleanDomain = publicDomain.endsWith("/")
                ? publicDomain.substring(0, publicDomain.length() - 1)
                : publicDomain;
        String cleanKey = objectKey.startsWith("/")
                ? objectKey.substring(1)
                : objectKey;

        return cleanDomain + "/" + cleanKey;
    }
//
//    private String extractKey(String fileKeyOrUrl) {
//        String cleanDomain = publicDomain.endsWith("/")
//                ? publicDomain
//                : publicDomain + "/";
//
//        if (fileKeyOrUrl.contains(cleanDomain)) {
//            return fileKeyOrUrl.replace(cleanDomain, "");
//        }
//        if (fileKeyOrUrl.startsWith("/")) {
//            return fileKeyOrUrl.substring(1);
//        }
//        return fileKeyOrUrl;
//    }
//
//    private String sanitizeFolder(String folderPath) {
//        if (folderPath == null || folderPath.isBlank()) {
//            return "";
//        }
//        String cleanFolder = folderPath.startsWith("/") ? folderPath.substring(1) : folderPath;
//        return cleanFolder.endsWith("/") ? cleanFolder : cleanFolder + "/";
//    }
}