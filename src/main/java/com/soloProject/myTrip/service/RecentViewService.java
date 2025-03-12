package com.soloProject.myTrip.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.soloProject.myTrip.dto.ItemFormDto;
import com.soloProject.myTrip.entity.Item;
import com.soloProject.myTrip.repository.ItemRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
@RequiredArgsConstructor
public class RecentViewService {

  private final MainService mainService;
  private final ItemRepository itemRepository;
  private final ObjectMapper objectMapper;
  private static final String COOKIE_NAME = "recentItems";
  private static final int MAX_ITEMS = 10;
  private static final int COOKIE_MAX_AGE = 7 * 24 * 60 * 60; // 7일

  public void addRecentView(HttpServletRequest request, HttpServletResponse response, Long itemId) {
    List<Long> recentItems = getRecentItemIds(request);

    // 동일한 상품이 있다면 제거 (최신 순서로 다시 추가하기 위해)
    recentItems.remove(itemId);

    // 최근 본 상품 목록 앞에 추가
    recentItems.addFirst(itemId);

    // 최대 개수 유지
    if (recentItems.size() > MAX_ITEMS) {
      recentItems = recentItems.subList(0, MAX_ITEMS);
    }

    // 쿠키 저장
    saveRecentItemsCookie(response, recentItems);
  }

  public List<Item> getRecentItems(HttpServletRequest request, Long currentItemId) {
    List<Long> recentItemIds = getRecentItemIds(request);
    List<Item> recentItems = new ArrayList<>();

    // 현재 보고 있는 상품 제외
    recentItemIds.remove(currentItemId);

    // 최근 본 상품 정보 조회
    for (Long itemId : recentItemIds) {
      try {
        Item item = itemRepository.findById(itemId).orElseThrow(EntityNotFoundException::new);
        recentItems.add(item);
      } catch (Exception e) {
        // 상품이 삭제되었거나 조회할 수 없는 경우 무시
      }
    }

    mainService.setItemInfo(recentItems);

    return recentItems;
  }

  private List<Long> getRecentItemIds(HttpServletRequest request) {
    Cookie[] cookies = request.getCookies();
    if (cookies != null) {
      for (Cookie cookie : cookies) {
        if (COOKIE_NAME.equals(cookie.getName())) {
          try {
            String decodedValue = URLDecoder.decode(cookie.getValue(), StandardCharsets.UTF_8);
            return objectMapper.readValue(decodedValue, new TypeReference<List<Long>>() {
            });
          } catch (JsonProcessingException e) {
            // 파싱 실패 시 빈 리스트 반환
            return new ArrayList<>();
          }
        }
      }
    }
    return new ArrayList<>();
  }

  private void saveRecentItemsCookie(HttpServletResponse response, List<Long> itemIds) {
    try {
      String jsonValue = objectMapper.writeValueAsString(itemIds);
      String encodedValue = URLEncoder.encode(jsonValue, StandardCharsets.UTF_8);

      Cookie cookie = new Cookie(COOKIE_NAME, encodedValue);
      cookie.setPath("/");
      cookie.setMaxAge(COOKIE_MAX_AGE);
      cookie.setHttpOnly(true); // XSS 방지
      response.addCookie(cookie);
    } catch (JsonProcessingException e) {
      // 쿠키 생성 실패 시 무시
    }
  }
}