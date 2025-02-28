package com.soloProject.myTrip.service;

import com.soloProject.myTrip.dto.BannnerFormDto;
import com.soloProject.myTrip.entity.Banners;
import com.soloProject.myTrip.repository.BannersRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class BannersService {

    private final BannersRepository bannersRepository;
    private final FileService fileService;

    @Value("${bannerImageLocation}")
    private String bannerImageLocation;

    // 배너 삭제
    public void deleteBanner(Long bannerId) {
        Banners banners = bannersRepository.findById(bannerId).orElseThrow(EntityNotFoundException::new);
        bannersRepository.delete(banners);
    }


    // 배너 저장
    public void saveBanner(BannnerFormDto bannnerFormDto, MultipartFile bannerImgFile) throws Exception {
        Banners banners = bannnerFormDto.createBanner();
        banners.setBannerImgUrl(fileService.saveImageFile(bannerImgFile, "banner"));
        bannersRepository.save(banners);

    }

    @Transactional
    public void updateBanner(BannnerFormDto bannnerFormDto, MultipartFile bannerImgFile) throws Exception {
        // 배너 엔티티 조회
        Banners banners = bannersRepository.findById(bannnerFormDto.getId())
                .orElseThrow(() -> new EntityNotFoundException("배너를 찾을 수 없습니다. id=" + bannnerFormDto.getId()));

        // 배너 정보 업데이트
        banners.updateBanner(bannnerFormDto);
        // 새 이미지 저장
        String newBannerUrl = fileService.updateImgFile(bannnerFormDto.getId(), bannerImgFile, "banner");
        banners.setBannerImgUrl(newBannerUrl);

    }

    // 배너 상세 조회
    @Transactional(readOnly = true)
    public BannnerFormDto getBannerDtl(Long bannerId) {
        Banners banners = bannersRepository.findById(bannerId).orElseThrow(EntityNotFoundException::new);

        return BannnerFormDto.of(banners);
    }

    @Transactional(readOnly = true)
    public List<Banners> getBannerList() {
        return bannersRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<BannnerFormDto> getLatestContents() {
        return bannersRepository.findAll().stream()
                .map(BannnerFormDto::of)
                .collect(Collectors.toList());
    }

}
