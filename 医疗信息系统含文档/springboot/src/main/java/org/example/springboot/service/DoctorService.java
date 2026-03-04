package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.StringUtils;
import org.example.springboot.entity.Department;
import org.example.springboot.entity.Doctor;
import org.example.springboot.entity.User;
import org.example.springboot.entity.Schedule;
import org.example.springboot.entity.Appointment;
import org.example.springboot.entity.MedicalRecord;
import org.example.springboot.entity.Prescription;
import org.example.springboot.exception.ServiceException;
import org.example.springboot.mapper.DepartmentMapper;
import org.example.springboot.mapper.DoctorMapper;
import org.example.springboot.mapper.UserMapper;
import org.example.springboot.mapper.ScheduleMapper;
import org.example.springboot.mapper.AppointmentMapper;
import org.example.springboot.mapper.MedicalRecordMapper;
import org.example.springboot.mapper.PrescriptionMapper;
import org.example.springboot.util.JwtTokenUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

@Service
public class DoctorService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DoctorService.class);
    
    @Resource
    private DoctorMapper doctorMapper;
    
    @Resource
    private DepartmentMapper departmentMapper;
    
    @Resource
    private UserMapper userMapper;
    
    @Resource
    private UserService userService;
    
    @Resource
    private ScheduleMapper scheduleMapper;
    
    @Resource
    private AppointmentMapper appointmentMapper;
    
    @Resource
    private MedicalRecordMapper medicalRecordMapper;
    
    @Resource
    private PrescriptionMapper prescriptionMapper;

    /**
     * 创建医生并同时创建用户
     */
    @Transactional
    public Doctor createDoctorWithUser(Doctor doctor, User user) {
        // 先创建用户
        user.setStatus(1); // 设置用户状态为启用
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        
        // 对密码进行加密处理
        // 这里应该有对密码的加密逻辑，如果没有请使用BCryptPasswordEncoder
        
        userMapper.insert(user);
        
        // 将创建的用户ID关联到医生
        doctor.setUserId(user.getId());
        
        // 创建医生
        return createDoctor(doctor);
    }

    /**
     * 创建医生
     */
    @Transactional
    public Doctor createDoctor(Doctor doctor) {
        // 检查科室是否存在
        Department department = departmentMapper.selectById(doctor.getDepartmentId());
        if (department == null) {
            throw new ServiceException("科室不存在");
        }
        
        // 如果关联了用户，检查用户是否存在
        if (doctor.getUserId() != null) {
            User user = userMapper.selectById(doctor.getUserId());
            if (user == null) {
                throw new ServiceException("关联的用户不存在");
            }
            
            // 检查该用户是否已经关联了医生
            LambdaQueryWrapper<Doctor> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Doctor::getUserId, doctor.getUserId());
            if (doctorMapper.selectCount(queryWrapper) > 0) {
                throw new ServiceException("该用户已关联其他医生");
            }
        }
        
        // 生成医生编号
        if (!StringUtils.isNotBlank(doctor.getDoctorNo())) {
            doctor.setDoctorNo(generateDoctorNo());
        }
        
        // 设置状态和时间
        if (doctor.getStatus() == null) {
            doctor.setStatus(1); // 默认在职
        }
        
        LocalDateTime now = LocalDateTime.now();
        doctor.setCreateTime(now);
        doctor.setUpdateTime(now);
        
        if (doctorMapper.insert(doctor) <= 0) {
            throw new ServiceException("医生添加失败");
        }
        
        return doctor;
    }

    /**
     * 更新医生信息
     */
    @Transactional
    public void updateDoctor(Long id, Doctor doctor) {
        // 检查医生是否存在
        Doctor existingDoctor = doctorMapper.selectById(id);
        if (existingDoctor == null) {
            throw new ServiceException("医生不存在");
        }
        
        // 如果修改了科室，检查科室是否存在
        if (doctor.getDepartmentId() != null && !doctor.getDepartmentId().equals(existingDoctor.getDepartmentId())) {
            Department department = departmentMapper.selectById(doctor.getDepartmentId());
            if (department == null) {
                throw new ServiceException("科室不存在");
            }
        }
        
        // 如果修改了关联用户，检查用户是否存在且未关联其他医生
        if (doctor.getUserId() != null && !doctor.getUserId().equals(existingDoctor.getUserId())) {
            User user = userMapper.selectById(doctor.getUserId());
            if (user == null) {
                throw new ServiceException("关联的用户不存在");
            }
            
            // 检查该用户是否已经关联了医生
            LambdaQueryWrapper<Doctor> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Doctor::getUserId, doctor.getUserId())
                       .ne(Doctor::getId, id);
            if (doctorMapper.selectCount(queryWrapper) > 0) {
                throw new ServiceException("该用户已关联其他医生");
            }
        }
        
        doctor.setId(id);
        doctor.setUpdateTime(LocalDateTime.now());
        
        if (doctorMapper.updateById(doctor) <= 0) {
            throw new ServiceException("医生信息更新失败");
        }
    }

    /**
     * 根据ID获取医生详情
     */
    public Doctor getDetailById(Long id) {
        Doctor doctor = doctorMapper.selectById(id);
        if (doctor == null) {
            throw new ServiceException("医生不存在");
        }
        
        // 获取科室信息
        if (doctor.getDepartmentId() != null) {
            Department department = departmentMapper.selectById(doctor.getDepartmentId());
            doctor.setDepartment(department);
        }
        
        // 获取用户信息
        if (doctor.getUserId() != null) {
            User user = userMapper.selectById(doctor.getUserId());
            doctor.setUser(user);
        }
        
        return doctor;
    }

    /**
     * 获取所有医生列表
     */
    public List<Doctor> getAllDoctors() {
        LambdaQueryWrapper<Doctor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Doctor::getStatus, 1) // 只查询在职医生
                   .orderByAsc(Doctor::getDepartmentId)
                   .orderByAsc(Doctor::getName);
        
        List<Doctor> doctors = doctorMapper.selectList(queryWrapper);
        
        // 获取关联信息
        for (Doctor doctor : doctors) {
            if (doctor.getDepartmentId() != null) {
                Department department = departmentMapper.selectById(doctor.getDepartmentId());
                doctor.setDepartment(department);
            }
            
            if (doctor.getUserId() != null) {
                User user = userMapper.selectById(doctor.getUserId());
                doctor.setUser(user);
            }
        }
        
        return doctors;
    }

    /**
     * 分页查询医生列表
     */
    public Page<Doctor> getDoctorsByPage(String name, String doctorNo, Long departmentId, 
                                         String title, Integer status, 
                                         Integer currentPage, Integer size) {
        Page<Doctor> page = new Page<>(currentPage, size);
        LambdaQueryWrapper<Doctor> queryWrapper = new LambdaQueryWrapper<>();
        
        // 添加查询条件
        if (StringUtils.isNotBlank(name)) {
            queryWrapper.like(Doctor::getName, name);
        }
        if (StringUtils.isNotBlank(doctorNo)) {
            queryWrapper.like(Doctor::getDoctorNo, doctorNo);
        }
        if (departmentId != null) {
            queryWrapper.eq(Doctor::getDepartmentId, departmentId);
        }
        if (StringUtils.isNotBlank(title)) {
            queryWrapper.like(Doctor::getTitle, title);
        }
        if (status != null) {
            queryWrapper.eq(Doctor::getStatus, status);
        }
        
        // 按创建时间降序排序
        queryWrapper.orderByDesc(Doctor::getCreateTime);
        
        Page<Doctor> resultPage = doctorMapper.selectPage(page, queryWrapper);
        
        // 获取关联信息
        for (Doctor doctor : resultPage.getRecords()) {
            if (doctor.getDepartmentId() != null) {
                Department department = departmentMapper.selectById(doctor.getDepartmentId());
                doctor.setDepartment(department);
            }
            
            if (doctor.getUserId() != null) {
                User user = userMapper.selectById(doctor.getUserId());
                doctor.setUser(user);
            }
        }
        
        return resultPage;
    }

    /**
     * 根据科室ID获取医生列表
     */
    public List<Doctor> getDoctorsByDepartment(Long departmentId) {
        LambdaQueryWrapper<Doctor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Doctor::getDepartmentId, departmentId);
        queryWrapper.eq(Doctor::getStatus, 1); // 只查询在职医生

        List<Doctor> doctors = doctorMapper.selectList(queryWrapper);
        for (Doctor doctor : doctors) {
            if (doctor.getDepartmentId() != null) {
                Department department = departmentMapper.selectById(doctor.getDepartmentId());
                doctor.setDepartment(department);
            }

            if (doctor.getUserId() != null) {
                User user = userMapper.selectById(doctor.getUserId());
                doctor.setUser(user);
            }
        }
        return doctors;
    }

    /**
     * 更新医生状态
     */
    @Transactional
    public void updateDoctorStatus(Long id, Integer status) {
        Doctor doctor = doctorMapper.selectById(id);
        if (doctor == null) {
            throw new ServiceException("医生不存在");
        }
        
        Doctor updateDoctor = new Doctor();
        updateDoctor.setId(id);
        updateDoctor.setStatus(status);
        updateDoctor.setUpdateTime(LocalDateTime.now());
        
        doctorMapper.updateById(updateDoctor);
    }

    /**
     * 删除医生
     */
    @Transactional
    public void deleteDoctor(Long id) {
        Doctor doctor = doctorMapper.selectById(id);
        if (doctor == null) {
            throw new ServiceException("医生不存在");
        }
        
        // 检查医生是否是科室负责人
        LambdaQueryWrapper<Department> departmentQueryWrapper = new LambdaQueryWrapper<>();
        departmentQueryWrapper.eq(Department::getDirectorId, id);
        long departmentCount = departmentMapper.selectCount(departmentQueryWrapper);
        if (departmentCount > 0) {
            throw new ServiceException("该医生是科室负责人，不能删除");
        }
        
        // 检查医生是否有关联排班
        LambdaQueryWrapper<Schedule> scheduleQueryWrapper = new LambdaQueryWrapper<>();
        scheduleQueryWrapper.eq(Schedule::getDoctorId, id);
        long scheduleCount = scheduleMapper.selectCount(scheduleQueryWrapper);
        if (scheduleCount > 0) {
            throw new ServiceException("该医生已有排班记录，不能删除");
        }
        
        // 检查医生是否有关联预约
        LambdaQueryWrapper<Appointment> appointmentQueryWrapper = new LambdaQueryWrapper<>();
        appointmentQueryWrapper.eq(Appointment::getDoctorId, id);
        long appointmentCount = appointmentMapper.selectCount(appointmentQueryWrapper);
        if (appointmentCount > 0) {
            throw new ServiceException("该医生已有预约记录，不能删除");
        }
        
        // 检查医生是否有关联就诊记录
        LambdaQueryWrapper<MedicalRecord> medicalRecordQueryWrapper = new LambdaQueryWrapper<>();
        medicalRecordQueryWrapper.eq(MedicalRecord::getDoctorId, id);
        long medicalRecordCount = medicalRecordMapper.selectCount(medicalRecordQueryWrapper);
        if (medicalRecordCount > 0) {
            throw new ServiceException("该医生已有就诊记录，不能删除");
        }
        
        // 检查医生是否有关联处方
        LambdaQueryWrapper<Prescription> prescriptionQueryWrapper = new LambdaQueryWrapper<>();
        prescriptionQueryWrapper.eq(Prescription::getDoctorId, id);
        long prescriptionCount = prescriptionMapper.selectCount(prescriptionQueryWrapper);
        if (prescriptionCount > 0) {
            throw new ServiceException("该医生已有处方记录，不能删除");
        }
        
        // 如果医生已关联用户，先解绑
        if (doctor.getUserId() != null) {
            User user = userMapper.selectById(doctor.getUserId());
            if (user != null) {
                // 解绑逻辑
            }
        }
        
        doctorMapper.deleteById(id);
    }

    /**
     * 绑定用户到医生
     */
    @Transactional
    public void bindUserToDoctor(Long doctorId, Long userId) {
        Doctor doctor = doctorMapper.selectById(doctorId);
        if (doctor == null) {
            throw new ServiceException("医生不存在");
        }
        
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new ServiceException("用户不存在");
        }
        
        // 检查用户是否已绑定其他医生
        LambdaQueryWrapper<Doctor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Doctor::getUserId, userId);
        queryWrapper.ne(Doctor::getId, doctorId);
        Long count = doctorMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new ServiceException("该用户已绑定其他医生");
        }
        
        // 更新医生关联的用户ID
        Doctor updateDoctor = new Doctor();
        updateDoctor.setId(doctorId);
        updateDoctor.setUserId(userId);
        updateDoctor.setUpdateTime(LocalDateTime.now());
        doctorMapper.updateById(updateDoctor);
    }

    /**
     * 解绑用户
     */
    @Transactional
    public void unbindUserFromDoctor(Long doctorId) {
        Doctor doctor = doctorMapper.selectById(doctorId);
        if (doctor == null) {
            throw new ServiceException("医生不存在");
        }
        
        Doctor updateDoctor = new Doctor();
        updateDoctor.setId(doctorId);
        updateDoctor.setUserId(null);
        updateDoctor.setUpdateTime(LocalDateTime.now());
        
        doctorMapper.updateById(updateDoctor);
    }

    /**
     * 生成医生编号
     */
    private String generateDoctorNo() {
        // 生成格式为 "D" + 年月日 + 4位序号的医生编号
        String prefix = "D" + LocalDateTime.now().toString().substring(0, 10).replace("-", "");
        
        // 查询当天最大序号
        LambdaQueryWrapper<Doctor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.likeRight(Doctor::getDoctorNo, prefix)
                   .orderByDesc(Doctor::getDoctorNo)
                   .last("limit 1");
        Doctor latestDoctor = doctorMapper.selectOne(queryWrapper);
        
        int sequence = 1;
        if (latestDoctor != null && latestDoctor.getDoctorNo().length() >= prefix.length() + 4) {
            String sequenceStr = latestDoctor.getDoctorNo().substring(prefix.length());
            try {
                sequence = Integer.parseInt(sequenceStr) + 1;
            } catch (NumberFormatException e) {
                // 忽略解析错误
            }
        }
        
        return prefix + String.format("%04d", sequence);
    }

    /**
     * 通过用户ID获取医生信息
     */
    public Doctor getDoctorByUserId(Long userId) {

        LambdaQueryWrapper<Doctor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Doctor::getUserId, userId);
        Doctor doctor = doctorMapper.selectOne(queryWrapper);

        if (doctor == null) {
            throw new ServiceException("未找到该用户关联的医生信息：{}"+userId);
        }
        
        // 查询科室信息
        if (doctor.getDepartmentId() != null) {
            Department department = departmentMapper.selectById(doctor.getDepartmentId());
            doctor.setDepartment(department);
        }
        
        // 查询用户信息
        User user = userMapper.selectById(userId);
        doctor.setUser(user);
        
        return doctor;
    }
} 