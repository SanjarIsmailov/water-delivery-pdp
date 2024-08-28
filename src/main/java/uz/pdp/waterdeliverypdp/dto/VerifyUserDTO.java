package uz.pdp.waterdeliverypdp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VerifyUserDTO {

    private UUID id;
    private String address;
    private Double longitude;
    private Double latitude;
    private UUID district;

}
