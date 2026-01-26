package com.ecommerce.project.controller;

import com.ecommerce.project.payload.SeatDTO;
import com.ecommerce.project.service.PerformanceSeatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@Validated
public class PerformanceSeatController {

    @Autowired
    private PerformanceSeatService performanceSeatService;

    @GetMapping("/public/performance/{performanceId}/seat")
    public ResponseEntity<List<SeatDTO>> getSeat(@PathVariable Long performanceId) {
        List<SeatDTO> seatDTO = performanceSeatService.getSeat(performanceId);

        return new ResponseEntity<List<SeatDTO>>(seatDTO, HttpStatus.OK);
    }
}
