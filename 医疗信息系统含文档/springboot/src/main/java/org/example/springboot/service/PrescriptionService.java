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
public class PrescriptionService {
    @Resource
    private PrescriptionMapper prescriptionMapper;
    
    @Resource
    private PrescriptionDetailMapper prescriptionDetailMapper;
    
    @Resource
    private MedicalRecordMapper medicalRecordMapper;
    
    @Resource
    private PatientMapper patientMapper;
    
    @Resource
    private DoctorMapper doctorMapper;
    
    @Resource
    private MedicineMapper medicineMapper;
    
    @Resource
    private DoctorService doctorService;
    @Autowired
    private UserMapper userMapper;

    /**
     * 创建处方
     */
    @Transactional
    public Prescription createPrescription(Prescription prescription) {
        // 检查就诊记录是否存在
        MedicalRecord medicalRecord = medicalRecordMapper.selectById(prescription.getRecordId());
        if (medicalRecord == null) {
            throw new ServiceException("就诊记录不存在");
        }
        
        // 检查患者是否存在
        Patient patient = patientMapper.selectById(prescription.getPatientId());
        if (patient == null) {
            throw new ServiceException("患者不存在");
        }
        
        // 检查医生是否存在
        Doctor doctor = doctorMapper.selectById(prescription.getDoctorId());
        if (doctor == null) {
            throw new ServiceException("医生不存在");
        }
        
        // 检查权限，如果是医生，只能创建自己的处方
        checkDoctorPermission(prescription);
        
        // 生成处方编号
        prescription.setPrescriptionNo(generatePrescriptionNo());
        
        // 如果没有指定处方日期，使用当前日期
        if (prescription.getPrescriptionDate() == null) {
            prescription.setPrescriptionDate(LocalDate.now());
        }
        
        // 设置默认状态为未取药
        if (prescription.getStatus() == null) {
            prescription.setStatus(0);
        }
        
        // 设置创建时间和更新时间
        LocalDateTime now = LocalDateTime.now();
        prescription.setCreateTime(now);
        prescription.setUpdateTime(now);
        
        if (prescriptionMapper.insert(prescription) <= 0) {
            throw new ServiceException("处方创建失败");
        }
        
        // 处理处方明细
        if (prescription.getDetails() != null && !prescription.getDetails().isEmpty()) {
            for (PrescriptionDetail detail : prescription.getDetails()) {
                // 检查药品是否存在
                Medicine medicine = medicineMapper.selectById(detail.getMedicineId());
                if (medicine == null) {
                    throw new ServiceException("药品不存在: ID=" + detail.getMedicineId());
                }
                
                detail.setPrescriptionId(prescription.getId());
                detail.setCreateTime(now);
                detail.setUpdateTime(now);
                
                if (prescriptionDetailMapper.insert(detail) <= 0) {
                    throw new ServiceException("处方明细创建失败");
                }
            }
        }
        
        return prescription;
    }
    
    /**
     * 更新处方状态
     */
    @Transactional
    public void updatePrescriptionStatus(Long id, Integer status) {
        Prescription prescription = prescriptionMapper.selectById(id);
        if (prescription == null) {
            throw new ServiceException("处方不存在");
        }
        
        // 检查权限，如果是医生，只能更新自己的处方
        checkDoctorPermission(prescription);
        
        Prescription updatePrescription = new Prescription();
        updatePrescription.setId(id);
        updatePrescription.setStatus(status);
        updatePrescription.setUpdateTime(LocalDateTime.now());
        
        if (prescriptionMapper.updateById(updatePrescription) <= 0) {
            throw new ServiceException("处方状态更新失败");
        }
    }
    
    /**
     * 获取处方详情
     */
    public Prescription getPrescriptionById(Long id) {
        Prescription prescription = prescriptionMapper.selectById(id);
        if (prescription == null) {
            throw new ServiceException("处方不存在");
        }
        
        // 查询患者信息
        Patient patient = patientMapper.selectById(prescription.getPatientId());
        prescription.setPatient(patient);
        
        // 查询医生信息
        Doctor doctor = doctorMapper.selectById(prescription.getDoctorId());
        prescription.setDoctor(doctor);
        
        // 查询就诊记录信息
        MedicalRecord medicalRecord = medicalRecordMapper.selectById(prescription.getRecordId());
        prescription.setMedicalRecord(medicalRecord);
        
        // 查询处方明细
        LambdaQueryWrapper<PrescriptionDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PrescriptionDetail::getPrescriptionId, id);
        List<PrescriptionDetail> details = prescriptionDetailMapper.selectList(queryWrapper);
        
        // 查询药品信息
        for (PrescriptionDetail detail : details) {
            Medicine medicine = medicineMapper.selectById(detail.getMedicineId());
            detail.setMedicine(medicine);
        }
        
        prescription.setDetails(details);
        
        return prescription;
    }
    
    /**
     * 分页查询处方列表
     */
    public Page<Prescription> getPrescriptionsByPage(String prescriptionNo, Long patientId, Long doctorId, 
                                               String patientName, String doctorName, Long recordId, 
                                               LocalDate startDate, LocalDate endDate, Integer status, 
                                               Integer currentPage, Integer size) {
        Page<Prescription> page = new Page<>(currentPage, size);
        
        // 检查当前用户是否是医生，如果是医生，只能查看自己的处方
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
        
        LambdaQueryWrapper<Prescription> queryWrapper = new LambdaQueryWrapper<>();
        
        // 添加查询条件
        if (StringUtils.isNotBlank(prescriptionNo)) {
            queryWrapper.like(Prescription::getPrescriptionNo, prescriptionNo);
        }
        
        if (patientId != null) {
            queryWrapper.eq(Prescription::getPatientId, patientId);
        }
        
        if (doctorId != null) {
            queryWrapper.eq(Prescription::getDoctorId, doctorId);
        }
        
        if (recordId != null) {
            queryWrapper.eq(Prescription::getRecordId, recordId);
        }
        
        if (startDate != null) {
            queryWrapper.ge(Prescription::getPrescriptionDate, startDate);
        }
        
        if (endDate != null) {
            queryWrapper.le(Prescription::getPrescriptionDate, endDate);
        }
        
        if (status != null) {
            queryWrapper.eq(Prescription::getStatus, status);
        }
        
        // 按创建时间降序排序
        queryWrapper.orderByDesc(Prescription::getCreateTime);
        
        // 如果有患者姓名或医生姓名查询条件，需要进行关联查询
        if (StringUtils.isNotBlank(patientName) || StringUtils.isNotBlank(doctorName)) {
            // 使用自定义SQL查询
            return prescriptionMapper.selectPrescriptionsByNamePage(
                page, prescriptionNo, patientId, doctorId, patientName, doctorName, 
                recordId, startDate, endDate, status);
        }
        
        Page<Prescription> resultPage = prescriptionMapper.selectPage(page, queryWrapper);
            
        // 填充关联信息
        fillPrescriptionsInfo(resultPage.getRecords());
        
        return resultPage;
    }
    
    /**
     * 获取患者处方列表
     */
    public List<Prescription> getPrescriptionsByPatient(Long patientId) {
        LambdaQueryWrapper<Prescription> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Prescription::getPatientId, patientId)
                   .orderByDesc(Prescription::getPrescriptionDate);
        
        List<Prescription> prescriptions = prescriptionMapper.selectList(queryWrapper);
        
        // 查询关联信息
        for (Prescription prescription : prescriptions) {
            // 查询医生信息
            Doctor doctor = doctorMapper.selectById(prescription.getDoctorId());
            prescription.setDoctor(doctor);
        }
        
        return prescriptions;
    }
    
    /**
     * 获取医生处方列表
     */
    public List<Prescription> getPrescriptionsByDoctor(Long doctorId, LocalDate date) {
        LambdaQueryWrapper<Prescription> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Prescription::getDoctorId, doctorId);
        
        if (date != null) {
            queryWrapper.eq(Prescription::getPrescriptionDate, date);
        }
        
        queryWrapper.orderByDesc(Prescription::getPrescriptionDate);
        
        List<Prescription> prescriptions = prescriptionMapper.selectList(queryWrapper);
        
        // 查询关联信息
        for (Prescription prescription : prescriptions) {
            // 查询患者信息
            Patient patient = patientMapper.selectById(prescription.getPatientId());
            prescription.setPatient(patient);
            
            // 查询就诊记录信息
            MedicalRecord medicalRecord = medicalRecordMapper.selectById(prescription.getRecordId());
            prescription.setMedicalRecord(medicalRecord);
        }
        
        return prescriptions;
    }
    
    /**
     * 获取就诊记录关联的处方列表
     */
    public List<Prescription> getPrescriptionsByRecord(Long recordId) {
        LambdaQueryWrapper<Prescription> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Prescription::getRecordId, recordId)
                   .orderByDesc(Prescription::getPrescriptionDate);
        
        List<Prescription> prescriptions = prescriptionMapper.selectList(queryWrapper);
        
        // 查询关联信息
        for (Prescription prescription : prescriptions) {
            // 查询患者信息
            Patient patient = patientMapper.selectById(prescription.getPatientId());
            prescription.setPatient(patient);
            
            // 查询医生信息
            Doctor doctor = doctorMapper.selectById(prescription.getDoctorId());
            prescription.setDoctor(doctor);
            
            // 查询处方明细
            LambdaQueryWrapper<PrescriptionDetail> detailQueryWrapper = new LambdaQueryWrapper<>();
            detailQueryWrapper.eq(PrescriptionDetail::getPrescriptionId, prescription.getId());
            List<PrescriptionDetail> details = prescriptionDetailMapper.selectList(detailQueryWrapper);
            
            // 查询药品信息
            for (PrescriptionDetail detail : details) {
                Medicine medicine = medicineMapper.selectById(detail.getMedicineId());
                detail.setMedicine(medicine);
            }
            
            prescription.setDetails(details);
        }
        
        return prescriptions;
    }
    
    /**
     * 获取处方明细
     */
    public PrescriptionDetail getPrescriptionDetailById(Long id) {
        PrescriptionDetail detail = prescriptionDetailMapper.selectById(id);
        if (detail == null) {
            throw new ServiceException("处方明细不存在");
        }
        
        // 查询药品信息
        Medicine medicine = medicineMapper.selectById(detail.getMedicineId());
        detail.setMedicine(medicine);
        
        return detail;
    }
    
    /**
     * 添加处方明细
     */
    @Transactional
    public PrescriptionDetail addPrescriptionDetail(PrescriptionDetail detail) {
        // 检查处方是否存在
        Prescription prescription = prescriptionMapper.selectById(detail.getPrescriptionId());
        if (prescription == null) {
            throw new ServiceException("处方不存在");
        }
        
        // 检查权限，如果是医生，只能添加自己处方的明细
        checkDoctorPermission(prescription);
        
        // 检查药品是否存在
        Medicine medicine = medicineMapper.selectById(detail.getMedicineId());
        if (medicine == null) {
            throw new ServiceException("药品不存在");
        }
        
        // 设置创建时间和更新时间
        LocalDateTime now = LocalDateTime.now();
        detail.setCreateTime(now);
        detail.setUpdateTime(now);
        
        if (prescriptionDetailMapper.insert(detail) <= 0) {
            throw new ServiceException("处方明细添加失败");
        }
        
        return detail;
    }
    
    /**
     * 更新处方明细
     */
    @Transactional
    public void updatePrescriptionDetail(Long id, PrescriptionDetail detail) {
        // 检查处方明细是否存在
        PrescriptionDetail existingDetail = prescriptionDetailMapper.selectById(id);
        if (existingDetail == null) {
            throw new ServiceException("处方明细不存在");
        }
        
        // 检查权限，如果是医生，只能更新自己处方的明细
        Prescription prescription = prescriptionMapper.selectById(existingDetail.getPrescriptionId());
        if (prescription != null) {
            checkDoctorPermission(prescription);
        }
        
        detail.setId(id);
        detail.setUpdateTime(LocalDateTime.now());
        
        if (prescriptionDetailMapper.updateById(detail) <= 0) {
            throw new ServiceException("处方明细更新失败");
        }
    }
    
    /**
     * 删除处方明细
     */
    @Transactional
    public void deletePrescriptionDetail(Long id) {
        // 检查处方明细是否存在
        PrescriptionDetail existingDetail = prescriptionDetailMapper.selectById(id);
        if (existingDetail == null) {
            throw new ServiceException("处方明细不存在");
        }
        
        // 检查权限，如果是医生，只能删除自己处方的明细
        Prescription prescription = prescriptionMapper.selectById(existingDetail.getPrescriptionId());
        if (prescription != null) {
            checkDoctorPermission(prescription);
        }
        
        if (prescriptionDetailMapper.deleteById(id) <= 0) {
            throw new ServiceException("处方明细删除失败");
        }
    }
    
    /**
     * 生成处方编号
     */
    private String generatePrescriptionNo() {
        // 生成格式：P + 年月日 + 6位随机数
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomStr = String.format("%06d", (int)(Math.random() * 1000000));
        return "P" + dateStr + randomStr;
    }
    
    /**
     * 填充处方关联信息
     */
    private void fillPrescriptionsInfo(List<Prescription> prescriptions) {
        if (prescriptions == null || prescriptions.isEmpty()) {
            return;
        }
        
        for (Prescription prescription : prescriptions) {
            // 查询患者信息
            Patient patient = patientMapper.selectById(prescription.getPatientId());
            prescription.setPatient(patient);
            
            // 查询医生信息
            Doctor doctor = doctorMapper.selectById(prescription.getDoctorId());
            prescription.setDoctor(doctor);
            
            // 查询就诊记录信息
            MedicalRecord medicalRecord = medicalRecordMapper.selectById(prescription.getRecordId());
            prescription.setMedicalRecord(medicalRecord);
            
            // 查询处方明细
            LambdaQueryWrapper<PrescriptionDetail> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(PrescriptionDetail::getPrescriptionId, prescription.getId());
            List<PrescriptionDetail> details = prescriptionDetailMapper.selectList(queryWrapper);
            
            // 填充药品信息
            for (PrescriptionDetail detail : details) {
                Medicine medicine = medicineMapper.selectById(detail.getMedicineId());
                detail.setMedicine(medicine);
            }
            
            prescription.setDetails(details);
        }
    }
    
    /**
     * 检查医生权限
     * 如果当前用户是医生，只能操作自己的处方
     */
    public void checkDoctorPermission(Prescription prescription) {
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
            if (currentDoctor != null && !currentDoctor.getId().equals(prescription.getDoctorId())) {
                throw new ServiceException("您只能操作自己的处方");
            }
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            // 不是医生，忽略异常
        }
    }
} 