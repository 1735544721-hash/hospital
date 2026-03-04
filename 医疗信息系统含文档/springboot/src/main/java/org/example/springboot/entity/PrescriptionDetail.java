package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("prescription_detail")
@Schema(description = "处方明细实体类")
public class PrescriptionDetail {
    @TableId(type = IdType.AUTO)
    @Schema(description = "明细ID")
    private Long id;
    
    @Schema(description = "处方ID")
    private Long prescriptionId;
    
    @Schema(description = "药品ID")
    private Long medicineId;
    
    @Schema(description = "用量")
    private String dosage;
    
    @Schema(description = "频次(一日三次/每日一次)")
    private String frequency;
    
    @Schema(description = "用药天数")
    private Integer days;
    
    @Schema(description = "数量")
    private Integer quantity;
    
    @Schema(description = "用法(口服/外用等)")
    @TableField("`usage`")
    private String usage;
    
    @Schema(description = "备注")
    private String notes;
    
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
    
    @TableField(exist = false)
    @Schema(description = "药品信息")
    private Medicine medicine;
} 