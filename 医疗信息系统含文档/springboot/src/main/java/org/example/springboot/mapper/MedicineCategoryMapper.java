package org.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.springboot.entity.MedicineCategory;
 
@Mapper
public interface MedicineCategoryMapper extends BaseMapper<MedicineCategory> {
    // 继承BaseMapper，无需编写SQL
} 