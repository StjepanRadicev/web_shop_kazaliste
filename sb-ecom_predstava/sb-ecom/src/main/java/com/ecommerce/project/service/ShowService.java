package com.ecommerce.project.service;

import com.ecommerce.project.payload.ShowDTO;
import com.ecommerce.project.payload.ShowResponse;
import jakarta.validation.Valid;

import java.util.List;

public interface ShowService {

    
    ShowDTO createShow(@Valid ShowDTO showDTO, Long categoryId);

    ShowResponse getAllShow(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);

    List<ShowDTO> getAllShowsWithoutPagination();

    ShowDTO deleteShow(Long showId);

    ShowDTO updateShow(@Valid ShowDTO showDTO, Long showId);
}