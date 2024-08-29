package com.example.demo.repo;

import com.example.demo.entity.Courier;
import com.example.demo.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CourierRepository extends JpaRepository<Courier, UUID> {
    Courier findByPhoneNumber(String phone);
}