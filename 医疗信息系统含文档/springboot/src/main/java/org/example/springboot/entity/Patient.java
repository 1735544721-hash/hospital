package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("patient")
@Schema(description = "患者实体类")
public class Patient {
    @TableId(type = IdType.AUTO)
    @Schema(description = "患者ID")
    private Long id;
    
    @Schema(description = "用户ID")
    private Long userId;
    
    @Schema(description = "患者编号")
    private String patientNo;
    
    @Schema(description = "姓名")
    private String name;
    
    @Schema(description = "身份证号")
    private String idCard;
    
    @Schema(description = "出生日期")
    private LocalDate birthday;
    
    @Schema(description = "性别")
    private String sex;
    
    @Schema(description = "联系电话")
    private String phone;
    
    @Schema(description = "住址")
    private String address;
    
    @Schema(description = "病史")
    private String medicalHistory;
    
    @Schema(description = "过敏史")
    private String allergies;
    
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
    
    @TableField(exist = false)
    @Schema(description = "用户信息")
    private User user;
} 