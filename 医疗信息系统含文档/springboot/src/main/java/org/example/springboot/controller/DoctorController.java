package org.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.example.springboot.common.Result;
import org.example.springboot.entity.Doctor;
import org.example.springboot.entity.User;
import org.example.springboot.service.DoctorService;
import org.example.springboot.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "医生管理接口")
@RestController
@RequestMapping("/doctor")
public class DoctorController {
    private static final Logger LOGGER = LoggerFactory.getLogger(DoctorController.class);
    
    @Resource
    private DoctorService doctorService;
    
    @Resource
    private UserService userService;
    
    @Operation(summary = "创建医生")
    @PostMapping
    public Result<?> createDoctor(@RequestBody Doctor doctor) {
        LOGGER.info("创建医生: {}", doctor.getName());
        Doctor createdDoctor = doctorService.createDoctor(doctor);
        return Result.success(createdDoctor);
    }
    
    @Operation(summary = "创建医生并同时创建用户")
    @PostMapping("/with-user")
    public Result<?> createDoctorWithUser(@RequestBody Map<String, Object> params) {
        User user = new User();
        Doctor doctor = new Doctor();
        
        // 从params中获取用户信息
        Map<String, Object> userMap = (Map<String, Object>) params.get("user");
        user.setUsername((String) userMap.get("username"));
        user.setPassword((String) userMap.get("password"));
        user.setEmail((String) userMap.get("email"));
        user.setPhone((String) userMap.get("phone"));
        user.setName((String) userMap.get("name"));
        user.setRoleCode("DOCTOR"); // 设置为医生角色
        
        // 从params中获取医生信息
        Map<String, Object> doctorMap = (Map<String, Object>) params.get("doctor");
        doctor.setName((String) doctorMap.get("name"));
        doctor.setDepartmentId(Long.valueOf(doctorMap.get("departmentId").toString()));
        doctor.setTitle((String) doctorMap.get("title"));
        doctor.setExpertise((String) doctorMap.get("expertise"));
        doctor.setIntroduction((String) doctorMap.get("introduction"));
        doctor.setStatus(Integer.valueOf(doctorMap.get("status").toString()));
        
        LOGGER.info("创建医生并同时创建用户: 医生={}, 用户={}", doctor.getName(), user.getUsername());
        Doctor createdDoctor = doctorService.createDoctorWithUser(doctor, user);
        return Result.success(createdDoctor);
    }
    
    @Operation(summary = "更新医生信息")
    @PutMapping("/{id}")
    public Result<?> updateDoctor(@PathVariable Long id, @RequestBody Doctor doctor) {
        LOGGER.info("更新医生信息: id={}, name={}", id, doctor.getName());
        doctorService.updateDoctor(id, doctor);
        return Result.success();
    }
    
    @Operation(summary = "获取医生详情")
    @GetMapping("/{id}")
    public Result<?> getDoctorById(@PathVariable Long id) {
        Doctor doctor = doctorService.getDetailById(id);
        return Result.success(doctor);
    }
    
    @Operation(summary = "通过用户ID获取医生信息")
    @GetMapping("/user/{userId}")
    public Result<?> getDoctorByUserId(@PathVariable Long userId) {
        LOGGER.info("通过用户ID获取医生信息: userId={}", userId);
        Doctor doctor = doctorService.getDoctorByUserId(userId);
        return Result.success(doctor);
    }
    
    @Operation(summary = "获取所有医生列表")
    @GetMapping("/list")
    public Result<?> getAllDoctors() {
        LOGGER.info("获取所有医生列表");
        List<Doctor> doctors = doctorService.getAllDoctors();
        return Result.success(doctors);
    }
    
    @Operation(summary = "获取所有医生列表(供前端下拉框使用)")
    @GetMapping
    public Result<?> getDoctorList() {
        LOGGER.info("获取医生列表供下拉框使用");
        List<Doctor> doctors = doctorService.getAllDoctors();
        return Result.success(doctors);
    }
    
    @Operation(summary = "分页查询医生")
    @GetMapping("/page")
    public Result<?> getDoctorsByPage(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String doctorNo,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer currentPage,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<Doctor> page = doctorService.getDoctorsByPage(name, doctorNo, departmentId, title, status, currentPage, size);
        return Result.success(page);
    }
    
    @Operation(summary = "获取科室下的医生")
    @GetMapping("/department/{departmentId}")
    public Result<?> getDoctorsByDepartment(@PathVariable Long departmentId) {
        List<Doctor> doctors = doctorService.getDoctorsByDepartment(departmentId);
        return Result.success(doctors);
    }
    
    @Operation(summary = "删除医生")
    @DeleteMapping("/{id}")
    public Result<?> deleteDoctor(@PathVariable Long id) {
        LOGGER.info("删除医生: id={}", id);
        doctorService.deleteDoctor(id);
        return Result.success();
    }
    
    @Operation(summary = "更新医生状态")
    @PutMapping("/{id}/status")
    public Result<?> updateDoctorStatus(@PathVariable Long id, @RequestParam Integer status) {
        LOGGER.info("更新医生状态: id={}, status={}", id, status);
        doctorService.updateDoctorStatus(id, status);
        return Result.success();
    }
    
    @Operation(summary = "绑定用户到医生")
    @PutMapping("/{doctorId}/bind/{userId}")
    public Result<?> bindUserToDoctor(@PathVariable Long doctorId, @PathVariable Long userId) {
        LOGGER.info("绑定用户到医生: doctorId={}, userId={}", doctorId, userId);
        doctorService.bindUserToDoctor(doctorId, userId);
        return Result.success();
    }
    
    @Operation(summary = "解绑医生关联的用户")
    @PutMapping("/{doctorId}/unbind")
    public Result<?> unbindUserFromDoctor(@PathVariable Long doctorId) {
        LOGGER.info("解绑医生关联的用户: doctorId={}", doctorId);
        doctorService.unbindUserFromDoctor(doctorId);
        return Result.success();
    }
} 