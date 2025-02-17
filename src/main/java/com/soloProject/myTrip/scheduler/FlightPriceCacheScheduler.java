package com.soloProject.myTrip.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@Slf4j
@RequiredArgsConstructor
public class FlightPriceCacheScheduler {

  private final RedisTemplate<String, Object> redisTemplate;

  @Scheduled(cron = "0 0 0 * * *")
  public void clearOldCaches() {
    try {
      Set<String> keys = redisTemplate.keys("flight:*");
      if (keys != null && !keys.isEmpty()) {
        redisTemplate.delete(keys);
        log.info("Cleared {} cached flight price entries", keys.size());
      }
    } catch (Exception e) {
      log.error("Cache clearing failed", e);
    }
  }
}