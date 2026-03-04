package org.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.example.springboot.common.Result;
import org.example.springboot.entity.Doctor;
import org.example.springboot.entity.MedicalRecord;
import org.example.springboot.entity.Patient;
import org.example.springboot.entity.User;
import org.example.springboot.exception.ServiceException;
import org.example.springboot.mapper.UserMapper;
import org.example.springboot.service.DoctorService;
import org.example.springboot.service.MedicalRecordService;
import org.example.springboot.service.PatientService;
import org.example.springboot.util.JwtTokenUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Tag(name = "就诊记录管理接口")
@RestController
@RequestMapping("/medical-record")
public class MedicalRecordController {
    private static final Logger LOGGER = LoggerFactory.getLogger(MedicalRecordController.class);
    
    @Resource
    private MedicalRecordService medicalRecordService;
    @Autowired
    private PatientService patientService;
    @Resource
    private DoctorService doctorService;
    @Autowired
    private UserMapper userMapper;

    @Operation(summary = "新增就诊记录")
    @PostMapping
    public Result<?> createMedicalRecord(@RequestBody MedicalRecord medicalRecord) {
        LOGGER.info("新增就诊记录: patientId={}, doctorId={}", medicalRecord.getPatientId(), medicalRecord.getDoctorId());
        
        // 检查权限，如果是医生，只能创建自己的就诊记录
        checkDoctorPermission(medicalRecord);
        
        MedicalRecord newRecord = medicalRecordService.createMedicalRecord(medicalRecord);
        return Result.success(newRecord);
    }
    
    @Operation(summary = "更新就诊记录")
    @PutMapping("/{id}")
    public Result<?> updateMedicalRecord(@PathVariable Long id, @RequestBody MedicalRecord medicalRecord) {
        LOGGER.info("更新就诊记录: id={}", id);
        
        // 检查权限，如果是医生，只能更新自己的就诊记录
        MedicalRecord existingRecord = medicalRecordService.getMedicalRecordById(id);
        if (existingRecord != null) {
            checkDoctorPermission(existingRecord);
        }
        
        medicalRecordService.updateMedicalRecord(id, medicalRecord);
        return Result.success();
    }
    
    @Operation(summary = "获取就诊记录详情")
    @GetMapping("/{id}")
    public Result<?> getMedicalRecordById(@PathVariable Long id) {
        MedicalRecord medicalRecord = medicalRecordService.getMedicalRecordById(id);
        
        // 检查权限，如果是医生，只能查看自己的就诊记录
        checkDoctorPermission(medicalRecord);
        
        return Result.success(medicalRecord);
    }
    
    @Operation(summary = "分页查询就诊记录")
    @GetMapping("/page")
    public Result<?> getMedicalRecordsByPage(
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) String patientName,
            @RequestParam(required = false) String doctorName,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "1") Integer currentPage,
            @RequestParam(defaultValue = "10") Integer size) {
        LOGGER.info("分页查询就诊记录: patientId={}, doctorId={}, patientName={}, doctorName={}, startDate={}, endDate={}, currentPage={}, size={}", 
                patientId, doctorId, patientName, doctorName, startDate, endDate, currentPage, size);
        
        // 检查当前用户是否是医生，如果是医生，只能查看自己的就诊记录
        Long currentUserId = JwtTokenUtils.getCurrentUserId();
        if (currentUserId != null) {
            try {
                Doctor currentDoctor = doctorService.getDoctorByUserId(currentUserId);
                if (currentDoctor != null) {
                    // 如果是医生，强制设置doctorId为当前医生ID
                    doctorId = currentDoctor.getId();
                }
            } catch (Exception e) {
                // 不是医生，忽略异常
            }
        }
        
        Page<MedicalRecord> page = medicalRecordService.getMedicalRecordsByPage(patientId, doctorId, patientName, doctorName, startDate, endDate, currentPage, size);
        return Result.success(page);
    }
    
    @Operation(summary = "获取患者就诊记录")
    @GetMapping("/patient/{patientId}")
    public Result<?> getMedicalRecordsByPatient(@PathVariable Long patientId) {
        return Result.success(medicalRecordService.getMedicalRecordsByPatient(patientId));
    }
    
    @Operation(summary = "获取当前患者就诊记录")
    @GetMapping("/my")
    public Result<?> getMyMedicalRecords() {
        // 获取当前登录用户的患者ID
        Patient currentPatient = patientService.getCurrentPatient();
        if(currentPatient != null) {
            return Result.success(medicalRecordService.getMedicalRecordsByPatient(currentPatient.getId()));
        }else{

            return Result.error("未找到患者信息");
        }

    }
    
    @Operation(summary = "获取当前医生就诊记录")
    @GetMapping("/my-doctor")
    public Result<?> getMyDoctorMedicalRecords(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            // 获取当前登录用户
            Long userId = JwtTokenUtils.getCurrentUserId();
            if (userId == null) {
                return Result.error("未登录");
            }
            
            // 获取医生信息
            Doctor doctor = doctorService.getDoctorByUserId(userId);
            if (doctor == null) {
                return Result.error("未找到医生信息");
            }
            
            return Result.success(medicalRecordService.getMedicalRecordsByDoctor(doctor.getId(), date));
        } catch (Exception e) {
            LOGGER.error("获取当前医生就诊记录失败", e);
            return Result.error("获取就诊记录失败: " + e.getMessage());
        }
    }
    
    @Operation(summary = "获取医生就诊记录")
    @GetMapping("/doctor/{doctorId}")
    public Result<?> getMedicalRecordsByDoctor(
            @PathVariable Long doctorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return Result.success(medicalRecordService.getMedicalRecordsByDoctor(doctorId, date));
    }
    
    @Operation(summary = "删除就诊记录")
    @DeleteMapping("/{id}")
    public Result<?> deleteMedicalRecord(@PathVariable Long id) {
        LOGGER.info("删除就诊记录: id={}", id);
        
        // 检查权限，如果是医生，只能删除自己的就诊记录
        MedicalRecord medicalRecord = medicalRecordService.getMedicalRecordById(id);
        if (medicalRecord != null) {
            checkDoctorPermission(medicalRecord);
        }
        
        medicalRecordService.deleteMedicalRecord(id);
        return Result.success();
    }
    
    /**
     * 检查医生权限
     * 如果当前用户是医生，只能操作自己的就诊记录
     */
    private void checkDoctorPermission(MedicalRecord medicalRecord) {
        Long currentUserId = JwtTokenUtils.getCurrentUserId();
        if (currentUserId == null) {
            return; // 未登录，由其他权限控制处理
        }
        User user = userMapper.selectById(currentUserId);
        if(user.getRoleCode().equals("ADMIN")){
            return;
        }

        try {
            Doctor currentDoctor = doctorService.getDoctorByUserId(currentUserId);
            if (currentDoctor != null && !currentDoctor.getId().equals(medicalRecord.getDoctorId())) {
                throw new ServiceException("您只能操作自己的就诊记录");
            }
        } catch (Exception e) {
            // 不是医生，忽略异常
        }
    }
} 