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
@TableName("schedule")
@Schema(description = "排班实体类")
public class Schedule {
    @TableId(type = IdType.AUTO)
    @Schema(description = "排班ID")
    private Long id;
    
    @Schema(description = "医生ID")
    private Long doctorId;
    
    @Schema(description = "排班日期")
    private LocalDate scheduleDate;
    
    @Schema(description = "时间段(上午/下午/晚上)")
    private String timeSlot;
    
    @Schema(description = "最大接诊人数")
    private Integer maxPatients;
    
    @Schema(description = "当前预约人数")
    private Integer currentPatients;
    
    @Schema(description = "状态(0:停诊,1:正常)")
    private Integer status;
    
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
    
    @TableField(exist = false)
    @Schema(description = "医生信息")
    private Doctor doctor;
    
    @TableField(exist = false)
    @Schema(description = "剩余可预约数")
    private Integer remainingQuota;
    
    public Integer getRemainingQuota() {
        if (maxPatients == null || currentPatients == null) {
            return 0; // 如果最大人数或当前人数为null，则返回0
        }
        return maxPatients - currentPatients;
    }
} 