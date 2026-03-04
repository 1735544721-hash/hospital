# 部门表（department）字段清单

来源：`hospital/医疗信息系统含文档/hospital_db.sql`

| 编号 | 字段名 | 类型 | 长度 | 是否非空 | 是否主键 | 注释 |
| --- | --- | --- | --- | --- | --- | --- |
| 1 | id | bigint | — | 是 | 是 | 部门ID |
| 2 | dept_name | varchar | 50 | 是 | 否 | 部门名称 |
| 3 | dept_code | varchar | 20 | 是 | 否 | 部门编码 |
| 4 | description | varchar | 200 | 否 | 否 | 部门描述 |
| 5 | director_id | bigint | — | 否 | 否 | 负责人ID |
| 6 | status | tinyint | — | 否 | 否 | 状态(0:禁用,1:启用) |
| 7 | create_time | datetime | — | 否 | 否 | 创建时间 |
| 8 | update_time | datetime | — | 否 | 否 | 更新时间 |
