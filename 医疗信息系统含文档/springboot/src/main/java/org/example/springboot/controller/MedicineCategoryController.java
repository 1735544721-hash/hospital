package org.example.springboot.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.example.springboot.common.Result;
import org.example.springboot.entity.MedicineCategory;
import org.example.springboot.service.MedicineCategoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name="药品分类管理接口")
@RestController
@RequestMapping("/medicine-category")
public class MedicineCategoryController {
    private static final Logger LOGGER = LoggerFactory.getLogger(MedicineCategoryController.class);
    
    @Resource
    private MedicineCategoryService medicineCategoryService;
    
    @Operation(summary = "新增药品分类")
    @PostMapping
    public Result<?> addCategory(@RequestBody MedicineCategory medicineCategory) {
        LOGGER.info("新增药品分类: {}", medicineCategory.getCategoryName());
        
        // 检查分类名称是否已存在
        if (medicineCategoryService.checkCategoryNameExists(medicineCategory.getCategoryName(), null)) {
            return Result.error("分类名称已存在");
        }
        
        // 检查分类编码是否已存在
        if (medicineCategoryService.checkCategoryCodeExists(medicineCategory.getCategoryCode(), null)) {
            return Result.error("分类编码已存在");
        }
        
        boolean success = medicineCategoryService.add(medicineCategory);
        return success ? Result.success(medicineCategory) : Result.error("新增失败");
    }
    
    @Operation(summary = "更新药品分类")
    @PutMapping("/{id}")
    public Result<?> updateCategory(@PathVariable Long id, @RequestBody MedicineCategory medicineCategory) {
        LOGGER.info("更新药品分类: {}", id);
        medicineCategory.setId(id);
        
        // 检查分类是否存在
        MedicineCategory existingCategory = medicineCategoryService.getById(id);
        if (existingCategory == null) {
            return Result.error("分类不存在");
        }
        
        // 检查分类名称是否已存在
        if (medicineCategoryService.checkCategoryNameExists(medicineCategory.getCategoryName(), id)) {
            return Result.error("分类名称已存在");
        }
        
        // 检查分类编码是否已存在
        if (medicineCategoryService.checkCategoryCodeExists(medicineCategory.getCategoryCode(), id)) {
            return Result.error("分类编码已存在");
        }
        
        boolean success = medicineCategoryService.update(medicineCategory);
        return success ? Result.success(medicineCategory) : Result.error("更新失败");
    }
    
    @Operation(summary = "删除药品分类")
    @DeleteMapping("/{id}")
    public Result<?> deleteCategory(@PathVariable Long id) {
        LOGGER.info("删除药品分类: {}", id);
        boolean success = medicineCategoryService.delete(id);
        return success ? Result.success() : Result.error("删除失败");
    }
    
    @Operation(summary = "获取药品分类详情")
    @GetMapping("/{id}")
    public Result<?> getCategoryById(@PathVariable Long id) {
        LOGGER.info("获取药品分类详情: {}", id);
        MedicineCategory category = medicineCategoryService.getById(id);
        return category != null ? Result.success(category) : Result.error("分类不存在");
    }
    
    @Operation(summary = "获取所有药品分类")
    @GetMapping("/list")
    public Result<?> getAllCategories() {
        LOGGER.info("获取所有药品分类");
        List<MedicineCategory> categories = medicineCategoryService.getAllCategories();
        return Result.success(categories);
    }
    
    @Operation(summary = "分页查询药品分类")
    @GetMapping("/page")
    public Result<?> getCategoriesByPage(
            @RequestParam(required = false) String categoryName,
            @RequestParam(required = false) String categoryCode,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer currentPage,
            @RequestParam(defaultValue = "10") Integer size) {
        LOGGER.info("分页查询药品分类: categoryName={}, categoryCode={}, status={}, page={}, size={}", 
                    categoryName, categoryCode, status, currentPage, size);
        return Result.success(medicineCategoryService.getCategoriesByPage(categoryName, categoryCode, status, currentPage, size));
    }
} 