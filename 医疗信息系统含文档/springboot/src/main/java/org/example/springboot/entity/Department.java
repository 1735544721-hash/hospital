package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("department")
@Schema(description = "科室实体类")
public class Department {
    @TableId(type = IdType.AUTO)
    @Schema(description = "科室ID")
    private Long id;
    
    @Schema(description = "科室名称")
    private String deptName;
    
    @Schema(description = "科室编码")
    private String deptCode;
    
    @Schema(description = "科室描述")
    private String description;
    
    @Schema(description = "负责人ID")
    private Long directorId;
    
    @Schema(description = "状态(0:禁用,1:启用)")
    private Integer status;
    
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
    
    @TableField(exist = false)
    @Schema(description = "负责人")
    private Doctor director;
} 