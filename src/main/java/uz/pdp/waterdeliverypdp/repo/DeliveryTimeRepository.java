package uz.pdp.waterdeliverypdp.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.waterdeliverypdp.entity.DeliveryTime;

public interface DeliveryTimeRepository extends JpaRepository<DeliveryTime, Integer> {
}