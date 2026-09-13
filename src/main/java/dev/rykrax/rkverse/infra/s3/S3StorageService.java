package dev.rykrax.rkverse.infra.s3;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@Slf4j
@RequiredArgsConstructor
public class S3StorageService {
    private final S3Client s3Client;

    @Value("${cloudflare.r2.bucket-name}")
    private String bucketName;

    @Value("${cloudflare.r2.public-domain:}")
    private String publicDomain;

    /**
     * Upload mảng byte[] lên R2 và trả về URL công khai
     *
     * @param key         Đường dẫn file (VD: comics/12/cover.webp)
     * @param data        Dữ liệu byte[] của ảnh
     * @param contentType MIME type (VD: image/webp)
     * @return URL truy cập công khai của ảnh
     */
    public String uploadBytes(String key, byte[] data, String contentType) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(contentType)
                .build();

        // Gửi request upload lên R2
        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(data));
        log.info("Đã tải tệp lên R2 thành công với key: {}", key);

        // Chuẩn hóa đường dẫn trả về
        String cleanDomain = publicDomain.endsWith("/") ? publicDomain.substring(0, publicDomain.length() - 1) : publicDomain;
        String cleanKey = key.startsWith("/") ? key.substring(1) : key;

        return cleanDomain + "/" + cleanKey;
    }

    /**
     * Xóa tệp trên R2 nếu xảy ra lỗi cần rollback
     *
     * @param key Đường dẫn file trên R2
     */
    public void deleteFile(String key) {
        try {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();
            s3Client.deleteObject(deleteRequest);
            log.info("Đã xóa tệp trên R2: {}", key);
        } catch (Exception e) {
            log.error("Lỗi khi xóa tệp trên R2 với key: {}", key, e);
        }
    }
}
