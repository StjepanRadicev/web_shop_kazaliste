package com.ecommerce.project.service;

import com.ecommerce.project.model.Performance;
import com.ecommerce.project.model.PerformanceSeat;
import com.ecommerce.project.model.Seat;
import com.ecommerce.project.payload.CartDTO;
import com.ecommerce.project.payload.PerformanceSeatDTO;
import com.ecommerce.project.payload.SeatDTO;
import com.ecommerce.project.repositories.PerformanceSeatRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PerformanceSeatImpl implements PerformanceSeatService{

    @Autowired
    private PerformanceSeatRepository performanceSeatRepository;

    @Autowired
    private ModelMapper modelMapper;


    @Override
    public List<SeatDTO> getSeat(Long performanceId) {

        List<PerformanceSeat> seatList = performanceSeatRepository.findByPerformanceId(performanceId);

        return seatList.stream().map(
                item -> {
                    SeatDTO seatDTO = modelMapper.map(item.getSeat(), SeatDTO.class);
                    seatDTO.setPerformanceSeatId(item.getPerformanceSeatId());
                    seatDTO.setStatus(item.getStatus());
                    seatDTO.setPrice(item.getPrice());
                    return seatDTO;
                }
        ).collect(Collectors.toList());
    }
}
