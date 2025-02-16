package com.soloProject.myTrip.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileOutputStream;
import java.util.UUID;

@Transactional
@Service
@RequiredArgsConstructor
public class FileService {

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
