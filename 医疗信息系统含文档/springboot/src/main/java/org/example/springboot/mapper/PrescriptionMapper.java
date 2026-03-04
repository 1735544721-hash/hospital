package org.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.springboot.entity.Prescription;

import java.time.LocalDate;

@Mapper
public interface PrescriptionMapper extends BaseMapper<Prescription> {
    /**
     * 根据处方号、患者姓名和医生姓名查询处方信息（分页）
     */
    @Select({
        "<script>",
        "SELECT p.* FROM prescription p",
        "LEFT JOIN patient pt ON p.patient_id = pt.id",
        "LEFT JOIN doctor d ON p.doctor_id = d.id",
        "WHERE 1=1",
        "<if test='prescriptionNo != null and prescriptionNo != \"\"'>",
        "    AND p.prescription_no LIKE CONCAT('%', #{prescriptionNo}, '%')",
        "</if>",
        "<if test='patientId != null'>",
        "    AND p.patient_id = #{patientId}",
        "</if>",
        "<if test='doctorId != null'>",
        "    AND p.doctor_id = #{doctorId}",
        "</if>",
        "<if test='patientName != null and patientName != \"\"'>",
        "    AND pt.name LIKE CONCAT('%', #{patientName}, '%')",
        "</if>",
        "<if test='doctorName != null and doctorName != \"\"'>",
        "    AND d.name LIKE CONCAT('%', #{doctorName}, '%')",
        "</if>",
        "<if test='recordId != null'>",
        "    AND p.record_id = #{recordId}",
        "</if>",
        "<if test='startDate != null'>",
        "    AND p.prescription_date &gt;= #{startDate}",
        "</if>",
        "<if test='endDate != null'>",
        "    AND p.prescription_date &lt;= #{endDate}",
        "</if>",
        "<if test='status != null'>",
        "    AND p.status = #{status}",
        "</if>",
        "ORDER BY p.create_time DESC",
        "</script>"
    })
    Page<Prescription> selectPrescriptionsByNamePage(Page<Prescription> page, 
                                           @Param("prescriptionNo") String prescriptionNo,
                                           @Param("patientId") Long patientId,
                                           @Param("doctorId") Long doctorId,
                                           @Param("patientName") String patientName,
                                           @Param("doctorName") String doctorName,
                                           @Param("recordId") Long recordId,
                                           @Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate,
                                           @Param("status") Integer status);
} 