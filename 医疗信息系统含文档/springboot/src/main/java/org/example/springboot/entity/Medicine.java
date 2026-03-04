package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("medicine")
@Schema(description = "药品实体类")
public class Medicine {
    @TableId(type = IdType.AUTO)
    @Schema(description = "药品ID")
    private Long id;
    
    @Schema(description = "药品编码")
    private String medicineCode;
    
    @Schema(description = "药品名称")
    private String medicineName;
    
    @Schema(description = "规格")
    private String specification;
    
    @Schema(description = "剂型")
    private String dosageForm;
    
    @Schema(description = "生产厂家")
    private String manufacturer;
    
    @Schema(description = "类别(处方药/非处方药)")
    private String category;
    
    @Schema(description = "分类ID")
    private Long categoryId;
    
    @Schema(description = "单价")
    private BigDecimal price;
    
    @Schema(description = "库存量")
    private Integer stock;
    
    @Schema(description = "状态(0:下架,1:上架)")
    private Integer status;
    
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
    
    @Schema(description = "使用说明")
    private String instructions;
    

} 