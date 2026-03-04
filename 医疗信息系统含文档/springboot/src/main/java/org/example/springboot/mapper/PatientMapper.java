package org.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.springboot.entity.Patient;

@Mapper
public interface PatientMapper extends BaseMapper<Patient> {
    // 无需实现方法，使用MyBatis-Plus提供的方法
    
    /**
     * 根据用户名查询患者信息（分页）
     */
    @Select({
        "<script>",
        "SELECT p.* FROM patient p",
        "LEFT JOIN user u ON p.user_id = u.id",
        "WHERE 1=1",
        "<if test='name != null and name != \"\"'>",
        "    AND p.name LIKE CONCAT('%', #{name}, '%')",
        "</if>",
        "<if test='idCard != null and idCard != \"\"'>",
        "    AND p.id_card LIKE CONCAT('%', #{idCard}, '%')",
        "</if>",
        "<if test='phone != null and phone != \"\"'>",
        "    AND p.phone LIKE CONCAT('%', #{phone}, '%')",
        "</if>",
        "<if test='username != null and username != \"\"'>",
        "    AND u.username LIKE CONCAT('%', #{username}, '%')",
        "</if>",
        "ORDER BY p.create_time DESC",
        "</script>"
    })
    Page<Patient> selectPatientsByUsername(Page<Patient> page, 
                                          @Param("name") String name, 
                                          @Param("idCard") String idCard, 
                                          @Param("phone") String phone, 
                                          @Param("username") String username);
} 