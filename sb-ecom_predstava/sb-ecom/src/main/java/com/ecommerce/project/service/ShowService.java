package com.ecommerce.project.service;

import com.ecommerce.project.payload.ShowDTO;
import com.ecommerce.project.payload.ShowResponse;
import jakarta.validation.Valid;

public interface ShowService {

    
    ShowDTO createShow(@Valid ShowDTO showDTO, Long categoryId);

    ShowResponse getAllShow(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);

    ShowDTO deleteShow(Long showId);

    ShowDTO updateShow(@Valid ShowDTO showDTO, Long showId);
}