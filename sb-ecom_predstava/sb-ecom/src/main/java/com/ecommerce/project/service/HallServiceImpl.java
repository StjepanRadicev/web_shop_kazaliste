package com.ecommerce.project.service;

import com.ecommerce.project.model.Hall;
import com.ecommerce.project.payload.CategoryDTO;
import com.ecommerce.project.payload.CategoryResponse;
import com.ecommerce.project.payload.HallDTO;
import com.ecommerce.project.payload.HallResponse;
import com.ecommerce.project.repositories.HallRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HallServiceImpl implements HallService{

    @Autowired
    private HallRepository hallRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public HallResponse getAllHall() {

        List<Hall> hallList = hallRepository.findAll();

        List<HallDTO> hallDTOS = hallList.stream()
                .map(hall -> modelMapper.map(hall, HallDTO.class))
                .toList();

        HallResponse hallResponse = new HallResponse();
        hallResponse.setContent(hallDTOS);

        return hallResponse;
    }
}
