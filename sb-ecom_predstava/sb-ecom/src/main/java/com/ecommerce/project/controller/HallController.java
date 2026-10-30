package com.ecommerce.project.controller;

import com.ecommerce.project.config.AppConstants;
import com.ecommerce.project.payload.HallResponse;
import com.ecommerce.project.payload.ShowResponse;
import com.ecommerce.project.service.HallService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Validated
@RequiredArgsConstructor
public class HallController
{
    @Autowired
    private HallService hallService;

    @GetMapping("/public/hall")
    public ResponseEntity<HallResponse> getAllHall(
    ){

        HallResponse hallResponse = hallService.getAllHall();

        return new ResponseEntity<>(hallResponse, HttpStatus.OK);
    }
}
