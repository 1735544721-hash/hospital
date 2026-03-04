package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("prescription")
@Schema(description = "处方实体类")
public class Prescription {
    @TableId(type = IdType.AUTO)
    @Schema(description = "处方ID")
    private Long id;
    
    @Schema(description = "处方编号")
    private String prescriptionNo;
    
    @Schema(description = "患者ID")
    private Long patientId;
    
    @Schema(description = "医生ID")
    private Long doctorId;
    
    @Schema(description = "就诊记录ID")
    private Long recordId;
    
    @Schema(description = "处方日期")
    private LocalDate prescriptionDate;
    
    @Schema(description = "诊断")
    private String diagnosis;
    
    @Schema(description = "备注")
    private String notes;
    
    @Schema(description = "状态(0:未取药,1:已取药)")
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
    @Schema(description = "就诊记录信息")
    private MedicalRecord medicalRecord;
    
    @TableField(exist = false)
    @Schema(description = "处方明细列表")
    private List<PrescriptionDetail> details;
} 