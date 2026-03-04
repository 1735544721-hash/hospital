package org.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.springboot.entity.Appointment;

import java.time.LocalDate;

@Mapper
public interface AppointmentMapper extends BaseMapper<Appointment> {
    /**
     * 根据患者姓名和医生姓名查询预约信息（分页）
     */
    @Select({
        "<script>",
        "SELECT a.* FROM appointment a",
        "LEFT JOIN patient p ON a.patient_id = p.id",
        "LEFT JOIN doctor d ON a.doctor_id = d.id",
        "WHERE 1=1",
        "<if test='patientId != null'>",
        "    AND a.patient_id = #{patientId}",
        "</if>",
        "<if test='doctorId != null'>",
        "    AND a.doctor_id = #{doctorId}",
        "</if>",
        "<if test='patientName != null and patientName != \"\"'>",
        "    AND p.name LIKE CONCAT('%', #{patientName}, '%')",
        "</if>",
        "<if test='doctorName != null and doctorName != \"\"'>",
        "    AND d.name LIKE CONCAT('%', #{doctorName}, '%')",
        "</if>",
        "<if test='startDate != null'>",
        "    AND a.appointment_date &gt;= #{startDate}",
        "</if>",
        "<if test='endDate != null'>",
        "    AND a.appointment_date &lt;= #{endDate}",
        "</if>",
        "<if test='status != null'>",
        "    AND a.status = #{status}",
        "</if>",
        "ORDER BY a.create_time DESC",
        "</script>"
    })
    Page<Appointment> selectAppointmentsByNamePage(Page<Appointment> page, 
                                          @Param("patientId") Long patientId,
                                          @Param("doctorId") Long doctorId,
                                          @Param("patientName") String patientName,
                                          @Param("doctorName") String doctorName,
                                          @Param("startDate") LocalDate startDate,
                                          @Param("endDate") LocalDate endDate,
                                          @Param("status") Integer status);
} 