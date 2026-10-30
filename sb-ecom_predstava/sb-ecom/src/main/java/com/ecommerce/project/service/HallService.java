package com.ecommerce.project.service;

import com.ecommerce.project.payload.HallResponse;
import org.springframework.stereotype.Service;

@Service
public interface HallService {
    HallResponse getAllHall();
}
