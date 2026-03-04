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
@TableName("appointment")
@Schema(description = "预约实体类")
public class Appointment {
    @TableId(type = IdType.AUTO)
    @Schema(description = "预约ID")
    private Long id;
    
    @Schema(description = "患者ID")
    private Long patientId;
    
    @Schema(description = "医生ID")
    private Long doctorId;
    
    @Schema(description = "排班ID")
    private Long scheduleId;
    
    @Schema(description = "预约编号")
    private String appointmentNo;
    
    @Schema(description = "预约日期")
    private LocalDate appointmentDate;
    
    @Schema(description = "时间段(上午/下午/晚上)")
    private String timeSlot;
    
    @Schema(description = "症状描述")
    private String symptoms;
    
    @Schema(description = "状态(0:取消,1:待就诊,2:已就诊)")
    private Integer status;
    
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
    
    @TableField(exist = false)
    @Schema(description = "患者信息")
    private Patient patient;
    
    @TableField(exist = false)
    @Schema(description = "医生信息")
    private Doctor doctor;
    
    @TableField(exist = false)
    @Schema(description = "排班信息")
    private Schedule schedule;
} 