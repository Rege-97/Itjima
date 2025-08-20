package com.itjima_server.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
public class FileUtil {

    public static FileResult save(MultipartFile file, String subPath, Long userId,
            String uploadDir) {
        if (file != null && !file.isEmpty()) {
            String fileContentType = file.getContentType();

            // 이미지 파일만 허용하는 유효성 검사
            if (fileContentType == null || !fileContentType.startsWith("image/")) {
                throw new IllegalArgumentException("이미지 파일만 업로드할 수 있습니다.");
            }
            try {
                // 1. 파일 저장 경로 설정
                String fullPath = uploadDir + subPath + "/" + userId + "/";
                Path uploadPath = Paths.get(fullPath);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                // 2. 고유한 파일명 생성
                String originalFilename = file.getOriginalFilename();
                String fileExtension = null;
                if (originalFilename != null) {
                    fileExtension = originalFilename.substring(
                            originalFilename.lastIndexOf("."));
                }
                String uniqueFileName = UUID.randomUUID() + fileExtension;

                // 3. 파일 저장
                Path filePath = uploadPath.resolve(uniqueFileName);
                file.transferTo(filePath);

                // 4. 저장된 파일의 URL 설정
                String fileUrl = "/uploads/" + subPath + "/" + userId + "/" + uniqueFileName;
                String fileType = file.getContentType();

                return FileResult.builder()
                        .fileUrl(fileUrl)
                        .fileType(fileType)
                        .build();
            } catch (IOException e) {
                log.error(e.getMessage());
                throw new RuntimeException("파일 저장 중 오류가 발생했습니다.", e);
            }
        }
        return null;
    }

    public static void delete(String fileUrl, String uploadDir) {
        if (fileUrl != null && !fileUrl.isEmpty()) {
            try {
                // URL에서 실제 파일 경로로 변환 (예: "/uploads/items/1/file.jpg" -> "uploads/items/1/file.jpg")
                String relativePath = fileUrl.substring("/uploads/".length());
                Path filePath = Paths.get(uploadDir + relativePath);
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                // 파일 삭제 실패 시 예외 처리 (로그 등)
                log.error(e.getMessage());
            }
        }
    }
}
