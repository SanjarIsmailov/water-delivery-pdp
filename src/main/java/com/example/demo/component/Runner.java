package com.example.demo.component;

import com.example.demo.entity.Courier;
import com.example.demo.entity.DeliveryTime;
import com.example.demo.entity.District;
import com.example.demo.repo.CourierRepository;
import com.example.demo.repo.DeliveryTimeRepository;
import com.example.demo.repo.DistrictRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class Runner implements CommandLineRunner {

    private final DeliveryTimeRepository deliveryTimeRepository;
    @Value("${spring.jpa.hibernate.ddl-auto}")
    private String ddl;
    private final DistrictRepository districtRepository;
    private final CourierRepository courierRepository;

    @Override
    public void run(String... args) throws Exception {
        if (ddl.equals("create")) {
            deliveryTimeRepository.saveAll(List.of(
                    new DeliveryTime(1, LocalTime.of(9, 0), LocalTime.of(12, 0)),
                    new DeliveryTime(2, LocalTime.of(13, 0), LocalTime.of(18, 0)),
                    new DeliveryTime(3, LocalTime.of(18, 0), LocalTime.of(23, 59))
            ));
            districtRepository.saveAll(
                    List.of(
                            new District(null, "Shayxontohur"),
                            new District(null, "Olmazor"),
                            new District(null, "Yunusobod"),
                            new District(null, "Mirobod"),
                            new District(null, "Chilonzor"),
                            new District(null, "Yashnobod"),
                            new District(null, "Mirzo ulugbek")
                    )
            );
            courierRepository.saveAll(List.of(
                    new Courier(null,"Eshmat","Damas","01|712PPP", "+998991234567"),
                    new Courier(null,"Toshmat","Labo","01|A717AA", "+998931234567"),
                    new Courier(null,"Hikmat","Gruzovik","01|701 UZB", "+998941234567"),
                    new Courier(UUID.randomUUID(), "Sanjar", "Gelik", "01|B888BB", "+998935592299")
            ));
        }

    }
}
