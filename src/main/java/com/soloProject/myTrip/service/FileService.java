package com.soloProject.myTrip.service;

import com.soloProject.myTrip.repository.BannersRepository;
import com.soloProject.myTrip.repository.ItemRepository;
import com.soloProject.myTrip.repository.ScheduleRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.util.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.util.UUID;

@Transactional
@Service
@RequiredArgsConstructor
public class FileService {

    private final ItemRepository itemRepository;
    private final ScheduleRepository scheduleRepository;
    private final BannersRepository bannersRepository;

    @Value("${thumbnailImageLocation}")
    private String thumbnailImageLocation;

    @Value("${itemDetailImageLocation}")
    private String itemDetailImageLocation;

    @Value("${activityImageLocation}")
    private String activityImageLocation;

    @Value("${bannerImageLocation}")
    private String bannerImageLocation;

    public String saveImageFile(MultipartFile imageFile, String value) throws Exception {
        String oriFilename = imageFile.getOriginalFilename();
        String imageUrl = "";
        String pathUrl = "";
        String location;
        if(value.equals("detail")){
            location = itemDetailImageLocation;
            pathUrl = "/itemDetailImages/";
        }
        else if(value.equals("thumbnail")){
            location = thumbnailImageLocation;
            pathUrl = "/thumbnailImages/";
        }
        else if(value.equals("schedule")){
            location = activityImageLocation;
            pathUrl = "/activityImages/";
        }
        else{
            location = bannerImageLocation;
            pathUrl = "/banner/";
        }

        if (!StringUtils.isEmpty(oriFilename)) {
            String fileName = uploadFile(location, oriFilename, imageFile.getBytes());
            imageUrl = pathUrl + fileName;
        }
        return imageUrl;
    }

    private String getFileName(String savedFileUrl){
        return savedFileUrl.substring(savedFileUrl.lastIndexOf("/") + 1);
    }

    // 이미지 업데이트
    public String updateImgFile(Long id, MultipartFile imageFile, String value) throws Exception {
        String savedFileUrl;
        if(value.equals("detail")){
            savedFileUrl = itemRepository.findById(id)
                    .orElseThrow(EntityNotFoundException::new)
                    .getItemDetailImageUrl();
            if (!imageFile.isEmpty() && !StringUtils.isEmpty(savedFileUrl)) {

                deleteFile(itemDetailImageLocation + "/" + getFileName(savedFileUrl));
            }
            String oriFilename = imageFile.getOriginalFilename();
            return uploadFile(bannerImageLocation, oriFilename, imageFile.getBytes());
        }
        else{
            savedFileUrl = bannersRepository.findById(id)
                    .orElseThrow(EntityNotFoundException::new)
                    .getBannerImgUrl();
            if (!imageFile.isEmpty() && !StringUtils.isEmpty(savedFileUrl)) {
                deleteFile(bannerImageLocation + "/" + getFileName(savedFileUrl));
            }
            String oriFilename = imageFile.getOriginalFilename();
            return uploadFile(bannerImageLocation, oriFilename, imageFile.getBytes());
        }
    }

    // 파일 업로드
    public String uploadFile(String uploadPath, String originalFileName, byte[] fileData) throws Exception {
        UUID uuid = UUID.randomUUID();
        String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        String savedFileName = uuid.toString() + extension;
        String fileUploadFullUrl = uploadPath + "/" + savedFileName;

        FileOutputStream fos = new FileOutputStream(fileUploadFullUrl);
        fos.write(fileData);
        fos.close();

        return savedFileName;
    }

    public void deleteFile(String filePath) throws Exception {
        File deleteFile = new File(filePath);

        if (deleteFile.exists()) {
            if (deleteFile.delete()) {
                System.out.println("파일 삭제 성공: " + filePath);
            } else {
                System.out.println("파일 삭제 실패: " + filePath);
                throw new Exception("파일 삭제에 실패했습니다: " + filePath);
            }
        } else {
            System.out.println("파일이 존재하지 않음: " + filePath);
        }
    }

}
