package com.ecommerce.project.repositories;

import com.ecommerce.project.model.Category;
import com.ecommerce.project.model.Hall;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HallRepository  extends JpaRepository<Hall, Long> {

}
