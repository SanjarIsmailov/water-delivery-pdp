package uz.pdp.waterdeliverypdp.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.waterdeliverypdp.entity.Courier;

import java.util.UUID;

public interface CourierRepository extends JpaRepository<Courier, UUID> {
    Courier findByPhoneNumber(String phoneNumber);
}
