package org.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.springboot.entity.MedicalRecord;

import java.time.LocalDate;

@Mapper
public interface MedicalRecordMapper extends BaseMapper<MedicalRecord> {
    /**
     * 根据患者姓名和医生姓名查询就诊记录信息（分页）
     */
    @Select({
        "<script>",
        "SELECT m.* FROM medical_record m",
        "LEFT JOIN patient p ON m.patient_id = p.id",
        "LEFT JOIN doctor d ON m.doctor_id = d.id",
        "WHERE 1=1",
        "<if test='patientId != null'>",
        "    AND m.patient_id = #{patientId}",
        "</if>",
        "<if test='doctorId != null'>",
        "    AND m.doctor_id = #{doctorId}",
        "</if>",
        "<if test='patientName != null and patientName != \"\"'>",
        "    AND p.name LIKE CONCAT('%', #{patientName}, '%')",
        "</if>",
        "<if test='doctorName != null and doctorName != \"\"'>",
        "    AND d.name LIKE CONCAT('%', #{doctorName}, '%')",
        "</if>",
        "<if test='startDate != null'>",
        "    AND m.record_date &gt;= #{startDate}",
        "</if>",
        "<if test='endDate != null'>",
        "    AND m.record_date &lt;= #{endDate}",
        "</if>",
        "ORDER BY m.create_time DESC",
        "</script>"
    })
    Page<MedicalRecord> selectMedicalRecordsByNamePage(Page<MedicalRecord> page,
                                              @Param("patientId") Long patientId,
                                              @Param("doctorId") Long doctorId,
                                              @Param("patientName") String patientName,
                                              @Param("doctorName") String doctorName,
                                              @Param("startDate") LocalDate startDate,
                                              @Param("endDate") LocalDate endDate);
} 