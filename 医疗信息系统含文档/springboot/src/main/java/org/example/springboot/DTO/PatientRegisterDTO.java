package org.example.springboot.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "患者注册DTO")
public class PatientRegisterDTO {
    @Schema(description = "用户信息")
    private UserDTO user;
    
    @Schema(description = "患者信息")
    private PatientDTO patient;
    
    @Schema(description = "确认密码")
    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;
    
    @Data
    @Schema(description = "用户信息DTO")
    public static class UserDTO {
        @Schema(description = "用户名")
        @NotBlank(message = "用户名不能为空")
        @Size(min = 3, max = 50, message = "用户名长度必须在3到50个字符之间")
        private String username;
        
        @Schema(description = "密码")
        @NotBlank(message = "密码不能为空")
        @Size(min = 6, max = 100, message = "密码长度必须在6到100个字符之间")
        private String password;
        
        @Schema(description = "邮箱")
        @NotBlank(message = "邮箱不能为空")
        @Email(message = "邮箱格式不正确")
        private String email;
        
        @Schema(description = "角色编码")
        private String roleCode = "PATIENT"; // 默认为患者
        
        // 以下字段会从患者信息中同步
        private String name;
        private String sex;
        private String phone;
    }
    
    @Data
    @Schema(description = "患者信息DTO")
    public static class PatientDTO {
        @Schema(description = "姓名")
        @NotBlank(message = "姓名不能为空")
        private String name;
        
        @Schema(description = "性别")
        @NotBlank(message = "性别不能为空")
        private String sex;
        
        @Schema(description = "出生日期")
        private LocalDate birthday;
        
        @Schema(description = "身份证号")
        @Pattern(regexp = "^\\d{17}[\\dX]$", message = "身份证号格式不正确")
        private String idCard;
        
        @Schema(description = "手机号")
        @NotBlank(message = "手机号不能为空")
        @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
        private String phone;
        
        @Schema(description = "住址")
        private String address;
        
        @Schema(description = "病史")
        private String medicalHistory;
        
        @Schema(description = "过敏史")
        private String allergies;
    }
} 