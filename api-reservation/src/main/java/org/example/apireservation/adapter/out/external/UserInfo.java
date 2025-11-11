package org.example.apireservation.adapter.out.external;

import lombok.*;
import org.example.common.user.UserRole;

@Getter
@Setter
public class UserInfo {
    private Long id;
    private Long companyId; // 소속 기업 ID
    private UserRole role;
}