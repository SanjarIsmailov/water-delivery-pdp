package uz.pdp.waterdeliverypdp.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;
import uz.pdp.waterdeliverypdp.entity.Order;
import uz.pdp.waterdeliverypdp.entity.enums.OrderStatus;
import uz.pdp.waterdeliverypdp.projections.OrderProjection;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
    List<OrderProjection> findAllByStartTimeAndOrderStatus(LocalDateTime localDateTime, OrderStatus orderStatus);

    List<Order> findByCourierId(UUID courierId);
}