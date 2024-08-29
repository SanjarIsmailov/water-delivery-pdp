package com.example.demo.repo;

import com.example.demo.entity.Order;
import com.example.demo.entity.enums.OrderStatus;
import com.example.demo.projections.OrderProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    List<OrderProjection> findAllByStartTimeAndOrderStatus(LocalDateTime localDateTime, OrderStatus orderStatus);

    List<Order> findByCourierId(UUID courierId);

}