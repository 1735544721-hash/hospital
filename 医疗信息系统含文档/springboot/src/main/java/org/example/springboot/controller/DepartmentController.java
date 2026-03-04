package org.example.springboot.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.example.springboot.common.Result;
import org.example.springboot.entity.Department;
import org.example.springboot.service.DepartmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@Tag(name = "科室管理接口")
@RestController
@RequestMapping("/department")
public class DepartmentController {
    private static final Logger LOGGER = LoggerFactory.getLogger(DepartmentController.class);
    
    @Resource
    private DepartmentService departmentService;
    
    @Operation(summary = "新增科室")
    @PostMapping
    public Result<?> createDepartment(@RequestBody Department department) {
        LOGGER.info("新增科室: {}", department);
        Department newDepartment = departmentService.createDepartment(department);
        return Result.success(newDepartment);
    }
    
    @Operation(summary = "更新科室信息")
    @PutMapping("/{id}")
    public Result<?> updateDepartment(@PathVariable Long id, @RequestBody Department department) {
        LOGGER.info("更新科室信息: id={}, department={}", id, department);
        departmentService.updateDepartment(id, department);
        return Result.success();
    }
    
    @Operation(summary = "获取科室详情")
    @GetMapping("/{id}")
    public Result<?> getDepartmentById(@PathVariable Long id) {
        Department department = departmentService.getDepartmentById(id);
        return Result.success(department);
    }
    
    @Operation(summary = "获取所有科室列表")
    @GetMapping("/list")
    public Result<?> getAllDepartments() {
        return Result.success(departmentService.getAllDepartments());
    }
    
    @Operation(summary = "分页查询科室列表")
    @GetMapping("/page")
    public Result<?> getDepartmentsByPage(
            @RequestParam(required = false) String deptName,
            @RequestParam(required = false) String deptCode,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer currentPage,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(departmentService.getDepartmentsByPage(deptName, deptCode, status, currentPage, size));
    }
    
    @Operation(summary = "删除科室")
    @DeleteMapping("/{id}")
    public Result<?> deleteDepartment(@PathVariable Long id) {
        LOGGER.info("删除科室: id={}", id);
        departmentService.deleteDepartment(id);
        return Result.success();
    }
} 