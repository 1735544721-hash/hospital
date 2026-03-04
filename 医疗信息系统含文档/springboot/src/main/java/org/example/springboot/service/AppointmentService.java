package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.example.springboot.entity.Appointment;
import org.example.springboot.entity.Doctor;
import org.example.springboot.entity.Patient;
import org.example.springboot.entity.Schedule;
import org.example.springboot.entity.Department;
import org.example.springboot.entity.User;
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
public class AppointmentService {
    @Resource
    private AppointmentMapper appointmentMapper;
    
    @Resource
    private PatientMapper patientMapper;
    
    @Resource
    private DoctorMapper doctorMapper;
    
    @Resource
    private ScheduleMapper scheduleMapper;
    
    @Resource
    private DepartmentMapper departmentMapper;
    
    @Resource
    private ScheduleService scheduleService;
    
    @Resource
    private DoctorService doctorService;
    @Autowired
    private UserMapper userMapper;

    /**
     * 创建预约
     */
    @Transactional
    public Appointment createAppointment(Appointment appointment) {
        // 检查患者是否存在
        Patient patient = patientMapper.selectById(appointment.getPatientId());
        if (patient == null) {
            throw new ServiceException("患者不存在");
        }
        
        // 检查医生是否存在
        Doctor doctor = doctorMapper.selectById(appointment.getDoctorId());
        if (doctor == null) {
            throw new ServiceException("医生不存在");
        }
        
        // 检查排班是否存在
        Schedule schedule = scheduleMapper.selectById(appointment.getScheduleId());
        if (schedule == null) {
            throw new ServiceException("排班不存在");
        }
        
        // 检查排班状态是否正常
        if (schedule.getStatus() != 1) {
            throw new ServiceException("该排班已停诊，无法预约");
        }
        
        // 检查是否有剩余预约名额
        if (schedule.getCurrentPatients() >= schedule.getMaxPatients()) {
            throw new ServiceException("该排班已满，无法预约");
        }
        
        // 检查患者是否已经预约过同一天同一医生的门诊
        LambdaQueryWrapper<Appointment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Appointment::getPatientId, appointment.getPatientId())
                   .eq(Appointment::getDoctorId, appointment.getDoctorId())
                   .eq(Appointment::getAppointmentDate, schedule.getScheduleDate())
                   .eq(Appointment::getStatus, 1); // 待就诊状态
        
        if (appointmentMapper.selectCount(queryWrapper) > 0) {
            throw new ServiceException("您已预约过该医生当天的门诊");
        }
        
        // 生成预约编号
        appointment.setAppointmentNo(generateAppointmentNo());
        
        // 设置预约日期和时间段
        appointment.setAppointmentDate(schedule.getScheduleDate());
        appointment.setTimeSlot(schedule.getTimeSlot());
        
        // 设置状态为待就诊
        appointment.setStatus(1);
        
        // 设置创建时间和更新时间
        LocalDateTime now = LocalDateTime.now();
        appointment.setCreateTime(now);
        appointment.setUpdateTime(now);
        
        // 保存预约信息
        if (appointmentMapper.insert(appointment) <= 0) {
            throw new ServiceException("预约失败");
        }
        
        // 更新排班的当前预约人数
        scheduleService.updateSchedulePatients(schedule.getId(), 1);
        
        return appointment;
    }
    
    /**
     * 取消预约
     */
    @Transactional
    public void cancelAppointment(Long id) {
        Appointment appointment = appointmentMapper.selectById(id);
        if (appointment == null) {
            throw new ServiceException("预约不存在");
        }
        
        // 检查权限，如果是医生，只能取消自己的预约
        checkDoctorPermission(appointment);
        
        // 检查预约状态是否为待就诊
        if (appointment.getStatus() != 1) {
            throw new ServiceException("只能取消待就诊的预约");
        }
        
        // 检查预约日期是否已过
        if (appointment.getAppointmentDate().isBefore(LocalDate.now())) {
            throw new ServiceException("无法取消过期的预约");
        }
        
        // 更新预约状态为已取消
        Appointment updateAppointment = new Appointment();
        updateAppointment.setId(id);
        updateAppointment.setStatus(0);
        updateAppointment.setUpdateTime(LocalDateTime.now());
        
        if (appointmentMapper.updateById(updateAppointment) <= 0) {
            throw new ServiceException("取消预约失败");
        }
        
        // 更新排班的当前预约人数
        scheduleService.updateSchedulePatients(appointment.getScheduleId(), -1);
    }
    
    /**
     * 完成就诊
     */
    @Transactional
    public void completeAppointment(Long id) {
        Appointment appointment = appointmentMapper.selectById(id);
        if (appointment == null) {
            throw new ServiceException("预约不存在");
        }
        
        // 检查权限，如果是医生，只能完成自己的预约
        checkDoctorPermission(appointment);
        
        // 检查预约状态是否为待就诊
        if (appointment.getStatus() != 1) {
            throw new ServiceException("只能完成待就诊的预约");
        }
        
        // 更新预约状态为已就诊
        Appointment updateAppointment = new Appointment();
        updateAppointment.setId(id);
        updateAppointment.setStatus(2);
        updateAppointment.setUpdateTime(LocalDateTime.now());
        
        if (appointmentMapper.updateById(updateAppointment) <= 0) {
            throw new ServiceException("完成就诊失败");
        }
    }
    
    /**
     * 获取预约详情
     */
    public Appointment getAppointmentById(Long id) {
        Appointment appointment = appointmentMapper.selectById(id);
        if (appointment == null) {
            throw new ServiceException("预约不存在");
        }
        
        // 检查权限，如果是医生，只能查看自己的预约
        checkDoctorPermission(appointment);
        
        // 查询患者信息
        Patient patient = patientMapper.selectById(appointment.getPatientId());
        appointment.setPatient(patient);
        
        // 查询医生信息
        Doctor doctor = doctorMapper.selectById(appointment.getDoctorId());
        if (doctor != null) {
            // 查询科室信息
            if (doctor.getDepartmentId() != null) {
                Department department = departmentMapper.selectById(doctor.getDepartmentId());
                doctor.setDepartment(department);
            }
            appointment.setDoctor(doctor);
        }
        
        // 查询排班信息
        Schedule schedule = scheduleMapper.selectById(appointment.getScheduleId());
        appointment.setSchedule(schedule);
        
        return appointment;
    }
    
    /**
     * 分页查询预约列表
     */
    public Page<Appointment> getAppointmentsByPage(Long patientId, Long doctorId, String patientName, String doctorName,
                                               LocalDate startDate, LocalDate endDate, Integer status, 
                                               Integer currentPage, Integer size) {
        Page<Appointment> page = new Page<>(currentPage, size);
        
        // 检查当前用户是否是医生，如果是医生，只能查看自己的预约
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
        
        LambdaQueryWrapper<Appointment> queryWrapper = new LambdaQueryWrapper<>();
        
        // 添加查询条件
        if (patientId != null) {
            queryWrapper.eq(Appointment::getPatientId, patientId);
        }
        
        if (doctorId != null) {
            queryWrapper.eq(Appointment::getDoctorId, doctorId);
        }
        
        if (startDate != null) {
            queryWrapper.ge(Appointment::getAppointmentDate, startDate);
        }
        
        if (endDate != null) {
            queryWrapper.le(Appointment::getAppointmentDate, endDate);
        }
        
        if (status != null) {
            queryWrapper.eq(Appointment::getStatus, status);
        }
        if(StringUtils.isNotBlank(patientName)){
            List<Long> ids = patientMapper.selectList(new LambdaQueryWrapper<Patient>().like(Patient::getName, patientName)).stream().map(Patient::getId).toList();
            if(!ids.isEmpty()){
                queryWrapper.in(Appointment::getPatientId, ids);
            }else{
                return new Page<>(currentPage, size);
            }

        }
        if(StringUtils.isNotBlank(doctorName)){
            List<Long> ids = doctorMapper.selectList(new LambdaQueryWrapper<Doctor>().like(Doctor::getName, doctorName)).stream().map(Doctor::getId).toList();
            if(!ids.isEmpty()){
                queryWrapper.in(Appointment::getDoctorId, ids);
            }else{
                return new Page<>(currentPage, size);
            }

        }
        
        // 按创建时间降序排序
        queryWrapper.orderByDesc(Appointment::getCreateTime);

        
        Page<Appointment> resultPage = appointmentMapper.selectPage(page, queryWrapper);
        
        // 填充患者和医生信息
        fillAppointmentsInfo(resultPage.getRecords());
        
        return resultPage;
    }

    public List<Appointment> getNoCancel(String patientId,String doctorId) {

        LambdaQueryWrapper<Appointment> queryWrapper = new LambdaQueryWrapper<>();

        // 添加查询条件
        if (patientId != null) {
            queryWrapper.eq(Appointment::getPatientId, patientId);
        }

        if (doctorId != null) {
            queryWrapper.eq(Appointment::getDoctorId, doctorId);
        }
        queryWrapper.ne(Appointment::getStatus, 0);
        List<Appointment> appointments = appointmentMapper.selectList(queryWrapper);
        fillAppointmentsInfo(appointments);
        return appointments;

    }
    
    /**
     * 获取患者预约列表
     */
    public List<Appointment> getAppointmentsByPatient(Long patientId) {
        LambdaQueryWrapper<Appointment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Appointment::getPatientId, patientId)
                   .orderByDesc(Appointment::getAppointmentDate)
                   .orderByAsc(Appointment::getTimeSlot);
        
        List<Appointment> appointments = appointmentMapper.selectList(queryWrapper);
        
        // 查询关联信息
        for (Appointment appointment : appointments) {
            // 查询医生信息
            Doctor doctor = doctorMapper.selectById(appointment.getDoctorId());
            if (doctor != null) {
                // 查询科室信息
                if (doctor.getDepartmentId() != null) {
                    Department department = departmentMapper.selectById(doctor.getDepartmentId());
                    doctor.setDepartment(department);
                }
                appointment.setDoctor(doctor);
            }
            
            // 查询排班信息
            Schedule schedule = scheduleMapper.selectById(appointment.getScheduleId());
            appointment.setSchedule(schedule);
        }
        
        return appointments;
    }
    
    /**
     * 获取医生预约列表
     */
    public List<Appointment> getAppointmentsByDoctor(Long doctorId, LocalDate date) {
        LambdaQueryWrapper<Appointment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Appointment::getDoctorId, doctorId);
        
        if (date != null) {
            queryWrapper.eq(Appointment::getAppointmentDate, date);
        }
        
        queryWrapper.orderByAsc(Appointment::getAppointmentDate)
                   .orderByAsc(Appointment::getTimeSlot);
        
        List<Appointment> appointments = appointmentMapper.selectList(queryWrapper);
        
        // 查询关联信息
        for (Appointment appointment : appointments) {
            // 查询患者信息
            Patient patient = patientMapper.selectById(appointment.getPatientId());
            appointment.setPatient(patient);
            
            // 查询排班信息
            Schedule schedule = scheduleMapper.selectById(appointment.getScheduleId());
            appointment.setSchedule(schedule);
        }
        
        return appointments;
    }
    
    /**
     * 更新预约信息
     */
    @Transactional
    public void updateAppointment(Long id, Appointment appointment) {
        Appointment existingAppointment = appointmentMapper.selectById(id);
        if (existingAppointment == null) {
            throw new ServiceException("预约不存在");
        }
        
        // 检查权限，如果是医生，只能更新自己的预约
        checkDoctorPermission(existingAppointment);
        
        // 检查预约状态是否为待就诊
        if (existingAppointment.getStatus() != 1) {
            throw new ServiceException("只能修改待就诊的预约");
        }
        
        // 检查预约日期是否已过
        if (existingAppointment.getAppointmentDate().isBefore(LocalDate.now())) {
            throw new ServiceException("无法修改过期的预约");
        }
        
        // 如果修改了排班
        if (appointment.getScheduleId() != null && !appointment.getScheduleId().equals(existingAppointment.getScheduleId())) {
            // 检查新排班是否存在
            Schedule newSchedule = scheduleMapper.selectById(appointment.getScheduleId());
            if (newSchedule == null) {
                throw new ServiceException("排班不存在");
            }
            
            // 检查新排班状态是否正常
            if (newSchedule.getStatus() != 1) {
                throw new ServiceException("该排班已停诊，无法预约");
            }
            
            // 检查新排班是否有剩余预约名额
            if (newSchedule.getCurrentPatients() >= newSchedule.getMaxPatients()) {
                throw new ServiceException("该排班已满，无法预约");
            }
            
            // 更新原排班的当前预约人数
            scheduleService.updateSchedulePatients(existingAppointment.getScheduleId(), -1);
            
            // 更新新排班的当前预约人数
            scheduleService.updateSchedulePatients(appointment.getScheduleId(), 1);
            
            // 设置新的预约日期和时间段
            appointment.setAppointmentDate(newSchedule.getScheduleDate());
            appointment.setTimeSlot(newSchedule.getTimeSlot());
        }
        
        appointment.setId(id);
        appointment.setUpdateTime(LocalDateTime.now());
        
        if (appointmentMapper.updateById(appointment) <= 0) {
            throw new ServiceException("更新预约失败");
        }
    }
    
    /**
     * 生成预约编号
     */
    private String generateAppointmentNo() {
        // 生成格式：A + 年月日 + 6位随机数
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomStr = String.format("%06d", (int)(Math.random() * 1000000));
        return "A" + dateStr + randomStr;
    }
    
    /**
     * 填充预约关联信息
     */
    private void fillAppointmentsInfo(List<Appointment> appointments) {
        if (appointments == null || appointments.isEmpty()) {
            return;
        }
        
        for (Appointment appointment : appointments) {
            // 查询患者信息
            Patient patient = patientMapper.selectById(appointment.getPatientId());
            appointment.setPatient(patient);
            
            // 查询医生信息
            Doctor doctor = doctorMapper.selectById(appointment.getDoctorId());
            if (doctor != null) {
                // 查询科室信息
                if (doctor.getDepartmentId() != null) {
                    Department department = departmentMapper.selectById(doctor.getDepartmentId());
                    doctor.setDepartment(department);
                }
                appointment.setDoctor(doctor);
            }
            
            // 查询排班信息
            Schedule schedule = scheduleMapper.selectById(appointment.getScheduleId());
            appointment.setSchedule(schedule);
        }
    }
    
    /**
     * 检查医生权限
     * 如果当前用户是医生，只能操作自己的预约
     */
    private void checkDoctorPermission(Appointment appointment) {
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
            if (currentDoctor != null && !currentDoctor.getId().equals(appointment.getDoctorId())) {
                throw new ServiceException("您只能操作自己的预约");
            }
        } catch (Exception e) {
            // 不是医生，忽略异常
        }
    }
} 