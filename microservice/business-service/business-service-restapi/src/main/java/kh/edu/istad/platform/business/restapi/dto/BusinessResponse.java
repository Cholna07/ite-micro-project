
package kh.edu.istad.platform.business.restapi.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BusinessResponse {

    private String businessId;
    private String legalName;
    private String displayName;
    private String email;
    private String phoneNumber;
    private boolean active;
}
