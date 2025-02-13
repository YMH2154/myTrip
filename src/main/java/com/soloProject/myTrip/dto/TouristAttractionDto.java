package com.soloProject.myTrip.dto;

import com.soloProject.myTrip.entity.TouristAttraction;
import lombok.Getter;
import lombok.Setter;
import org.modelmapper.ModelMapper;

@Getter
@Setter
public class TouristAttractionDto {
  private Long id;
  private String name;
  private String imageUrl;
  private String description;

  public static ModelMapper modelMapper = new ModelMapper();

  public static TouristAttractionDto of(TouristAttraction touristAttraction){
    return modelMapper.map(touristAttraction, TouristAttractionDto.class);
  }
}