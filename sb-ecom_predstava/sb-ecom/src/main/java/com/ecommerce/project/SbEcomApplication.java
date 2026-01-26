package com.ecommerce.project;

import com.ecommerce.project.model.Hall;
import com.ecommerce.project.model.Seat;
import com.ecommerce.project.repositories.HallRepository;
import com.ecommerce.project.repositories.SeatRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@EnableCaching
@EnableScheduling
@SpringBootApplication
public class SbEcomApplication {

	public static void main(String[] args) {

		SpringApplication.run(SbEcomApplication.class, args);
	}



}
