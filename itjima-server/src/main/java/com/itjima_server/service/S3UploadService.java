package com.itjima_server.service;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3UploadService {

    private final S3Client s3Client; // AWS SDK v2의 S3Client를 주입받습니다.

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;

    public String upload(MultipartFile multipartFile) throws IOException {
        // 파일 이름이 겹치지 않도록 UUID를 사용
        String s3FileName = UUID.randomUUID() + "-" + multipartFile.getOriginalFilename();

        // S3에 파일을 업로드하기 위한 요청 객체를 생성
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(s3FileName)
                .contentType(multipartFile.getContentType())
                .contentLength(multipartFile.getSize())
                .build();

        // 파일을 S3에 업로드
        s3Client.putObject(putObjectRequest,
                RequestBody.fromInputStream(multipartFile.getInputStream(),
                        multipartFile.getSize()));

        // 업로드된 파일의 URL을 생성하여 반환
        // URL 형식: https://버킷이름.s3.리전.amazonaws.com/파일이름
        return String.format("https://%s.s3.%s.amazonaws.com/%s",
                bucket,
                s3Client.serviceClientConfiguration().region().id(),
                s3FileName);
    }

    public void delete(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            // URL이 없는 경우 아무 작업도 하지 않음
            return;
        }

        try {
            // URL에서 파일 키(파일 경로 및 이름)를 추출
            String key = fileUrl.substring(fileUrl.indexOf(bucket + "/") + bucket.length() + 1);
            String decodedKey = URLDecoder.decode(key, StandardCharsets.UTF_8);

            // 삭제 요청 객체를 생성
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(decodedKey)
                    .build();

            // 파일을 삭제
            s3Client.deleteObject(deleteObjectRequest);

        } catch (Exception e) {
            System.err.println("S3 파일 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

}