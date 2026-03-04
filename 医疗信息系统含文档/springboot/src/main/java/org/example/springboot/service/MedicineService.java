package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.example.springboot.entity.Medicine;
import org.example.springboot.entity.MedicineCategory;
import org.example.springboot.entity.PrescriptionDetail;
import org.example.springboot.exception.ServiceException;
import org.example.springboot.mapper.MedicineMapper;
import org.example.springboot.mapper.PrescriptionDetailMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MedicineService {
    @Resource
    private MedicineMapper medicineMapper;
    
    @Resource
    private MedicineCategoryService medicineCategoryService;
    
    @Resource
    private PrescriptionDetailMapper prescriptionDetailMapper;
    
    /**
     * 新增药品
     */
    @Transactional
    public Medicine createMedicine(Medicine medicine) {
        // 检查药品编码是否已存在
        if (medicineMapper.selectOne(
                new LambdaQueryWrapper<Medicine>()
                    .eq(Medicine::getMedicineCode, medicine.getMedicineCode())
            ) != null) {
            throw new ServiceException("药品编码已存在");
        }
        
        // 确保category和categoryId同步
        syncCategoryInfo(medicine);
        
        // 设置创建时间和更新时间
        LocalDateTime now = LocalDateTime.now();
        medicine.setCreateTime(now);
        medicine.setUpdateTime(now);
        
        // 设置默认状态
        if (medicine.getStatus() == null) {
            medicine.setStatus(1); // 默认上架
        }
        
        if (medicineMapper.insert(medicine) <= 0) {
            throw new ServiceException("药品添加失败");
        }
        
        return medicine;
    }
    
    /**
     * 更新药品信息
     */
    @Transactional
    public void updateMedicine(Long id, Medicine medicine) {
        // 检查药品是否存在
        if (medicineMapper.selectById(id) == null) {
            throw new ServiceException("药品不存在");
        }
        
        // 如果修改了药品编码，检查是否与其他药品冲突
        if (StringUtils.isNotBlank(medicine.getMedicineCode())) {
            Medicine existMedicine = medicineMapper.selectOne(
                new LambdaQueryWrapper<Medicine>()
                    .eq(Medicine::getMedicineCode, medicine.getMedicineCode())
            );
            if (existMedicine != null && !existMedicine.getId().equals(id)) {
                throw new ServiceException("药品编码已被其他药品使用");
            }
        }
        
        // 确保category和categoryId同步
        syncCategoryInfo(medicine);
        
        medicine.setId(id);
        medicine.setUpdateTime(LocalDateTime.now());
        
        if (medicineMapper.updateById(medicine) <= 0) {
            throw new ServiceException("药品信息更新失败");
        }
    }
    
    /**
     * 同步药品分类信息
     * 确保category和categoryId保持一致
     */
    private void syncCategoryInfo(Medicine medicine) {
        // 如果设置了categoryId但没有设置category
        if (medicine.getCategoryId() != null && StringUtils.isBlank(medicine.getCategory())) {
            MedicineCategory category = medicineCategoryService.getById(medicine.getCategoryId());
            if (category != null) {
                medicine.setCategory(category.getCategoryName());
            }
        } 
        // 如果设置了category但没有设置categoryId
        else if (StringUtils.isNotBlank(medicine.getCategory()) && medicine.getCategoryId() == null) {
            List<MedicineCategory> categories = medicineCategoryService.getAllCategories();
            for (MedicineCategory category : categories) {
                if (category.getCategoryName().equals(medicine.getCategory())) {
                    medicine.setCategoryId(category.getId());
                    break;
                }
            }
        }
        // 两者都没设置时，设置默认值
        else if (StringUtils.isBlank(medicine.getCategory()) && medicine.getCategoryId() == null) {
            medicine.setCategory("未分类");
        }
    }
    
    /**
     * 获取药品详情
     */
    public Medicine getMedicineById(Long id) {
        Medicine medicine = medicineMapper.selectById(id);
        if (medicine == null) {
            throw new ServiceException("药品不存在");
        }
        return medicine;
    }
    
    /**
     * 获取所有药品列表
     */
    public List<Medicine> getAllMedicines() {
        return medicineMapper.selectList(
            new LambdaQueryWrapper<Medicine>()
                .orderByAsc(Medicine::getMedicineCode)
        );
    }
    
    /**
     * 根据分类ID查询药品列表
     */
    public List<Medicine> getMedicinesByCategory(Long categoryId) {
        if (categoryId == null) {
            return new ArrayList<>();
        }
        
        return medicineMapper.selectList(
            new LambdaQueryWrapper<Medicine>()
                .eq(Medicine::getCategoryId, categoryId)
                .eq(Medicine::getStatus, 1)
                .orderByAsc(Medicine::getMedicineCode)
        );
    }
    
    /**
     * 分页查询药品列表
     */
    public Page<Medicine> getMedicinesByPage(String medicineName, String medicineCode, String category, 
                                          Long categoryId, Integer status, 
                                          Integer currentPage, Integer size) {
        LambdaQueryWrapper<Medicine> queryWrapper = new LambdaQueryWrapper<>();
        
        // 添加查询条件
        if (StringUtils.isNotBlank(medicineName)) {
            queryWrapper.like(Medicine::getMedicineName, medicineName);
        }
        if (StringUtils.isNotBlank(medicineCode)) {
            queryWrapper.like(Medicine::getMedicineCode, medicineCode);
        }
        if (StringUtils.isNotBlank(category)) {
            queryWrapper.eq(Medicine::getCategory, category);
        }
        if (categoryId != null) {
            queryWrapper.eq(Medicine::getCategoryId, categoryId);
        }
        if (status != null) {
            queryWrapper.eq(Medicine::getStatus, status);
        }
        
        // 按药品编码排序
        queryWrapper.orderByAsc(Medicine::getMedicineCode);
        
        return medicineMapper.selectPage(new Page<>(currentPage, size), queryWrapper);
    }
    
    /**
     * 删除药品
     */
    @Transactional
    public void deleteMedicine(Long id) {
        // 检查药品是否存在
        Medicine medicine = medicineMapper.selectById(id);
        if (medicine == null) {
            throw new ServiceException("药品不存在");
        }
        
        // 检查药品是否被用于处方明细
        LambdaQueryWrapper<PrescriptionDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PrescriptionDetail::getMedicineId, id);
        long count = prescriptionDetailMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new ServiceException("该药品已被用于处方，不能删除");
        }
        
        if (medicineMapper.deleteById(id) <= 0) {
            throw new ServiceException("药品删除失败");
        }
    }
    
    /**
     * 更新药品库存
     */
    @Transactional
    public void updateStock(Long id, Integer stockChange) {
        Medicine medicine = getMedicineById(id);
        
        // 计算新库存
        int newStock = medicine.getStock() + stockChange;
        if (newStock < 0) {
            throw new ServiceException("库存不足");
        }
        
        Medicine updateMedicine = new Medicine();
        updateMedicine.setId(id);
        updateMedicine.setStock(newStock);
        updateMedicine.setUpdateTime(LocalDateTime.now());
        
        if (medicineMapper.updateById(updateMedicine) <= 0) {
            throw new ServiceException("库存更新失败");
        }
    }
    
    /**
     * 更新药品状态
     */
    @Transactional
    public void updateStatus(Long id, Integer status) {
        Medicine medicine = getMedicineById(id);
        
        Medicine updateMedicine = new Medicine();
        updateMedicine.setId(id);
        updateMedicine.setStatus(status);
        updateMedicine.setUpdateTime(LocalDateTime.now());
        
        if (medicineMapper.updateById(updateMedicine) <= 0) {
            throw new ServiceException("状态更新失败");
        }
    }
    
    /**
     * 获取药品分类列表
     */
    public List<String> getAllCategories() {
        // 使用药品分类表获取所有启用的分类
        List<MedicineCategory> categories = medicineCategoryService.getAllCategories();
        return categories.stream()
                .map(MedicineCategory::getCategoryName)
                .collect(Collectors.toList());
    }
} 