package com.eduassess.dto;

import com.eduassess.entity.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDto {
    private String id;
    private String name;
    private String email;
    private Role role;
    private String grade;
    private String phone;
    private String joinedAt;
}
