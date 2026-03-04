package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.example.springboot.entity.Patient;
import org.example.springboot.entity.User;
import org.example.springboot.entity.Appointment;
import org.example.springboot.entity.MedicalRecord;
import org.example.springboot.entity.Prescription;
import org.example.springboot.exception.ServiceException;
import org.example.springboot.mapper.PatientMapper;
import org.example.springboot.mapper.UserMapper;
import org.example.springboot.mapper.AppointmentMapper;
import org.example.springboot.mapper.MedicalRecordMapper;
import org.example.springboot.mapper.PrescriptionMapper;
import org.example.springboot.util.JwtTokenUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class PatientService {
    @Resource
    private PatientMapper patientMapper;
    
    @Resource
    private UserMapper userMapper;
    
    @Resource
    private AppointmentMapper appointmentMapper;
    
    @Resource
    private MedicalRecordMapper medicalRecordMapper;
    
    @Resource
    private PrescriptionMapper prescriptionMapper;
    
    @Resource
    private UserService userService;
    
    /**
     * 新增患者
     */
    @Transactional
    public Patient createPatient(Patient patient) {
        // 检查患者是否已存在
        if (patientMapper.selectOne(
                new LambdaQueryWrapper<Patient>()
                    .eq(Patient::getIdCard, patient.getIdCard())
            ) != null) {
            throw new ServiceException("身份证号已被注册");
        }
        
        // 生成患者编号
        String patientNo = "P" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        patient.setPatientNo(patientNo);
        
        // 设置创建时间
        patient.setCreateTime(LocalDateTime.now());
        patient.setUpdateTime(LocalDateTime.now());
        
        // 如果绑定了用户ID，检查用户是否存在
        if (patient.getUserId() != null) {
            User user = userMapper.selectById(patient.getUserId());
            if (user == null) {
                throw new ServiceException("绑定的用户不存在");
            }
        }
        
        if (patientMapper.insert(patient) <= 0) {
            throw new ServiceException("患者信息添加失败");
        }
        
        return patient;
    }
    
    /**
     * 更新患者信息
     */
    public void updatePatient(Long id, Patient patient) {
        // 检查患者是否存在
        if (patientMapper.selectById(id) == null) {
            throw new ServiceException("患者不存在");
        }
        
        // 如果修改了身份证号，检查是否与其他患者冲突
        if (StringUtils.isNotBlank(patient.getIdCard())) {
            Patient existPatient = patientMapper.selectOne(
                new LambdaQueryWrapper<Patient>()
                    .eq(Patient::getIdCard, patient.getIdCard())
            );
            if (existPatient != null && !existPatient.getId().equals(id)) {
                throw new ServiceException("身份证号已被其他患者使用");
            }
        }
        
        patient.setId(id);
        patient.setUpdateTime(LocalDateTime.now());
        
        if (patientMapper.updateById(patient) <= 0) {
            throw new ServiceException("患者信息更新失败");
        }
    }
    
    /**
     * 获取患者详情
     */
    public Patient getPatientById(Long id) {
        Patient patient = patientMapper.selectById(id);
        if (patient == null) {
            throw new ServiceException("患者不存在");
        }
        
        // 如果有关联用户，查询用户信息
        if (patient.getUserId() != null) {
            User user = userMapper.selectById(patient.getUserId());
            patient.setUser(user);
        }
        
        return patient;
    }
    
    /**
     * 获取患者详情(通过用户ID)
     */
    public Patient getPatientByUserId(Long userId) {
        Patient patient = patientMapper.selectOne(
            new LambdaQueryWrapper<Patient>()
                .eq(Patient::getUserId, userId)
        );
        
        if (patient == null) {
            throw new ServiceException("未找到患者信息");
        }
        
        // 查询关联的用户信息
        User user = userMapper.selectById(userId);
        patient.setUser(user);
        
        return patient;
    }
    
    /**
     * 获取当前登录用户的患者信息
     */
    public Patient getCurrentPatient() {
        User currentUser = JwtTokenUtils.getCurrentUser();
        if (currentUser == null) {
            throw new ServiceException("用户未登录");
        }
        
        return getPatientByUserId(currentUser.getId());
    }
    
    /**
     * 分页查询患者列表
     */
    public Page<Patient> getPatientsByPage(String name, String idCard, String phone, String username, Integer currentPage, Integer size) {
        Page<Patient> page = new Page<>(currentPage, size);
        
        // 如果需要按关联用户名查询，使用自定义SQL查询
        if (StringUtils.isNotBlank(username)) {
            return patientMapper.selectPatientsByUsername(page, name, idCard, phone, username);
        }
        
        // 常规查询条件
        LambdaQueryWrapper<Patient> queryWrapper = new LambdaQueryWrapper<>();
        
        // 添加查询条件
        if (StringUtils.isNotBlank(name)) {
            queryWrapper.like(Patient::getName, name);
        }
        if (StringUtils.isNotBlank(idCard)) {
            queryWrapper.like(Patient::getIdCard, idCard);
        }
        if (StringUtils.isNotBlank(phone)) {
            queryWrapper.like(Patient::getPhone, phone);
        }
        
        // 按创建时间降序排序
        queryWrapper.orderByDesc(Patient::getCreateTime);
        
        Page<Patient> patientPage = patientMapper.selectPage(page, queryWrapper);
        
        // 查询关联的用户信息
        for (Patient patient : patientPage.getRecords()) {
            if (patient.getUserId() != null) {
                User user = userMapper.selectById(patient.getUserId());
                patient.setUser(user);
            }
        }
        
        return patientPage;
    }
    
    /**
     * 删除患者
     */
    @Transactional
    public void deletePatient(Long id) {
        // 检查患者是否存在
        Patient patient = patientMapper.selectById(id);
        if (patient == null) {
            throw new ServiceException("患者不存在");
        }
        
        // 检查患者是否有关联预约
        LambdaQueryWrapper<Appointment> appointmentQueryWrapper = new LambdaQueryWrapper<>();
        appointmentQueryWrapper.eq(Appointment::getPatientId, id);
        long appointmentCount = appointmentMapper.selectCount(appointmentQueryWrapper);
        if (appointmentCount > 0) {
            throw new ServiceException("该患者已有预约记录，不能删除");
        }
        
        // 检查患者是否有关联就诊记录
        LambdaQueryWrapper<MedicalRecord> medicalRecordQueryWrapper = new LambdaQueryWrapper<>();
        medicalRecordQueryWrapper.eq(MedicalRecord::getPatientId, id);
        long medicalRecordCount = medicalRecordMapper.selectCount(medicalRecordQueryWrapper);
        if (medicalRecordCount > 0) {
            throw new ServiceException("该患者已有就诊记录，不能删除");
        }
        
        // 检查患者是否有关联处方
        LambdaQueryWrapper<Prescription> prescriptionQueryWrapper = new LambdaQueryWrapper<>();
        prescriptionQueryWrapper.eq(Prescription::getPatientId, id);
        long prescriptionCount = prescriptionMapper.selectCount(prescriptionQueryWrapper);
        if (prescriptionCount > 0) {
            throw new ServiceException("该患者已有处方记录，不能删除");
        }
        
        if (patientMapper.deleteById(id) <= 0) {
            throw new ServiceException("患者删除失败");
        }
    }
    
    /**
     * 批量删除患者
     */
    @Transactional
    public void batchDeletePatients(List<Long> ids) {
        for (Long id : ids) {
            // 检查患者是否存在
            Patient patient = patientMapper.selectById(id);
            if (patient == null) {
                throw new ServiceException("ID为" + id + "的患者不存在");
            }
            
            // 检查患者是否有关联预约
            LambdaQueryWrapper<Appointment> appointmentQueryWrapper = new LambdaQueryWrapper<>();
            appointmentQueryWrapper.eq(Appointment::getPatientId, id);
            long appointmentCount = appointmentMapper.selectCount(appointmentQueryWrapper);
            if (appointmentCount > 0) {
                throw new ServiceException("ID为" + id + "的患者已有预约记录，不能删除");
            }
            
            // 检查患者是否有关联就诊记录
            LambdaQueryWrapper<MedicalRecord> medicalRecordQueryWrapper = new LambdaQueryWrapper<>();
            medicalRecordQueryWrapper.eq(MedicalRecord::getPatientId, id);
            long medicalRecordCount = medicalRecordMapper.selectCount(medicalRecordQueryWrapper);
            if (medicalRecordCount > 0) {
                throw new ServiceException("ID为" + id + "的患者已有就诊记录，不能删除");
            }
            
            // 检查患者是否有关联处方
            LambdaQueryWrapper<Prescription> prescriptionQueryWrapper = new LambdaQueryWrapper<>();
            prescriptionQueryWrapper.eq(Prescription::getPatientId, id);
            long prescriptionCount = prescriptionMapper.selectCount(prescriptionQueryWrapper);
            if (prescriptionCount > 0) {
                throw new ServiceException("ID为" + id + "的患者已有处方记录，不能删除");
            }
        }
        
        if (patientMapper.deleteBatchIds(ids) <= 0) {
            throw new ServiceException("批量删除患者失败");
        }
    }
    
    /**
     * 绑定患者与用户关系
     */
    @Transactional
    public void bindUserToPatient(Long patientId, Long userId) {
        // 检查患者是否存在
        Patient patient = patientMapper.selectById(patientId);
        if (patient == null) {
            throw new ServiceException("患者不存在");
        }
        
        // 检查用户是否存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new ServiceException("用户不存在");
        }
        
        // 检查用户是否已绑定其他患者
        Patient existPatient = patientMapper.selectOne(
            new LambdaQueryWrapper<Patient>()
                .eq(Patient::getUserId, userId)
        );
        if (existPatient != null && !existPatient.getId().equals(patientId)) {
            throw new ServiceException("该用户已绑定其他患者");
        }
        
        // 更新患者关联的用户ID
        patient.setUserId(userId);
        patient.setUpdateTime(LocalDateTime.now());
        
        if (patientMapper.updateById(patient) <= 0) {
            throw new ServiceException("绑定用户失败");
        }
    }
    
    /**
     * 解绑患者与用户关系
     */
    public void unbindUserFromPatient(Long patientId) {
        // 检查患者是否存在
        Patient patient = patientMapper.selectById(patientId);
        if (patient == null) {
            throw new ServiceException("患者不存在");
        }
        
        // 解除绑定
        patient.setUserId(null);
        patient.setUpdateTime(LocalDateTime.now());
        
        if (patientMapper.updateById(patient) <= 0) {
            throw new ServiceException("解绑用户失败");
        }
    }
    
    /**
     * 获取所有患者列表
     */
    public List<Patient> getAllPatients() {
        return patientMapper.selectList(
            new LambdaQueryWrapper<Patient>()
                .orderByDesc(Patient::getCreateTime)
        );
    }
    
    /**
     * 创建患者并同时创建用户
     */
    @Transactional
    public Patient createPatientWithUser(User user, Patient patient) {
        // 设置用户角色为患者
        if (user.getRoleCode() == null) {
            user.setRoleCode("PATIENT");
        }
        
        // 确保用户名和密码存在
        if (StringUtils.isBlank(user.getUsername()) || StringUtils.isBlank(user.getPassword())) {
            throw new ServiceException("用户名和密码不能为空");
        }
        
        // 密码加密
        user.setPassword(userService.encryptPassword(user.getPassword()));
        
        // 设置默认状态
        user.setStatus(1); // 启用状态
        
        // 设置默认头像
        if (StringUtils.isBlank(user.getAvatar())) {
            user.setAvatar("/img/default_avatar.png");
        }
        
        // 同步用户与患者的共同信息
        user.setName(patient.getName());
        user.setPhone(patient.getPhone());
        user.setSex(patient.getSex());
        
        // 先创建用户
        userMapper.insert(user);
        
        // 设置患者关联的用户ID
        patient.setUserId(user.getId());
        
        // 创建患者
        return createPatient(patient);
    }
} 