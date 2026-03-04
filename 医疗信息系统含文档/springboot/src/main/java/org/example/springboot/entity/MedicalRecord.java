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
@TableName("medical_record")
@Schema(description = "就诊记录实体类")
public class MedicalRecord {
    @TableId(type = IdType.AUTO)
    @Schema(description = "记录ID")
    private Long id;
    
    @Schema(description = "记录编号")
    private String recordNo;
    
    @Schema(description = "患者ID")
    private Long patientId;
    
    @Schema(description = "医生ID")
    private Long doctorId;
    
    @Schema(description = "预约ID")
    private Long appointmentId;
    
    @Schema(description = "诊断结果")
    private String diagnosis;
    
    @Schema(description = "治疗方案")
    private String treatment;
    
    @Schema(description = "就诊日期")
    private LocalDate recordDate;
    
    @Schema(description = "医生备注")
    private String notes;
    
    @Schema(description = "随访日期")
    private LocalDate followUp;
    
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
    @Schema(description = "预约信息")
    private Appointment appointment;
}