package com.soloProject.myTrip.initializer;

import com.soloProject.myTrip.constant.*;
import com.soloProject.myTrip.entity.Item;
import com.soloProject.myTrip.entity.Schedule;
import com.soloProject.myTrip.repository.ItemRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class InitItem {

  private final ItemRepository itemRepository;

  @PostConstruct
  @Transactional
  public void init() {
      if(itemRepository.count() == 0) {
          // 도쿄 여행 상품
          createTokyoItem();

          // 오사카 여행 상품
          createOsakaItem();

          // 후쿠오카 여행 상품
          createFukuokaItem();

          // 삿포로 여행 상품
          createSapporoItem();

          // 교토 여행 상품
          createKyotoItem();

          // 오키나와 여행 상품
          createOkinawaItem();
      }
  }

  private void createTokyoItem() {
    Item item = new Item();
    item.setItemName("[도쿄] 신주쿠/아사쿠사 3박 4일 자유여행");
    item.setTravelType(TravelType.OVERSEAS);
    item.setOverseasCategory(OverseasCategory.JAPAN);
    item.setOrigin(AirportCode.ICN);
    item.setDestination(AirportCode.NRT);
    item.setPrice(599000);
    item.setLowestPrice(599000);
    item.setItemDetailImageUrl("/itemDetailImages/TokyoDTL.jpg");
    item.setMaxParticipants(20);
    item.setMinParticipants(1);
    item.setThumbnailImageUrls(Arrays.asList("/thumbnailImages/japan1.jpg", "/thumbnailImages/japan2.jpg"));
    item.setNight(3);
    item.setDuration(4);
    item.setHasLeader(false);
    item.setHasGuideFee(false);
    item.setHasShopping(false);
    item.setHasInsurance(true);
    item.setSchedules(createTokyoSchedules());

    itemRepository.save(item);
  }

  private void createOsakaItem() {
    Item item = new Item();
    item.setItemName("[오사카] 도톤보리/유니버설 4박 5일 패키지");
    item.setTravelType(TravelType.OVERSEAS);
    item.setOverseasCategory(OverseasCategory.JAPAN);
    item.setOrigin(AirportCode.ICN);
    item.setDestination(AirportCode.KIX);
    item.setPrice(899000);
    item.setLowestPrice(899000);
    item.setItemDetailImageUrl("/itemDetailImages/OsakaDTL.jpg");
    item.setMaxParticipants(15);
    item.setMinParticipants(5);
    item.setThumbnailImageUrls(Arrays.asList("/thumbnailImages/japan3.jpg", "/thumbnailImages/japan4.jpg"));
    item.setNight(4);
    item.setDuration(5);
    item.setHasLeader(true);
    item.setHasGuideFee(true);
    item.setGuideFee(50000);
    item.setGuideFeeUnit(CurrencyUnit.JPY);
    item.setHasShopping(true);
    item.setShoppingCount(2);
    item.setHasInsurance(true);
    item.setSchedules(createOsakaSchedules());

    itemRepository.save(item);
  }

  private void createFukuokaItem() {
    Item item = new Item();
    item.setItemName("[후쿠오카] 캐널시티/텐진 2박 3일 자유여행");
    item.setTravelType(TravelType.OVERSEAS);
    item.setOverseasCategory(OverseasCategory.JAPAN);
    item.setOrigin(AirportCode.ICN);
    item.setDestination(AirportCode.FUK);
    item.setPrice(399000);
    item.setLowestPrice(399000);
    item.setItemDetailImageUrl("/itemDetailImages/FukuokaDTL.jpg");
    item.setMaxParticipants(10);
    item.setMinParticipants(1);
    item.setThumbnailImageUrls(Arrays.asList("/thumbnailImages/japan5.jpg"));
    item.setNight(2);
    item.setDuration(3);
    item.setHasLeader(false);
    item.setHasGuideFee(false);
    item.setHasShopping(false);
    item.setHasInsurance(true);
    item.setSchedules(createFukuokaSchedules());

    itemRepository.save(item);
  }

  private void createSapporoItem() {
    Item item = new Item();
    item.setItemName("[삿포로] 오타루/노보리베츠 온천 3박 4일 패키지");
    item.setTravelType(TravelType.OVERSEAS);
    item.setOverseasCategory(OverseasCategory.JAPAN);
    item.setOrigin(AirportCode.ICN);
    item.setDestination(AirportCode.CTS);
    item.setPrice(799000);
    item.setLowestPrice(799000);
    item.setItemDetailImageUrl("/itemDetailImages/SapporoDTL.jpg");
    item.setMaxParticipants(12);
    item.setMinParticipants(4);
    item.setThumbnailImageUrls(Arrays.asList("/thumbnailImages/japan6.jpg", "/thumbnailImages/japan1.jpg"));
    item.setNight(3);
    item.setDuration(4);
    item.setHasLeader(true);
    item.setHasGuideFee(true);
    item.setGuideFee(40000);
    item.setGuideFeeUnit(CurrencyUnit.JPY);
    item.setHasShopping(true);
    item.setShoppingCount(1);
    item.setHasInsurance(true);
    item.setSchedules(createSapporoSchedules());

    itemRepository.save(item);
  }

  private void createKyotoItem() {
    Item item = new Item();
    item.setItemName("[교토] 기온/아라시야마 3박 4일 자유여행");
    item.setTravelType(TravelType.OVERSEAS);
    item.setOverseasCategory(OverseasCategory.JAPAN);
    item.setOrigin(AirportCode.ICN);
    item.setDestination(AirportCode.KIX);
    item.setPrice(699000);
    item.setLowestPrice(699000);
    item.setItemDetailImageUrl("/itemDetailImages/KyotoDTL.jpg");
    item.setMaxParticipants(15);
    item.setMinParticipants(1);
    item.setThumbnailImageUrls(Arrays.asList("/thumbnailImages/japan2.jpg", "/thumbnailImages/japan3.jpg"));
    item.setNight(3);
    item.setDuration(4);
    item.setHasLeader(false);
    item.setHasGuideFee(false);
    item.setHasShopping(false);
    item.setHasInsurance(true);
    item.setSchedules(createKyotoSchedules());

    itemRepository.save(item);
  }

  private void createOkinawaItem() {
    Item item = new Item();
    item.setItemName("[오키나와] 나하/츄라우미 4박 5일 패키지");
    item.setTravelType(TravelType.OVERSEAS);
    item.setOverseasCategory(OverseasCategory.JAPAN);
    item.setOrigin(AirportCode.ICN);
    item.setDestination(AirportCode.OKA);
    item.setPrice(999000);
    item.setLowestPrice(999000);
    item.setItemDetailImageUrl("/itemDetailImages/OkinawaDTL.jpg");
    item.setMaxParticipants(20);
    item.setMinParticipants(6);
    item.setThumbnailImageUrls(Arrays.asList("/thumbnailImages/japan4.jpg", "/thumbnailImages/japan5.jpg"));
    item.setNight(4);
    item.setDuration(5);
    item.setHasLeader(true);
    item.setHasGuideFee(true);
    item.setGuideFee(60000);
    item.setGuideFeeUnit(CurrencyUnit.JPY);
    item.setHasShopping(true);
    item.setShoppingCount(2);
    item.setHasInsurance(true);
    item.setSchedules(createOkinawaSchedules());

    itemRepository.save(item);
  }

  private List<Schedule> createTokyoSchedules() {
    List<Schedule> schedules = new ArrayList<>();
    schedules.add(createSchedule("신주쿠 쇼핑 및 자유시간", "신주쿠역 주변 쇼핑과 레스토랑 탐방"));
    schedules.add(createSchedule("아사쿠사 관광", "센소지 사원, 나카미세 상점가 관광"));
    schedules.add(createSchedule("도쿄 디즈니랜드", "도쿄 디즈니랜드 자유이용권"));
    return schedules;
  }

  private List<Schedule> createOsakaSchedules() {
    List<Schedule> schedules = new ArrayList<>();
    schedules.add(createSchedule("도톤보리 관광", "도톤보리 먹거리 투어 및 쇼핑"));
    schedules.add(createSchedule("유니버설 스튜디오", "유니버설 스튜디오 재팬 자유이용권"));
    schedules.add(createSchedule("오사카성", "오사카성 및 주변 관광"));
    schedules.add(createSchedule("교토 당일치기", "금각사, 청수사 관광"));
    return schedules;
  }

  private List<Schedule> createFukuokaSchedules() {
    List<Schedule> schedules = new ArrayList<>();
    schedules.add(createSchedule("캐널시티", "캐널시티 하카타 쇼핑"));
    schedules.add(createSchedule("텐진 지역", "텐진 지하상가 쇼핑"));
    return schedules;
  }

  private List<Schedule> createSapporoSchedules() {
    List<Schedule> schedules = new ArrayList<>();
    schedules.add(createSchedule("오타루 운하", "오타루 운하 관광 및 유리공예품 구경"));
    schedules.add(createSchedule("노보리베츠 온천", "노보리베츠 온천 료칸 숙박"));
    schedules.add(createSchedule("삿포로 맥주 박물관", "삿포로 맥주 박물관 견학"));
    return schedules;
  }

  private List<Schedule> createKyotoSchedules() {
    List<Schedule> schedules = new ArrayList<>();
    schedules.add(createSchedule("기온거리", "기온거리 산책 및 마이코 구경"));
    schedules.add(createSchedule("아라시야마", "대나무 숲, 도게츠교 관광"));
    schedules.add(createSchedule("후시미이나리", "천 개의 도리이 산책"));
    return schedules;
  }

  private List<Schedule> createOkinawaSchedules() {
    List<Schedule> schedules = new ArrayList<>();
    schedules.add(createSchedule("나하 시내", "국제거리 쇼핑 및 먹거리 탐방"));
    schedules.add(createSchedule("츄라우미 수족관", "오키나와 츄라우미 수족관 관람"));
    schedules.add(createSchedule("해변 액티비티", "오키나와 비치에서 스노클링"));
    schedules.add(createSchedule("슈리성", "슈리성 역사 탐방"));
    return schedules;
  }

  private Schedule createSchedule(String activity, String description) {
    Schedule schedule = new Schedule();
    schedule.setDay(1); // 기본값으로 1일차 설정
    schedule.setActivity(activity);
    schedule.setImageUrl("/activityImages/dummy.jpg");
    schedule.setDescription(description);
    return schedule;
  }
}
