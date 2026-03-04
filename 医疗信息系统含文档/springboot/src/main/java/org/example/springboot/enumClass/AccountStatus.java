package org.example.springboot.enumClass;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "账户状态枚举")
public enum AccountStatus {
    @Schema(description = "禁用") DISABLED(0),
    @Schema(description = "启用") ENABLED(1),
    @Schema(description = "待审核") PENDING_REVIEW(2),
    @Schema(description = "审核通过") REVIEW_SUCCESS(3),
    @Schema(description = "审核不通过") REVIEW_FAILED(4);

    private final int value;

    AccountStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
    
    public static AccountStatus fromValue(int value) {
        for (AccountStatus status : AccountStatus.values()) {
            if (status.getValue() == value) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的账户状态值: " + value);
    }
}