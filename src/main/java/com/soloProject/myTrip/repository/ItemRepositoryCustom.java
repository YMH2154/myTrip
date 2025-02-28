package com.soloProject.myTrip.repository;

import com.soloProject.myTrip.dto.ItemSearchDto;
import com.soloProject.myTrip.entity.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ItemRepositoryCustom {
    Page<Item> getAdminItemPage(ItemSearchDto itemSearchDto, Pageable pageable);
    Page<Item> findItemsByCategory(String link, Pageable pageable);
}
