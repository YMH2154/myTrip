package com.soloProject.myTrip.config;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import org.hibernate.validator.constraints.URL;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import com.soloProject.myTrip.dto.FlightOfferDto;
import java.util.List;

@Configuration
public class RedisConfig {

  @Value("${spring.redis.host}")
  private String redisHost;

  @Value("${spring.redis.port}")
  private int redisPort;

  @Bean
  public RedisConnectionFactory redisConnectionFactory() {
    return new LettuceConnectionFactory(redisHost, redisPort);
  }

  @Bean
  public ObjectMapper objectMapper() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    return mapper;
  }

  @Bean
  public RedisTemplate<String, List<FlightOfferDto>> flightOffersRedisTemplate(RedisConnectionFactory connectionFactory) {
    RedisTemplate<String, List<FlightOfferDto>> template = new RedisTemplate<>();
    template.setConnectionFactory(connectionFactory);
    template.setKeySerializer(new StringRedisSerializer());

    // List<FlightOfferDto> 타입을 나타내는 JavaType을 생성
    JavaType javaType = objectMapper().getTypeFactory()
        .constructParametricType(List.class, FlightOfferDto.class);

    // Jackson2JsonRedisSerializer를 List<FlightOfferDto> 타입으로 설정
    Jackson2JsonRedisSerializer<List<FlightOfferDto>> valueSerializer = 
        new Jackson2JsonRedisSerializer<>(javaType);
    valueSerializer.setObjectMapper(objectMapper());

    template.setValueSerializer(valueSerializer);
    return template;
  }

  @Bean
  public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
    RedisTemplate<String, Object> template = new RedisTemplate<>();
    template.setConnectionFactory(connectionFactory);
    template.setKeySerializer(new StringRedisSerializer());
    
    Jackson2JsonRedisSerializer<Object> valueSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
    valueSerializer.setObjectMapper(objectMapper());
    
    template.setValueSerializer(valueSerializer);
    return template;
  }
}