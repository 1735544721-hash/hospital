package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.example.springboot.entity.*;
import org.example.springboot.exception.ServiceException;
import org.example.springboot.mapper.*;
import org.example.springboot.util.JwtTokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class MedicalRecordService {
    @Resource
    private MedicalRecordMapper medicalRecordMapper;
    
    @Resource
    private PatientMapper patientMapper;
    
    @Resource
    private DoctorMapper doctorMapper;
    
    @Resource
    private AppointmentMapper appointmentMapper;
    
    @Resource
    private AppointmentService appointmentService;
    
    @Resource
    private PrescriptionMapper prescriptionMapper;
    
    @Resource
    private DepartmentMapper departmentMapper;
    
    @Resource
    private DoctorService doctorService;
    @Autowired
    private UserMapper userMapper;

    /**
     * 创建就诊记录
     */
    @Transactional
    public MedicalRecord createMedicalRecord(MedicalRecord medicalRecord) {
        // 检查患者是否存在
        Patient patient = patientMapper.selectById(medicalRecord.getPatientId());
        if (patient == null) {
            throw new ServiceException("患者不存在");
        }
        
        // 检查医生是否存在
        Doctor doctor = doctorMapper.selectById(medicalRecord.getDoctorId());
        if (doctor == null) {
            throw new ServiceException("医生不存在");
        }
        
        // 检查权限，如果是医生，只能创建自己的就诊记录
        checkDoctorPermission(medicalRecord);
        
        // 如果有关联预约，检查预约是否存在
        if (medicalRecord.getAppointmentId() != null) {
            Appointment appointment = appointmentMapper.selectById(medicalRecord.getAppointmentId());
            if (appointment == null) {
                throw new ServiceException("预约不存在");
            }
            
            // 更新预约状态为已就诊
            if (appointment.getStatus() == 1) {
                appointmentService.completeAppointment(appointment.getId());
            }
        }
        
        // 生成就诊记录编号
        medicalRecord.setRecordNo(generateRecordNo());
        
        // 如果没有指定就诊日期，使用当前日期
        if (medicalRecord.getRecordDate() == null) {
            medicalRecord.setRecordDate(LocalDate.now());
        }
        
        // 设置创建时间和更新时间
        LocalDateTime now = LocalDateTime.now();
        medicalRecord.setCreateTime(now);
        medicalRecord.setUpdateTime(now);
        
        if (medicalRecordMapper.insert(medicalRecord) <= 0) {
            throw new ServiceException("就诊记录创建失败");
        }
        
        return medicalRecord;
    }
    
    /**
     * 更新就诊记录
     */
    @Transactional
    public void updateMedicalRecord(Long id, MedicalRecord medicalRecord) {
        // 检查就诊记录是否存在
        MedicalRecord existingRecord = medicalRecordMapper.selectById(id);
        if (existingRecord == null) {
            throw new ServiceException("就诊记录不存在");
        }
        
        // 检查权限，如果是医生，只能更新自己的就诊记录
        checkDoctorPermission(existingRecord);
        
        medicalRecord.setId(id);
        medicalRecord.setUpdateTime(LocalDateTime.now());
        
        if (medicalRecordMapper.updateById(medicalRecord) <= 0) {
            throw new ServiceException("就诊记录更新失败");
        }
    }
    
    /**
     * 获取就诊记录详情
     */
    public MedicalRecord getMedicalRecordById(Long id) {
        MedicalRecord medicalRecord = medicalRecordMapper.selectById(id);
        if (medicalRecord == null) {
            throw new ServiceException("就诊记录不存在");
        }
        
        // 查询患者信息
        Patient patient = patientMapper.selectById(medicalRecord.getPatientId());
        medicalRecord.setPatient(patient);
        
        // 查询医生信息
        Doctor doctor = doctorMapper.selectById(medicalRecord.getDoctorId());
        if (doctor != null) {
            // 查询科室信息
            if (doctor.getDepartmentId() != null) {
                Department department = departmentMapper.selectById(doctor.getDepartmentId());
                doctor.setDepartment(department);
            }
        medicalRecord.setDoctor(doctor);
        }
        
        // 如果有关联预约，查询预约信息
        if (medicalRecord.getAppointmentId() != null) {
            Appointment appointment = appointmentMapper.selectById(medicalRecord.getAppointmentId());
            medicalRecord.setAppointment(appointment);
        }
        
        return medicalRecord;
    }
    
    /**
     * 分页查询就诊记录
     */
    public Page<MedicalRecord> getMedicalRecordsByPage(Long patientId, Long doctorId, String patientName, String doctorName,
                                                LocalDate startDate, LocalDate endDate, Integer currentPage, Integer size) {
        Page<MedicalRecord> page = new Page<>(currentPage, size);
        
        // 如果有患者姓名或医生姓名查询条件，需要进行关联查询
        if (StringUtils.isNotBlank(patientName) || StringUtils.isNotBlank(doctorName)) {
            // 使用自定义SQL查询
            Page<MedicalRecord> resultPage = medicalRecordMapper.selectMedicalRecordsByNamePage(
                page, patientId, doctorId, patientName, doctorName, startDate, endDate);
            
            // 填充关联信息
            fillMedicalRecordsInfo(resultPage.getRecords());
            
            return resultPage;
        }
        
        LambdaQueryWrapper<MedicalRecord> queryWrapper = new LambdaQueryWrapper<>();
        
        // 添加查询条件
        if (patientId != null) {
            queryWrapper.eq(MedicalRecord::getPatientId, patientId);
        }
        if (doctorId != null) {
            queryWrapper.eq(MedicalRecord::getDoctorId, doctorId);
        }
        if (startDate != null) {
            queryWrapper.ge(MedicalRecord::getRecordDate, startDate);
        }
        if (endDate != null) {
            queryWrapper.le(MedicalRecord::getRecordDate, endDate);
        }
        
        // 按就诊日期降序排序
        queryWrapper.orderByDesc(MedicalRecord::getRecordDate);
        
        Page<MedicalRecord> resultPage = medicalRecordMapper.selectPage(page, queryWrapper);
        
        // 填充关联信息
        fillMedicalRecordsInfo(resultPage.getRecords());
        
        return resultPage;
    }
    
    /**
     * 获取患者就诊记录
     */
    public List<MedicalRecord> getMedicalRecordsByPatient(Long patientId) {
        LambdaQueryWrapper<MedicalRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MedicalRecord::getPatientId, patientId)
                   .orderByDesc(MedicalRecord::getRecordDate);
        
        List<MedicalRecord> records = medicalRecordMapper.selectList(queryWrapper);
        
        // 查询关联信息
        for (MedicalRecord record : records) {
            // 查询医生信息
            Doctor doctor = doctorMapper.selectById(record.getDoctorId());
            if (doctor != null) {
                // 查询科室信息
                if (doctor.getDepartmentId() != null) {
                    Department department = departmentMapper.selectById(doctor.getDepartmentId());
                    doctor.setDepartment(department);
                }
            record.setDoctor(doctor);
            }
        }
        
        return records;
    }
    
    /**
     * 获取医生就诊记录
     */
    public List<MedicalRecord> getMedicalRecordsByDoctor(Long doctorId, LocalDate date) {
        LambdaQueryWrapper<MedicalRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MedicalRecord::getDoctorId, doctorId);
        
        if (date != null) {
            queryWrapper.eq(MedicalRecord::getRecordDate, date);
        }
        
        queryWrapper.orderByDesc(MedicalRecord::getRecordDate);
        
        List<MedicalRecord> records = medicalRecordMapper.selectList(queryWrapper);
        
        // 查询关联信息
        for (MedicalRecord record : records) {
            // 查询患者信息
            Patient patient = patientMapper.selectById(record.getPatientId());
            record.setPatient(patient);
            
            // 查询医生信息
            Doctor doctor = doctorMapper.selectById(record.getDoctorId());
            if (doctor != null) {
                // 查询科室信息
                if (doctor.getDepartmentId() != null) {
                    Department department = departmentMapper.selectById(doctor.getDepartmentId());
                    doctor.setDepartment(department);
                }
                record.setDoctor(doctor);
            }
        }
        
        return records;
    }
    
    /**
     * 删除就诊记录
     */
    @Transactional
    public void deleteMedicalRecord(Long id) {
        // 检查就诊记录是否存在
        MedicalRecord medicalRecord = medicalRecordMapper.selectById(id);
        if (medicalRecord == null) {
            throw new ServiceException("就诊记录不存在");
        }
        
        // 检查权限，如果是医生，只能删除自己的就诊记录
        checkDoctorPermission(medicalRecord);
        
        // 检查就诊记录是否有关联处方
        LambdaQueryWrapper<Prescription> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Prescription::getRecordId, id);
        long count = prescriptionMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new ServiceException("该就诊记录已关联处方，不能删除");
        }
        
        if (medicalRecordMapper.deleteById(id) <= 0) {
            throw new ServiceException("就诊记录删除失败");
        }
    }
    
    /**
     * 生成就诊记录编号
     */
    private String generateRecordNo() {
        // 生成格式：MR + 年月日 + 6位随机数
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomStr = String.format("%06d", (int)(Math.random() * 1000000));
        return "MR" + dateStr + randomStr;
    }

    private void fillMedicalRecordsInfo(List<MedicalRecord> records) {
        for (MedicalRecord record : records) {
            // 查询患者信息
            Patient patient = patientMapper.selectById(record.getPatientId());
            record.setPatient(patient);
            
            // 查询医生信息
            Doctor doctor = doctorMapper.selectById(record.getDoctorId());
            if (doctor != null) {
                // 查询科室信息
                if (doctor.getDepartmentId() != null) {
                    Department department = departmentMapper.selectById(doctor.getDepartmentId());
                    doctor.setDepartment(department);
                }
                record.setDoctor(doctor);
            }
        }
    }
    
    /**
     * 检查医生权限
     * 如果当前用户是医生，只能操作自己的就诊记录
     */
    public void checkDoctorPermission(MedicalRecord medicalRecord) {
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
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            // 不是医生，忽略异常
        }
    }
} 