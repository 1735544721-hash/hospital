package org.example.springboot.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.example.springboot.common.Result;
import org.example.springboot.entity.Medicine;
import org.example.springboot.service.MedicineService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@Tag(name = "药品管理接口")
@RestController
@RequestMapping("/medicine")
public class MedicineController {
    private static final Logger LOGGER = LoggerFactory.getLogger(MedicineController.class);
    
    @Resource
    private MedicineService medicineService;
    
    @Operation(summary = "新增药品")
    @PostMapping
    public Result<?> createMedicine(@RequestBody Medicine medicine) {
        LOGGER.info("新增药品: {}", medicine.getMedicineName());
        Medicine newMedicine = medicineService.createMedicine(medicine);
        return Result.success(newMedicine);
    }
    
    @Operation(summary = "更新药品信息")
    @PutMapping("/{id}")
    public Result<?> updateMedicine(@PathVariable Long id, @RequestBody Medicine medicine) {
        LOGGER.info("更新药品信息: id={}, name={}", id, medicine.getMedicineName());
        medicineService.updateMedicine(id, medicine);
        return Result.success();
    }
    
    @Operation(summary = "获取药品详情")
    @GetMapping("/{id}")
    public Result<?> getMedicineById(@PathVariable Long id) {
        Medicine medicine = medicineService.getMedicineById(id);
        return Result.success(medicine);
    }
    
    @Operation(summary = "获取所有药品列表")
    @GetMapping("/list")
    public Result<?> getAllMedicines() {
        return Result.success(medicineService.getAllMedicines());
    }
    
    @Operation(summary = "分页查询药品列表")
    @GetMapping("/page")
    public Result<?> getMedicinesByPage(
            @RequestParam(required = false) String medicineName,
            @RequestParam(required = false) String medicineCode,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer currentPage,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(medicineService.getMedicinesByPage(medicineName, medicineCode, category, categoryId, status, currentPage, size));
    }
    
    @Operation(summary = "删除药品")
    @DeleteMapping("/{id}")
    public Result<?> deleteMedicine(@PathVariable Long id) {
        LOGGER.info("删除药品: id={}", id);
        medicineService.deleteMedicine(id);
        return Result.success();
    }
    
    @Operation(summary = "更新药品库存")
    @PutMapping("/{id}/stock")
    public Result<?> updateStock(@PathVariable Long id, @RequestParam Integer stockChange) {
        LOGGER.info("更新药品库存: id={}, stockChange={}", id, stockChange);
        medicineService.updateStock(id, stockChange);
        return Result.success();
    }
    
    @Operation(summary = "更新药品状态")
    @PutMapping("/{id}/status")
    public Result<?> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        LOGGER.info("更新药品状态: id={}, status={}", id, status);
        medicineService.updateStatus(id, status);
        return Result.success();
    }
    
    @Operation(summary = "获取所有药品分类")
    @GetMapping("/categories")
    public Result<?> getAllCategories() {
        return Result.success(medicineService.getAllCategories());
    }
    
    @Operation(summary = "根据分类ID获取药品列表")
    @GetMapping("/category/{categoryId}")
    public Result<?> getMedicinesByCategory(@PathVariable Long categoryId) {
        LOGGER.info("根据分类ID获取药品列表: categoryId={}", categoryId);
        return Result.success(medicineService.getMedicinesByCategory(categoryId));
    }
} 