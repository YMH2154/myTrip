package com.soloProject.myTrip.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "item_image")
@Getter
@Setter
public class TouristAttraction {

  @Id
  @Column(name = "item_image_id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(length = 30)
  private String name; //관광지 명

  private String imageUrl; // 관광지 이미지(url)

  @Column(length = 500)
  private String description; // 관광지 설명

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "schedule_id")
  private Schedule schedule;
}