package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.example.springboot.entity.MedicineCategory;
import org.example.springboot.mapper.MedicineCategoryMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MedicineCategoryService {
    @Resource
    private MedicineCategoryMapper medicineCategoryMapper;

    /**
     * 新增药品分类
     */
    public boolean add(MedicineCategory medicineCategory) {
        return medicineCategoryMapper.insert(medicineCategory) > 0;
    }

    /**
     * 更新药品分类
     */
    public boolean update(MedicineCategory medicineCategory) {
        return medicineCategoryMapper.updateById(medicineCategory) > 0;
    }

    /**
     * 删除药品分类
     */
    public boolean delete(Long id) {
        return medicineCategoryMapper.deleteById(id) > 0;
    }

    /**
     * 根据ID获取药品分类
     */
    public MedicineCategory getById(Long id) {
        return medicineCategoryMapper.selectById(id);
    }

    /**
     * 获取所有药品分类
     */
    public List<MedicineCategory> getAllCategories() {
        return medicineCategoryMapper.selectList(
            new LambdaQueryWrapper<MedicineCategory>()
                .eq(MedicineCategory::getStatus, 1)
                .orderByAsc(MedicineCategory::getCategoryName)
        );
    }

    /**
     * 分页查询药品分类
     */
    public Page<MedicineCategory> getCategoriesByPage(String categoryName, String categoryCode, Integer status, 
                                            Integer currentPage, Integer size) {
        LambdaQueryWrapper<MedicineCategory> queryWrapper = new LambdaQueryWrapper<>();
        
        // 添加查询条件
        if (StringUtils.isNotBlank(categoryName)) {
            queryWrapper.like(MedicineCategory::getCategoryName, categoryName);
        }
        if (StringUtils.isNotBlank(categoryCode)) {
            queryWrapper.like(MedicineCategory::getCategoryCode, categoryCode);
        }
        if (status != null) {
            queryWrapper.eq(MedicineCategory::getStatus, status);
        }
        
        // 按创建时间降序排序
        queryWrapper.orderByDesc(MedicineCategory::getCreateTime);
        
        return medicineCategoryMapper.selectPage(new Page<>(currentPage, size), queryWrapper);
    }
    
    /**
     * 检查分类名称是否存在
     */
    public boolean checkCategoryNameExists(String categoryName, Long excludeId) {
        LambdaQueryWrapper<MedicineCategory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MedicineCategory::getCategoryName, categoryName);
        
        if (excludeId != null) {
            queryWrapper.ne(MedicineCategory::getId, excludeId);
        }
        
        return medicineCategoryMapper.selectCount(queryWrapper) > 0;
    }
    
    /**
     * 检查分类编码是否存在
     */
    public boolean checkCategoryCodeExists(String categoryCode, Long excludeId) {
        LambdaQueryWrapper<MedicineCategory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MedicineCategory::getCategoryCode, categoryCode);
        
        if (excludeId != null) {
            queryWrapper.ne(MedicineCategory::getId, excludeId);
        }
        
        return medicineCategoryMapper.selectCount(queryWrapper) > 0;
    }
} 