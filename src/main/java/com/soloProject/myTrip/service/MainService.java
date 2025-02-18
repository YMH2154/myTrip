package com.soloProject.myTrip.service;

import com.soloProject.myTrip.entity.Item;
import com.soloProject.myTrip.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MainService {

    private final ItemRepository itemRepository;

    @Transactional(readOnly = true)
    public List<Item> getRecommendedItems() {
        return itemRepository.findRandomItems(8);
    }

    @Transactional(readOnly = true)
    public List<Item> getConfirmedItems() {
        PageRequest pageRequest = PageRequest.of(0, 8);
        return itemRepository.findTop8ByParticipantsCondition(pageRequest);
    }

}
