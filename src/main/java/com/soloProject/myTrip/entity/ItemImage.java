package com.soloProject.myTrip.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class ItemImage {

  @Id
  @Column(name = "item_image_id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String imageUrl; // 이미지 조회 경로

  @Column(length = 500)
  private String imageDescription; // 이미지 설명

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "item_id")
  private Item item;
}