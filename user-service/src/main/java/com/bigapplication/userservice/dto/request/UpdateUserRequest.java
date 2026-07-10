package com.bigapplication.userservice.dto.request;

import com.bigapplication.userservice.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import java.util.Set;
import lombok.Data;

@Data
public class UpdateUserRequest {

    @Size(max = 50)
    private String username;

    @Email
    @Size(max = 120)
    private String email;

    @Size(min = 8, max = 72)
    private String password;

    @Size(max = 100)
    private String firstName;

    @Size(max = 100)
    private String lastName;

    private Boolean enabled;

    private Set<Role> roles;
}
