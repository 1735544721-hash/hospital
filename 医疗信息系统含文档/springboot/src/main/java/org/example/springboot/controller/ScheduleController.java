package org.example.springboot.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.example.springboot.common.Result;
import org.example.springboot.entity.Schedule;
import org.example.springboot.service.ScheduleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "排班管理接口")
@RestController
@RequestMapping("/schedule")
public class ScheduleController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduleController.class);
    
    @Resource
    private ScheduleService scheduleService;
    
    @Operation(summary = "新增排班")
    @PostMapping
    public Result<?> createSchedule(@RequestBody Schedule schedule) {
        LOGGER.info("新增排班: doctorId={}, date={}, timeSlot={}", schedule.getDoctorId(), schedule.getScheduleDate(), schedule.getTimeSlot());
        Schedule newSchedule = scheduleService.createSchedule(schedule);
        return Result.success(newSchedule);
    }
    
    @Operation(summary = "批量新增排班")
    @PostMapping("/batch")
    public Result<?> batchCreateSchedule(@RequestBody List<Schedule> schedules) {
        LOGGER.info("批量新增排班: size={}", schedules.size());
        scheduleService.batchCreateSchedule(schedules);
        return Result.success();
    }
    
    @Operation(summary = "更新排班信息")
    @PutMapping("/{id}")
    public Result<?> updateSchedule(@PathVariable Long id, @RequestBody Schedule schedule) {
        LOGGER.info("更新排班信息: id={}", id);
        scheduleService.updateSchedule(id, schedule);
        return Result.success();
    }
    
    @Operation(summary = "获取排班详情")
    @GetMapping("/{id}")
    public Result<?> getScheduleById(@PathVariable Long id) {
        Schedule schedule = scheduleService.getScheduleById(id);
        return Result.success(schedule);
    }
    
    @Operation(summary = "分页查询排班列表")
    @GetMapping("/page")
    public Result<?> getSchedulesByPage(
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String timeSlot,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer currentPage,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(scheduleService.getSchedulesByPage(doctorId, startDate, endDate, timeSlot, status, currentPage, size));
    }
    
    @Operation(summary = "获取医生排班列表")
    @GetMapping("/doctor/{doctorId}")
    public Result<?> getSchedulesByDoctor(
            @PathVariable Long doctorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<Schedule> schedules = scheduleService.getSchedulesByDoctor(doctorId, startDate, endDate);
        return Result.success(schedules);
    }
    
    @Operation(summary = "获取某日排班列表")
    @GetMapping("/date/{date}")
    public Result<?> getSchedulesByDate(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<Schedule> schedules = scheduleService.getSchedulesByDate(date);
        return Result.success(schedules);
    }
    
    @Operation(summary = "更新排班状态")
    @PutMapping("/{id}/status")
    public Result<?> updateScheduleStatus(@PathVariable Long id, @RequestParam Integer status) {
        LOGGER.info("更新排班状态: id={}, status={}", id, status);
        scheduleService.updateScheduleStatus(id, status);
        return Result.success();
    }
    
    @Operation(summary = "删除排班")
    @DeleteMapping("/{id}")
    public Result<?> deleteSchedule(@PathVariable Long id) {
        LOGGER.info("删除排班: id={}", id);
        scheduleService.deleteSchedule(id);
        return Result.success();
    }
} 