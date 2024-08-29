package com.example.demo.repo;

import com.example.demo.entity.DeliveryTime;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryTimeRepository extends JpaRepository<DeliveryTime, Integer> {
}