package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.example.springboot.entity.Doctor;
import org.example.springboot.entity.Schedule;
import org.example.springboot.entity.Department;
import org.example.springboot.exception.ServiceException;
import org.example.springboot.mapper.DoctorMapper;
import org.example.springboot.mapper.ScheduleMapper;
import org.example.springboot.mapper.DepartmentMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ScheduleService {
    @Resource
    private ScheduleMapper scheduleMapper;
    
    @Resource
    private DoctorMapper doctorMapper;
    
    @Resource
    private DepartmentMapper departmentMapper;
    
    /**
     * 新增排班
     */
    @Transactional
    public Schedule createSchedule(Schedule schedule) {
        // 检查医生是否存在
        Doctor doctor = doctorMapper.selectById(schedule.getDoctorId());
        if (doctor == null) {
            throw new ServiceException("医生不存在");
        }
        
        // 检查是否已存在相同日期和时间段的排班
        LambdaQueryWrapper<Schedule> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Schedule::getDoctorId, schedule.getDoctorId())
                   .eq(Schedule::getScheduleDate, schedule.getScheduleDate())
                   .eq(Schedule::getTimeSlot, schedule.getTimeSlot());
        
        if (scheduleMapper.selectCount(queryWrapper) > 0) {
            throw new ServiceException("该医生在所选日期和时间段已有排班");
        }
        
        // 设置默认值
        if (schedule.getCurrentPatients() == null) {
            schedule.setCurrentPatients(0);
        }
        
        if (schedule.getStatus() == null) {
            schedule.setStatus(1); // 默认正常
        }
        
        // 设置创建时间和更新时间
        LocalDateTime now = LocalDateTime.now();
        schedule.setCreateTime(now);
        schedule.setUpdateTime(now);
        
        if (scheduleMapper.insert(schedule) <= 0) {
            throw new ServiceException("排班添加失败");
        }
        
        return schedule;
    }
    
    /**
     * 更新排班信息
     */
    @Transactional
    public void updateSchedule(Long id, Schedule schedule) {
        // 检查排班是否存在
        Schedule existingSchedule = scheduleMapper.selectById(id);
        if (existingSchedule == null) {
            throw new ServiceException("排班不存在");
        }
        
        // 如果修改了医生，检查医生是否存在
        if (schedule.getDoctorId() != null && !schedule.getDoctorId().equals(existingSchedule.getDoctorId())) {
            Doctor doctor = doctorMapper.selectById(schedule.getDoctorId());
            if (doctor == null) {
                throw new ServiceException("医生不存在");
            }
            
            // 检查是否已存在相同日期和时间段的排班
            LambdaQueryWrapper<Schedule> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Schedule::getDoctorId, schedule.getDoctorId())
                       .eq(Schedule::getScheduleDate, schedule.getScheduleDate() != null ? schedule.getScheduleDate() : existingSchedule.getScheduleDate())
                       .eq(Schedule::getTimeSlot, schedule.getTimeSlot() != null ? schedule.getTimeSlot() : existingSchedule.getTimeSlot())
                       .ne(Schedule::getId, id);
            
            if (scheduleMapper.selectCount(queryWrapper) > 0) {
                throw new ServiceException("该医生在所选日期和时间段已有排班");
            }
        }
        
        // 如果修改了最大接诊人数，确保不小于当前预约人数
        if (schedule.getMaxPatients() != null && schedule.getMaxPatients() < existingSchedule.getCurrentPatients()) {
            throw new ServiceException("最大接诊人数不能小于当前预约人数");
        }
        
        schedule.setId(id);
        schedule.setUpdateTime(LocalDateTime.now());
        
        if (scheduleMapper.updateById(schedule) <= 0) {
            throw new ServiceException("排班更新失败");
        }
    }
    
    /**
     * 获取排班详情
     */
    public Schedule getScheduleById(Long id) {
        Schedule schedule = scheduleMapper.selectById(id);
        if (schedule == null) {
            throw new ServiceException("排班不存在");
        }
        
        // 查询医生信息
        Doctor doctor = doctorMapper.selectById(schedule.getDoctorId());
        if (doctor != null) {
            // 查询科室信息
            if (doctor.getDepartmentId() != null) {
                Department department = departmentMapper.selectById(doctor.getDepartmentId());
                doctor.setDepartment(department);
            }
        schedule.setDoctor(doctor);
        }
        
        return schedule;
    }
    
    /**
     * 分页查询排班列表
     */
    public Page<Schedule> getSchedulesByPage(Long doctorId, LocalDate startDate, LocalDate endDate, 
                                          String timeSlot, Integer status, 
                                          Integer currentPage, Integer size) {
        LambdaQueryWrapper<Schedule> queryWrapper = new LambdaQueryWrapper<>();
        
        // 添加查询条件
        if (doctorId != null) {
            queryWrapper.eq(Schedule::getDoctorId, doctorId);
        }
        if (startDate != null) {
            queryWrapper.ge(Schedule::getScheduleDate, startDate);
        }
        if (endDate != null) {
            queryWrapper.le(Schedule::getScheduleDate, endDate);
        }
        if (timeSlot != null && !timeSlot.isEmpty()) {
            queryWrapper.eq(Schedule::getTimeSlot, timeSlot);
        }
        if (status != null) {
            queryWrapper.eq(Schedule::getStatus, status);
        }
        
        // 按日期和时间段排序
        queryWrapper.orderByAsc(Schedule::getScheduleDate)
                   .orderByAsc(Schedule::getTimeSlot);
        
        Page<Schedule> page = scheduleMapper.selectPage(new Page<>(currentPage, size), queryWrapper);
        
        // 查询医生信息
        for (Schedule schedule : page.getRecords()) {
            Doctor doctor = doctorMapper.selectById(schedule.getDoctorId());
            if (doctor != null) {
                // 查询科室信息
                if (doctor.getDepartmentId() != null) {
                    Department department = departmentMapper.selectById(doctor.getDepartmentId());
                    doctor.setDepartment(department);
                }
            schedule.setDoctor(doctor);
            }
        }
        
        return page;
    }
    
    /**
     * 获取医生排班列表
     */
    public List<Schedule> getSchedulesByDoctor(Long doctorId, LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<Schedule> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Schedule::getDoctorId, doctorId);
        
        if (startDate != null) {
            queryWrapper.ge(Schedule::getScheduleDate, startDate);
        }
        if (endDate != null) {
            queryWrapper.le(Schedule::getScheduleDate, endDate);
        }
        
        // 只查询状态正常的排班
        queryWrapper.eq(Schedule::getStatus, 1);
        
        // 按日期和时间段排序
        queryWrapper.orderByAsc(Schedule::getScheduleDate)
                   .orderByAsc(Schedule::getTimeSlot);
        
        List<Schedule> schedules = scheduleMapper.selectList(queryWrapper);
        
        // 查询医生信息
        Doctor doctor = doctorMapper.selectById(doctorId);
        if (doctor != null) {
            // 查询科室信息
            if (doctor.getDepartmentId() != null) {
                Department department = departmentMapper.selectById(doctor.getDepartmentId());
                doctor.setDepartment(department);
            }
        for (Schedule schedule : schedules) {
            schedule.setDoctor(doctor);
            }
        }
        
        return schedules;
    }
    
    /**
     * 获取某日排班列表
     */
    public List<Schedule> getSchedulesByDate(LocalDate date) {
        LambdaQueryWrapper<Schedule> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Schedule::getScheduleDate, date)
                   .eq(Schedule::getStatus, 1) // 只查询状态正常的排班
                   .orderByAsc(Schedule::getTimeSlot);
        
        List<Schedule> schedules = scheduleMapper.selectList(queryWrapper);
        
        // 查询医生信息
        for (Schedule schedule : schedules) {
            Doctor doctor = doctorMapper.selectById(schedule.getDoctorId());
            if (doctor != null) {
                // 查询科室信息
                if (doctor.getDepartmentId() != null) {
                    Department department = departmentMapper.selectById(doctor.getDepartmentId());
                    doctor.setDepartment(department);
                }
            schedule.setDoctor(doctor);
            }
        }
        
        return schedules;
    }
    
    /**
     * 更新排班状态
     */
    @Transactional
    public void updateScheduleStatus(Long id, Integer status) {
        Schedule schedule = scheduleMapper.selectById(id);
        if (schedule == null) {
            throw new ServiceException("排班不存在");
        }
        
        // 如果要停诊，检查是否已有预约
        if (status == 0 && schedule.getCurrentPatients() > 0) {
            throw new ServiceException("该排班已有患者预约，无法停诊");
        }
        
        Schedule updateSchedule = new Schedule();
        updateSchedule.setId(id);
        updateSchedule.setStatus(status);
        updateSchedule.setUpdateTime(LocalDateTime.now());
        
        if (scheduleMapper.updateById(updateSchedule) <= 0) {
            throw new ServiceException("排班状态更新失败");
        }
    }
    
    /**
     * 批量创建排班
     */
    @Transactional
    public void batchCreateSchedule(List<Schedule> schedules) {
        for (Schedule schedule : schedules) {
            createSchedule(schedule);
        }
    }
    
    /**
     * 删除排班
     */
    @Transactional
    public void deleteSchedule(Long id) {
        Schedule schedule = scheduleMapper.selectById(id);
        if (schedule == null) {
            throw new ServiceException("排班不存在");
        }
        
        // 检查是否已有预约
        if (schedule.getCurrentPatients() > 0) {
            throw new ServiceException("该排班已有患者预约，无法删除");
        }
        
        if (scheduleMapper.deleteById(id) <= 0) {
            throw new ServiceException("排班删除失败");
        }
    }
    
    /**
     * 更新排班预约人数
     */
    @Transactional
    public void updateSchedulePatients(Long id, int change) {
        Schedule schedule = scheduleMapper.selectById(id);
        if (schedule == null) {
            throw new ServiceException("排班不存在");
        }
        
        int newCount = schedule.getCurrentPatients() + change;
        
        // 检查是否超出最大接诊人数
        if (newCount > schedule.getMaxPatients()) {
            throw new ServiceException("超出最大接诊人数");
        }
        
        // 检查是否小于0
        if (newCount < 0) {
            throw new ServiceException("当前预约人数不能小于0");
        }
        
        Schedule updateSchedule = new Schedule();
        updateSchedule.setId(id);
        updateSchedule.setCurrentPatients(newCount);
        updateSchedule.setUpdateTime(LocalDateTime.now());
        
        if (scheduleMapper.updateById(updateSchedule) <= 0) {
            throw new ServiceException("排班预约人数更新失败");
        }
    }
} 