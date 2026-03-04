package org.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.springboot.entity.Doctor;

@Mapper
public interface DoctorMapper extends BaseMapper<Doctor> {
    // 无需实现方法，使用MyBatis-Plus提供的方法
} 