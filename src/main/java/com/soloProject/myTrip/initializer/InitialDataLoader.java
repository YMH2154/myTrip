//package com.soloProject.myTrip.Intitializer;
//
//import com.soloProject.myTrip.constant.*;
//import com.soloProject.myTrip.entity.Item;
//import com.soloProject.myTrip.entity.Schedule;
//import com.soloProject.myTrip.repository.ItemRepository;
//import com.soloProject.myTrip.repository.ScheduleRepository;
//import com.soloProject.myTrip.service.ItemReservationService;
//import jakarta.annotation.PostConstruct;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//
//@Component
//@RequiredArgsConstructor
//public class InitialDataLoader {
//
//  private final ItemRepository itemRepository;
//  private final ScheduleRepository scheduleRepository;
//  private final ItemReservationService itemReservationService;
//
//  @PostConstruct
//  @Transactional
//  public void init() {
//    createSpainPortugalTour();
//    createJapanTour();
//    createKoreaTour();
//    createEuropeTour();
//    createVietnamTour();
//    createThailandTour();
//    createSwitzerlandTour();
//    createTurkeyTour();
//  }
//
//  private void createSpainPortugalTour() {
//    Item item = new Item();
//    item.setItemName("[엘로팜딜] 스페인/포르투갈 5일#바르셀로나직항#8대 내부관람");
//    item.setTravelType(TravelType.OVERSEAS);
//    item.setOverseasCategory(OverseasCategory.EUROPE);
//    item.setOrigin(AirportCode.ICN);
//    item.setDestination(AirportCode.BCN);
//    item.setPrice(3990000);
//    item.setItemDetail("스페인과 포르투갈의 핵심 도시를 모두 관람하는 알찬 일정!");
//    item.setMaxParticipants(20);
//    item.setMinParticipants(10);
//    item.setCurrentParticipants(0);
//    item.setNight(7);
//    item.setDuration(9);
//    item.setHasLeader(true);
//    item.setHasGuideFee(true);
//    item.setGuideFee(100);
//    item.setGuideFeeUnit(CurrencyUnit.EUR);
//    item.setHasShopping(true);
//    item.setShoppingCount(2);
//    item.setHasInsurance(true);
//
//    List<String> imageUrls = Arrays.asList(
//        "/images/spain1.jpg",
//        "/images/spain2.jpg",
//        "/images/spain3.jpg");
//    item.setItemImageUrls(imageUrls);
//
//    Item savedItem = itemRepository.save(item);
//
//    List<Schedule> schedules = new ArrayList<>();
//    schedules.add(createSchedule(savedItem, 1, "바르셀로나 도착",
//        "바르셀로나 공항 도착 후 가우디의 도시 바르셀로나 관광 시작", "/images/barcelona.jpg"));
//    schedules.add(createSchedule(savedItem, 2, "마드리드로 이동",
//        "고속열차로 마드리드로 이동, 프라도 미술관 관람", "/images/madrid.jpg"));
//    // ... 나머지 일정 추가
//
//    scheduleRepository.saveAll(schedules);
//    itemReservationService.initializeReservations(savedItem);
//  }
//
//  private void createJapanTour() {
//    Item item = new Item();
//    item.setItemName("[온리재팬] 일본 북해도 4일#삿포로/오타루#특급료칸");
//    item.setTravelType(TravelType.OVERSEAS);
//    item.setOverseasCategory(OverseasCategory.JAPAN);
//    item.setOrigin(AirportCode.ICN);
//    item.setDestination(AirportCode.CTS);
//    item.setPrice(1290000);
//    item.setItemDetail("겨울 북해도의 매력을 느낄 수 있는 완벽한 일정!");
//    item.setMaxParticipants(15);
//    item.setMinParticipants(5);
//    item.setCurrentParticipants(0);
//    item.setNight(3);
//    item.setDuration(4);
//    item.setHasLeader(true);
//    item.setHasGuideFee(true);
//    item.setGuideFee(10000);
//    item.setGuideFeeUnit(CurrencyUnit.JPY);
//    item.setHasShopping(true);
//    item.setShoppingCount(1);
//    item.setHasInsurance(true);
//
//    // ... 이미지 및 일정 추가
//    Item savedItem = itemRepository.save(item);
//    itemReservationService.initializeReservations(savedItem);
//  }
//
//  private void createKoreaTour() {
//    Item item = new Item();
//    item.setItemName("[제주도] 제주 3일#렌터카#특급호텔#자유여행");
//    item.setTravelType(TravelType.DOMESTIC);
//    item.setDomesticCategory(DomesticCategory.JEJU);
//    item.setOrigin(AirportCode.GMP);
//    item.setDestination(AirportCode.CJU);
//    item.setPrice(399000);
//    item.setItemDetail("렌터카와 특급호텔이 포함된 제주도 3일 자유여행!");
//    item.setMaxParticipants(20);
//    item.setMinParticipants(2);
//    item.setCurrentParticipants(0);
//    item.setNight(2);
//    item.setDuration(3);
//    item.setHasLeader(false);
//    item.setHasGuideFee(false);
//    item.setGuideFee(0);
//    item.setGuideFeeUnit(CurrencyUnit.KRW);
//    item.setHasShopping(false);
//    item.setShoppingCount(0);
//    item.setHasInsurance(true);
//
//    List<String> imageUrls = Arrays.asList(
//            "/images/jeju1.jpg",
//            "/images/jeju2.jpg",
//            "/images/jeju3.jpg");
//    item.setItemImageUrls(imageUrls);
//
//    Item savedItem = itemRepository.save(item);
//
//    List<Schedule> schedules = new ArrayList<>();
//    schedules.add(createSchedule(savedItem, 1, "제주도 도착 및 렌터카 픽업",
//            "제주공항 도착 후 렌터카 픽업, 자유여행 시작", "/images/jeju_arrival.jpg"));
//    schedules.add(createSchedule(savedItem, 2, "성산일출봉 및 우도 여행",
//            "성산일출봉과 우도 방문, 제주 바다 감상", "/images/seongsan.jpg"));
//    schedules.add(createSchedule(savedItem, 3, "오설록 티뮤지엄 및 귀경",
//            "오설록 티뮤지엄 관람 후 렌터카 반납, 서울 귀경", "/images/osulloc.jpg"));
//
//    scheduleRepository.saveAll(schedules);
//    itemReservationService.initializeReservations(savedItem);
//  }
//
//
//  private void createVietnamTour() {
//    Item item = new Item();
//    item.setItemName("[베트남] 다낭/호이안 5일#바나힐#호이안야경");
//    item.setTravelType(TravelType.OVERSEAS);
//    item.setOverseasCategory(OverseasCategory.VIETNAM);
//    item.setOrigin(AirportCode.ICN);
//    item.setDestination(AirportCode.DAD);
//    item.setPrice(899000);
//    item.setItemDetail("바나힐과 호이안 야경을 즐길 수 있는 다낭/호이안 5일 일정!");
//    item.setMaxParticipants(20);
//    item.setMinParticipants(5);
//    item.setCurrentParticipants(0);
//    item.setNight(4);
//    item.setDuration(5);
//    item.setHasLeader(true);
//    item.setHasGuideFee(true);
//    item.setGuideFee(20);
//    item.setGuideFeeUnit(CurrencyUnit.USD);
//    item.setHasShopping(true);
//    item.setShoppingCount(2);
//    item.setHasInsurance(true);
//
//    List<String> imageUrls = Arrays.asList(
//            "/images/danang1.jpg",
//            "/images/danang2.jpg",
//            "/images/danang3.jpg");
//    item.setItemImageUrls(imageUrls);
//
//    Item savedItem = itemRepository.save(item);
//
//    List<Schedule> schedules = new ArrayList<>();
//    schedules.add(createSchedule(savedItem, 1, "다낭 도착 및 시내 투어",
//            "다낭 공항 도착 후 시내 투어 및 미케 해변 방문", "/images/danang_city.jpg"));
//    schedules.add(createSchedule(savedItem, 2, "바나힐 관광",
//            "바나힐에서 골든브릿지와 테마파크 방문", "/images/banahills.jpg"));
//    schedules.add(createSchedule(savedItem, 3, "호이안 야경 투어",
//            "호이안 구시가지 야경 및 전통 등불 관람", "/images/hoian.jpg"));
//    schedules.add(createSchedule(savedItem, 4, "다낭 자유일정",
//            "다낭에서 자유일정 및 현지식 체험", "/images/danang_free.jpg"));
//    schedules.add(createSchedule(savedItem, 5, "귀국",
//            "다낭 공항 출발 및 귀국", "/images/danang_departure.jpg"));
//
//    scheduleRepository.saveAll(schedules);
//    itemReservationService.initializeReservations(savedItem);
//  }
//
//
//  private void createEuropeTour() {
//    Item item = new Item();
//    item.setItemName("[프랑스/스위스] 파리/취리히 7일#에펠탑#융프라우");
//    item.setTravelType(TravelType.OVERSEAS);
//    item.setOverseasCategory(OverseasCategory.EUROPE);
//    item.setOrigin(AirportCode.ICN);
//    item.setDestination(AirportCode.CDG);
//    item.setPrice(3590000);
//    item.setItemDetail("파리와 스위스의 아름다운 자연과 문화를 즐길 수 있는 7일 일정!");
//    item.setMaxParticipants(15);
//    item.setMinParticipants(5);
//    item.setCurrentParticipants(0);
//    item.setNight(6);
//    item.setDuration(7);
//    item.setHasLeader(true);
//    item.setHasGuideFee(true);
//    item.setGuideFee(150);
//    item.setGuideFeeUnit(CurrencyUnit.EUR);
//    item.setHasShopping(true);
//    item.setShoppingCount(3);
//    item.setHasInsurance(true);
//
//    List<String> imageUrls = Arrays.asList(
//            "/images/europe1.jpg",
//            "/images/europe2.jpg",
//            "/images/europe3.jpg");
//    item.setItemImageUrls(imageUrls);
//
//    Item savedItem = itemRepository.save(item);
//
//    List<Schedule> schedules = new ArrayList<>();
//    schedules.add(createSchedule(savedItem, 1, "파리 도착 및 시내 투어",
//            "파리 공항 도착 후 에펠탑 및 루브르 박물관 관람", "/images/paris.jpg"));
//    schedules.add(createSchedule(savedItem, 2, "몽마르트 언덕 및 자유시간",
//            "몽마르트 언덕과 샹젤리제 거리 방문", "/images/montmartre.jpg"));
//    schedules.add(createSchedule(savedItem, 3, "스위스 이동",
//            "TGV를 타고 취리히로 이동", "/images/tgv.jpg"));
//    schedules.add(createSchedule(savedItem, 4, "융프라우 관광",
//            "융프라우 정상 방문 및 스위스 알프스 감상", "/images/jungfrau.jpg"));
//    schedules.add(createSchedule(savedItem, 5, "인터라켄 자유일정",
//            "인터라켄에서 패러글라이딩 체험", "/images/interlaken.jpg"));
//    schedules.add(createSchedule(savedItem, 6, "루체른 관광",
//            "카펠교와 비취호수 관광", "/images/lucerne.jpg"));
//    schedules.add(createSchedule(savedItem, 7, "취리히 출발",
//            "취리히 공항 출발 및 귀국", "/images/zurich.jpg"));
//
//    scheduleRepository.saveAll(schedules);
//    itemReservationService.initializeReservations(savedItem);
//  }
//
//
//  private void createThailandTour() {
//    Item item = new Item();
//    item.setItemName("[태국] 방콕/파타야 5일#마사지2회#씨푸드특식");
//    item.setTravelType(TravelType.OVERSEAS);
//    item.setOverseasCategory(OverseasCategory.THAILAND);
//    item.setOrigin(AirportCode.ICN);
//    item.setDestination(AirportCode.BKK);
//    item.setPrice(699000);
//    item.setItemDetail("방콕과 파타야의 핵심 관광지를 모두 즐길 수 있는 알찬 일정!");
//    item.setMaxParticipants(25);
//    item.setMinParticipants(10);
//    item.setCurrentParticipants(0);
//    item.setNight(4);
//    item.setDuration(5);
//    item.setHasLeader(true);
//    item.setHasGuideFee(true);
//    item.setGuideFee(1500);
//    item.setGuideFeeUnit(CurrencyUnit.THB);
//    item.setHasShopping(true);
//    item.setShoppingCount(2);
//    item.setHasInsurance(true);
//
//    List<String> imageUrls = Arrays.asList(
//            "/images/thailand1.jpg",
//            "/images/thailand2.jpg",
//            "/images/thailand3.jpg");
//    item.setItemImageUrls(imageUrls);
//
//    Item savedItem = itemRepository.save(item);
//
//    List<Schedule> schedules = new ArrayList<>();
//    schedules.add(createSchedule(savedItem, 1, "방콕 도착",
//            "방콕 공항 도착 후 시내 투어 및 마사지 체험", "/images/bangkok.jpg"));
//    schedules.add(createSchedule(savedItem, 2, "파타야 이동",
//            "파타야 해변 및 씨푸드 특식 저녁", "/images/pattaya.jpg"));
//
//    scheduleRepository.saveAll(schedules);
//    itemReservationService.initializeReservations(savedItem);
//  }
//
//  private void createSwitzerlandTour() {
//    Item item = new Item();
//    item.setItemName("[스위스] 스위스 일주 6일#빙하특급#마테호른");
//    item.setTravelType(TravelType.OVERSEAS);
//    item.setOverseasCategory(OverseasCategory.SWISS);
//    item.setOrigin(AirportCode.ICN);
//    item.setDestination(AirportCode.ZRH);
//    item.setPrice(4590000);
//    item.setItemDetail("스위스의 대자연을 만끽할 수 있는 빙하특급과 마테호른 관광 일정!");
//    item.setMaxParticipants(20);
//    item.setMinParticipants(8);
//    item.setCurrentParticipants(0);
//    item.setNight(5);
//    item.setDuration(6);
//    item.setHasLeader(true);
//    item.setHasGuideFee(true);
//    item.setGuideFee(120);
//    item.setGuideFeeUnit(CurrencyUnit.CHF);
//    item.setHasShopping(false);
//    item.setShoppingCount(0);
//    item.setHasInsurance(true);
//
//    List<String> imageUrls = Arrays.asList(
//            "/images/switzerland1.jpg",
//            "/images/switzerland2.jpg",
//            "/images/switzerland3.jpg");
//    item.setItemImageUrls(imageUrls);
//
//    Item savedItem = itemRepository.save(item);
//
//    List<Schedule> schedules = new ArrayList<>();
//    schedules.add(createSchedule(savedItem, 1, "취리히 도착",
//            "취리히 공항 도착 후 스위스 시내 투어", "/images/zurich.jpg"));
//    schedules.add(createSchedule(savedItem, 2, "빙하특급 탑승",
//            "스위스의 절경을 감상할 수 있는 빙하특급 탑승", "/images/glacier_express.jpg"));
//
//    scheduleRepository.saveAll(schedules);
//    itemReservationService.initializeReservations(savedItem);
//  }
//
//  private void createTurkeyTour() {
//    Item item = new Item();
//    item.setItemName("[터키] 터키 일주 8일#열기구#지중해크루즈");
//    item.setTravelType(TravelType.OVERSEAS);
//    item.setOverseasCategory(OverseasCategory.TURKEY);
//    item.setOrigin(AirportCode.ICN);
//    item.setDestination(AirportCode.IST);
//    item.setPrice(2990000);
//    item.setItemDetail("터키의 역사와 문화를 한 번에 체험할 수 있는 완벽한 일정!");
//    item.setMaxParticipants(20);
//    item.setMinParticipants(10);
//    item.setCurrentParticipants(0);
//    item.setNight(7);
//    item.setDuration(8);
//    item.setHasLeader(true);
//    item.setHasGuideFee(true);
//    item.setGuideFee(80);
//    item.setGuideFeeUnit(CurrencyUnit.USD);
//    item.setHasShopping(true);
//    item.setShoppingCount(3);
//    item.setHasInsurance(true);
//
//    List<String> imageUrls = Arrays.asList(
//            "/images/turkey1.jpg",
//            "/images/turkey2.jpg",
//            "/images/turkey3.jpg");
//    item.setItemImageUrls(imageUrls);
//
//    Item savedItem = itemRepository.save(item);
//
//    List<Schedule> schedules = new ArrayList<>();
//    schedules.add(createSchedule(savedItem, 1, "이스탄불 도착",
//            "이스탄불 공항 도착 후 블루모스크 및 아야소피아 관광", "/images/istanbul.jpg"));
//    schedules.add(createSchedule(savedItem, 2, "카파도키아 이동",
//            "열기구 체험 및 카파도키아 절경 감상", "/images/cappadocia.jpg"));
//
//    scheduleRepository.saveAll(schedules);
//    itemReservationService.initializeReservations(savedItem);
//  }
//
//
//  private Schedule createSchedule(Item item, int day, String activity, String description, String imageUrl) {
//    return Schedule.builder()
//        .item(item)
//        .day(day)
//        .activity(activity)
//        .description(description)
//        .imageUrl(imageUrl)
//        .build();
//  }
//}