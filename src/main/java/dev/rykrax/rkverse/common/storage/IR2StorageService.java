package dev.rykrax.rkverse.common.storage;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IR2StorageService {
    String uploadCoverImage(MultipartFile file, Long comicId);
//    String uploadFile(MultipartFile file, String folderPath);
    String uploadBytesWithKey(byte[] bytes, String objectKey, String contentType);
//    String uploadBytes(byte[] bytes, String fileName, String contentType, String folderPath);
//    void deleteFile(String fileKeyOrUrl);
//    void deleteFiles(List<String> fileKeysOrUrls);
}
