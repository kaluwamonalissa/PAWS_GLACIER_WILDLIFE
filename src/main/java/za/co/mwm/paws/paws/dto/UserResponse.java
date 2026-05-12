package za.co.mwm.paws.paws.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import za.co.mwm.paws.paws.domain.Role;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String username;
    private String fullName;
    private Role role;
    private String ward;
    private boolean active;
}

