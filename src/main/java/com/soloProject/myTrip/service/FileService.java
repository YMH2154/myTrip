package com.soloProject.myTrip.service;

import com.soloProject.myTrip.repository.ItemRepository;
import com.soloProject.myTrip.repository.ScheduleRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.util.UUID;

@Transactional
@Service
@RequiredArgsConstructor
public class FileService {

    private final ItemRepository itemRepository;
    private final ScheduleRepository scheduleRepository;
    private static final Logger log = LoggerFactory.getLogger(FileService.class);

    @Value("${thumbnailImageLocation}")
    private String thumbnailImageLocation;

    @Value("${itemDetailImageLocation}")
    private String itemDetailImageLocation;

    @Value("${activityImageLocation}")
    private String activityImageLocation;

    public String saveImageFile(MultipartFile imageFile, String value) throws Exception {
        String oriFilename = imageFile.getOriginalFilename();
        log.info("이미지 파일 저장 시작 - 원본 파일명: {}, 타입: {}", oriFilename, value);

        String imageUrl = "";
        String pathUrl = "";
        String location;

        if (value.equals("detail")) {
            location = itemDetailImageLocation;
            pathUrl = "/itemDetailImages/";
        } else if (value.equals("thumbnail")) {
            location = thumbnailImageLocation;
            pathUrl = "/thumbnailImages/";
        } else if (value.equals("activity")) {
            location = activityImageLocation;
            pathUrl = "/activityImages/";
        } else {
            log.error("잘못된 이미지 타입: {}", value);
            throw new IllegalArgumentException("잘못된 이미지 타입입니다.");
        }

        log.info("저장 위치: {}, URL 경로: {}", location, pathUrl);

        if (!StringUtils.isEmpty(oriFilename)) {
            String fileName = uploadFile(location, oriFilename, imageFile.getBytes());
            imageUrl = pathUrl + fileName;
            log.info("이미지 파일 저장 완료 - URL: {}", imageUrl);
        } else {
            log.warn("원본 파일명이 비어있습니다.");
        }

        return imageUrl;
    }

    private String getFileName(String savedFileUrl) {
        return savedFileUrl.substring(savedFileUrl.lastIndexOf("/") + 1);
    }

    // 이미지 업데이트
    public String updateImgFile(Long id, MultipartFile imageFile, String value) throws Exception {
        log.info("이미지 파일 업데이트 시작 - ID: {}, 타입: {}", id, value);

        String savedFileUrl = itemRepository.findById(id)
                .orElseThrow(EntityNotFoundException::new)
                .getItemDetailImageUrl();
        log.info("기존 파일 URL: {}", savedFileUrl);

        if (!imageFile.isEmpty() && !StringUtils.isEmpty(savedFileUrl)) {
            String filePath = itemDetailImageLocation + "/" + getFileName(savedFileUrl);
            log.info("기존 파일 삭제 시도 - 경로: {}", filePath);
            deleteFile(filePath);
        }

        String oriFilename = imageFile.getOriginalFilename();
        String newFileName = uploadFile(itemDetailImageLocation, oriFilename, imageFile.getBytes());
        log.info("새 파일 업로드 완료 - 파일명: {}", newFileName);

        return newFileName;
    }

    // 파일 업로드
    public String uploadFile(String uploadPath, String originalFileName, byte[] fileData) throws Exception {
        UUID uuid = UUID.randomUUID();
        String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        String savedFileName = uuid + extension;
        String fileUploadFullUrl = uploadPath + "/" + savedFileName;

        log.info("파일 업로드 시작 - 경로: {}, 원본 파일명: {}", uploadPath, originalFileName);

        FileOutputStream fos = new FileOutputStream(fileUploadFullUrl);
        fos.write(fileData);
        fos.close();

        log.info("파일 업로드 완료 - 저장된 파일명: {}", savedFileName);
        return savedFileName;
    }

    public void deleteFile(String filePath) throws Exception {
        log.info("파일 삭제 시도 - 경로: {}", filePath);
        File deleteFile = new File(filePath);

        if (deleteFile.exists()) {
            if (deleteFile.delete()) {
                log.info("파일 삭제 성공: {}", filePath);
            } else {
                log.error("파일 삭제 실패: {}", filePath);
                throw new Exception("파일 삭제에 실패했습니다: " + filePath);
            }
        } else {
            log.warn("파일이 존재하지 않음: {}", filePath);
        }
    }

}
