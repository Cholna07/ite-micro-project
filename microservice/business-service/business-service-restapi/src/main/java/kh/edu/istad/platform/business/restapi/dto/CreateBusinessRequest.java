
package kh.edu.istad.platform.business.restapi.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateBusinessRequest {

    @NotBlank(message = "Legal name is required")
    private String legalName;

    @NotBlank(message = "Display name is required")
    private String displayName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Phone number is required")
    private String phoneNumber;
}
