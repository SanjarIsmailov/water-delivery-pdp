package uz.pdp.waterdeliverypdp.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.waterdeliverypdp.entity.District;

import java.util.UUID;

public interface DistrictRepository extends JpaRepository<District, UUID> {
}