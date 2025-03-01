package com.soloProject.myTrip.service;

import com.soloProject.myTrip.dto.ItemFormDto;
import com.soloProject.myTrip.entity.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RecentViewService {
  private final RedisTemplate<String, Object> redisTemplate;
  private final ItemService itemService;
  private static final String KEY_PREFIX = "recent_view:";
  private static final int MAX_SIZE = 3; // 최대 3개까지만 저장

  public void addRecentView(String sessionId, Long itemId) {
    String key = KEY_PREFIX + sessionId;

    // 현재 아이템이 이미 있다면 삭제 (중복 방지)
    redisTemplate.opsForList().remove(key, 0, itemId);

    // 새로운 아이템을 리스트 앞에 추가
    redisTemplate.opsForList().leftPush(key, itemId);

    // 리스트 크기를 MAX_SIZE로 유지
    redisTemplate.opsForList().trim(key, 0, MAX_SIZE - 1);

    // 24시간 후 만료
    redisTemplate.expire(key, 24, TimeUnit.HOURS);
  }

  public List<ItemFormDto> getRecentItems(String sessionId, Long currentItemId) {
    String key = KEY_PREFIX + sessionId;
    List<Object> itemIds = redisTemplate.opsForList().range(key, 0, -1);
    List<ItemFormDto> recentItems = new ArrayList<>();

    if (itemIds != null) {
      for (Object id : itemIds) {
        Long itemId = Long.valueOf(id.toString());
        // 현재 보고 있는 상품은 제외
        if (!itemId.equals(currentItemId)) {
          try {
            ItemFormDto item = itemService.getItem(itemId);
            if (item != null) {
              recentItems.add(item);
            }
          } catch (Exception e) {
            // 상품이 삭제되었거나 찾을 수 없는 경우 무시
          }
        }
      }
    }

    return recentItems;
  }
}