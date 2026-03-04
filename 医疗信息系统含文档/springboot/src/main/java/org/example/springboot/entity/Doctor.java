package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;


import java.time.LocalDateTime;

@Data
@TableName("doctor")
@Schema(description = "医生实体类")
public class Doctor {
    @TableId(type = IdType.AUTO)
    @Schema(description = "医生ID")
    private Long id;
    
    @Schema(description = "医生编号")
    private String doctorNo;
    
    @NotBlank(message = "医生姓名不能为空")
    @Size(min = 2, max = 50, message = "医生姓名长度必须在2到50个字符之间")
    @Schema(description = "医生姓名")
    private String name;
    
    @NotNull(message = "所属科室不能为空")
    @Schema(description = "所属科室ID")
    private Long departmentId;
    
    @Schema(description = "职称")
    private String title;
    
    @Schema(description = "专长")
    private String expertise;
    
    @Schema(description = "简介")
    private String introduction;
    
    @Schema(description = "关联用户ID")
    private Long userId;
    
    @Schema(description = "状态: 1-在职, 0-离职")
    private Integer status;
    
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
    
    @TableField(exist = false)
    @Schema(description = "科室信息")
    private Department department;
    
    @TableField(exist = false)
    @Schema(description = "用户信息")
    private User user;
} 