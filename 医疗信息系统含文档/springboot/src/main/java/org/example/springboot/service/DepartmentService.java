package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.example.springboot.entity.Department;
import org.example.springboot.entity.Doctor;
import org.example.springboot.exception.ServiceException;
import org.example.springboot.mapper.DepartmentMapper;
import org.example.springboot.mapper.DoctorMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DepartmentService {
    @Resource
    private DepartmentMapper departmentMapper;
    
    @Resource
    private DoctorMapper doctorMapper;
    
    /**
     * 新增科室
     */
    @Transactional
    public Department createDepartment(Department department) {
        // 检查科室编码是否已存在
        if (departmentMapper.selectOne(
                new LambdaQueryWrapper<Department>()
                    .eq(Department::getDeptCode, department.getDeptCode())
            ) != null) {
            throw new ServiceException("科室编码已存在");
        }
        
        // 设置创建时间
        department.setCreateTime(LocalDateTime.now());
        department.setUpdateTime(LocalDateTime.now());
        
        // 如果设置了负责人，检查负责人是否存在
        if (department.getDirectorId() != null) {
            Doctor doctor = doctorMapper.selectById(department.getDirectorId());
            if (doctor == null) {
                throw new ServiceException("负责人不存在");
            }
        }
        
        if (departmentMapper.insert(department) <= 0) {
            throw new ServiceException("科室添加失败");
        }
        
        return department;
    }
    
    /**
     * 更新科室信息
     */
    public void updateDepartment(Long id, Department department) {
        // 检查科室是否存在
        if (departmentMapper.selectById(id) == null) {
            throw new ServiceException("科室不存在");
        }
        
        // 如果修改了科室编码，检查是否与其他科室冲突
        if (StringUtils.isNotBlank(department.getDeptCode())) {
            Department existDept = departmentMapper.selectOne(
                new LambdaQueryWrapper<Department>()
                    .eq(Department::getDeptCode, department.getDeptCode())
            );
            if (existDept != null && !existDept.getId().equals(id)) {
                throw new ServiceException("科室编码已被其他科室使用");
            }
        }
        
        // 如果设置了负责人，检查负责人是否存在
        if (department.getDirectorId() != null) {
            Doctor doctor = doctorMapper.selectById(department.getDirectorId());
            if (doctor == null) {
                throw new ServiceException("负责人不存在");
            }
        }
        
        department.setId(id);
        department.setUpdateTime(LocalDateTime.now());
        
        if (departmentMapper.updateById(department) <= 0) {
            throw new ServiceException("科室信息更新失败");
        }
    }
    
    /**
     * 获取科室详情
     */
    public Department getDepartmentById(Long id) {
        Department department = departmentMapper.selectById(id);
        if (department == null) {
            throw new ServiceException("科室不存在");
        }
        
        // 如果有负责人，查询负责人信息
        if (department.getDirectorId() != null) {
            Doctor director = doctorMapper.selectById(department.getDirectorId());
            department.setDirector(director);
        }
        
        return department;
    }
    
    /**
     * 获取所有科室列表
     */
    public List<Department> getAllDepartments() {
        List<Department> departments = departmentMapper.selectList(
            new LambdaQueryWrapper<Department>()
                .orderByAsc(Department::getDeptCode)
        );
        
        // 查询负责人信息
        for (Department department : departments) {
            if (department.getDirectorId() != null) {
                Doctor director = doctorMapper.selectById(department.getDirectorId());
                department.setDirector(director);
            }
        }
        
        return departments;
    }
    
    /**
     * 分页查询科室列表
     */
    public Page<Department> getDepartmentsByPage(String deptName, String deptCode, Integer status, Integer currentPage, Integer size) {
        LambdaQueryWrapper<Department> queryWrapper = new LambdaQueryWrapper<>();
        
        // 添加查询条件
        if (StringUtils.isNotBlank(deptName)) {
            queryWrapper.like(Department::getDeptName, deptName);
        }
        if (StringUtils.isNotBlank(deptCode)) {
            queryWrapper.like(Department::getDeptCode, deptCode);
        }
        if (status != null) {
            queryWrapper.eq(Department::getStatus, status);
        }
        
        // 按科室编码排序
        queryWrapper.orderByAsc(Department::getDeptCode);
        
        Page<Department> page = departmentMapper.selectPage(new Page<>(currentPage, size), queryWrapper);
        
        // 查询负责人信息
        for (Department department : page.getRecords()) {
            if (department.getDirectorId() != null) {
                Doctor director = doctorMapper.selectById(department.getDirectorId());
                department.setDirector(director);
            }
        }
        
        return page;
    }
    
    /**
     * 删除科室
     */
    @Transactional
    public void deleteDepartment(Long id) {
        // 检查科室是否存在
        if (departmentMapper.selectById(id) == null) {
            throw new ServiceException("科室不存在");
        }
        
        // 检查科室下是否有医生
        long doctorCount = doctorMapper.selectCount(
            new LambdaQueryWrapper<Doctor>()
                .eq(Doctor::getDepartmentId, id)
        );
        if (doctorCount > 0) {
            throw new ServiceException("该科室下有医生，无法删除");
        }
        
        if (departmentMapper.deleteById(id) <= 0) {
            throw new ServiceException("科室删除失败");
        }
    }
} 