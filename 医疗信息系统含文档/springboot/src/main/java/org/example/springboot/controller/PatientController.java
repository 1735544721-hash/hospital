package org.example.springboot.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.example.springboot.common.Result;
import org.example.springboot.entity.Patient;
import org.example.springboot.entity.User;
import org.example.springboot.DTO.PatientRegisterDTO;
import org.example.springboot.service.PatientService;
import org.example.springboot.util.JwtTokenUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

@Tag(name = "患者管理接口")
@RestController
@RequestMapping("/patient")
public class PatientController {
    private static final Logger LOGGER = LoggerFactory.getLogger(PatientController.class);
    
    @Resource
    private PatientService patientService;
    
    @Operation(summary = "新增患者")
    @PostMapping
    public Result<?> createPatient(@RequestBody Patient patient) {
        LOGGER.info("新增患者: {}", patient);
        Patient newPatient = patientService.createPatient(patient);
        return Result.success(newPatient);
    }
    
    @Operation(summary = "更新患者信息")
    @PutMapping("/{id}")
    public Result<?> updatePatient(@PathVariable Long id, @RequestBody Patient patient) {
        LOGGER.info("更新患者信息: id={}, patient={}", id, patient);
        patientService.updatePatient(id, patient);
        return Result.success();
    }
    
    @Operation(summary = "获取患者详情")
    @GetMapping("/{id}")
    public Result<?> getPatientById(@PathVariable Long id) {
        Patient patient = patientService.getPatientById(id);
        return Result.success(patient);
    }
    
    @Operation(summary = "获取当前登录用户的患者信息")
    @GetMapping("/current")
    public Result<?> getCurrentPatient() {
        try {
            Patient patient = patientService.getCurrentPatient();
            return Result.success(patient);
        } catch (Exception e) {
            // 如果用户没有关联患者信息，返回null
            return Result.success(null);
        }
    }
    
    @Operation(summary = "获取所有患者列表")
    @GetMapping("/list")
    public Result<?> getAllPatients() {
        LOGGER.info("获取所有患者列表");
        List<Patient> patients = patientService.getAllPatients();
        return Result.success(patients);
    }
    
    @Operation(summary = "分页查询患者列表")
    @GetMapping("/page")
    public Result<?> getPatientsByPage(
            @RequestParam(defaultValue = "") String name,
            @RequestParam(defaultValue = "") String idCard,
            @RequestParam(defaultValue = "") String phone,
            @RequestParam(defaultValue = "") String username,
            @RequestParam(defaultValue = "1") Integer currentPage,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(patientService.getPatientsByPage(name, idCard, phone, username, currentPage, size));
    }
    
    @Operation(summary = "删除患者")
    @DeleteMapping("/{id}")
    public Result<?> deletePatient(@PathVariable Long id) {
        LOGGER.info("删除患者: id={}", id);
        patientService.deletePatient(id);
        return Result.success();
    }
    
    @Operation(summary = "批量删除患者")
    @DeleteMapping("/batch")
    public Result<?> batchDeletePatients(@RequestBody List<Long> ids) {
        LOGGER.info("批量删除患者: ids={}", ids);
        patientService.batchDeletePatients(ids);
        return Result.success();
    }
    
    @Operation(summary = "绑定患者与用户关系")
    @PutMapping("/bind/{patientId}/{userId}")
    public Result<?> bindUserToPatient(@PathVariable Long patientId, @PathVariable Long userId) {
        LOGGER.info("绑定患者与用户: patientId={}, userId={}", patientId, userId);
        patientService.bindUserToPatient(patientId, userId);
        return Result.success();
    }
    
    @Operation(summary = "解绑患者与用户关系")
    @PutMapping("/unbind/{patientId}")
    public Result<?> unbindUserFromPatient(@PathVariable Long patientId) {
        LOGGER.info("解绑患者与用户: patientId={}", patientId);
        patientService.unbindUserFromPatient(patientId);
        return Result.success();
    }
    
    @Operation(summary = "新增患者并创建用户")
    @PostMapping("/with-user")
    public Result<?> createPatientWithUser(@RequestBody Map<String, Object> params) {
        LOGGER.info("新增患者并创建用户: {}", params);
        
        // 解析用户和患者数据
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        
        User user = null;
        Patient patient = null;
        
        try {
            if (params.containsKey("user")) {
                user = objectMapper.convertValue(params.get("user"), User.class);
            }
            
            if (params.containsKey("patient")) {
                patient = objectMapper.convertValue(params.get("patient"), Patient.class);
            }
        } catch (Exception e) {
            LOGGER.error("解析请求参数失败", e);
            return Result.error("参数格式错误");
        }
        
        if (user == null || patient == null) {
            return Result.error("用户或患者信息不能为空");
        }
        
        // 日志记录接收到的用户和患者信息
        LOGGER.info("解析后的用户信息: {}", user);
        LOGGER.info("解析后的患者信息: {}", patient);
        
        // 调用服务创建用户和患者
        Patient newPatient = patientService.createPatientWithUser(user, patient);
        return Result.success(newPatient);
    }
    
    @Operation(summary = "患者注册")
    @PostMapping("/register")
    public Result<?> registerPatient(@RequestBody PatientRegisterDTO registerDTO) {
        LOGGER.info("患者注册: {}", registerDTO);
        
        // 验证两次密码是否一致
        if (!registerDTO.getUser().getPassword().equals(registerDTO.getConfirmPassword())) {
            return Result.error("两次输入的密码不一致");
        }
        
        // 创建用户和患者对象
        User user = new User();
        Patient patient = new Patient();
        
        // 复制用户信息
        user.setUsername(registerDTO.getUser().getUsername());
        user.setPassword(registerDTO.getUser().getPassword());
        user.setEmail(registerDTO.getUser().getEmail());
        user.setRoleCode(registerDTO.getUser().getRoleCode());
        user.setName(registerDTO.getUser().getName());
        user.setSex(registerDTO.getUser().getSex());
        user.setPhone(registerDTO.getUser().getPhone());
        
        // 复制患者信息
        patient.setName(registerDTO.getPatient().getName());
        patient.setSex(registerDTO.getPatient().getSex());
        patient.setBirthday(registerDTO.getPatient().getBirthday());
        patient.setIdCard(registerDTO.getPatient().getIdCard());
        patient.setPhone(registerDTO.getPatient().getPhone());
        patient.setAddress(registerDTO.getPatient().getAddress());
        patient.setMedicalHistory(registerDTO.getPatient().getMedicalHistory());
        patient.setAllergies(registerDTO.getPatient().getAllergies());
        
        // 调用服务创建用户和患者
        Patient newPatient = patientService.createPatientWithUser(user, patient);
        return Result.success(newPatient);
    }
} 