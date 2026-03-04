# 医生表（doctor）字段清单

来源：`hospital/医疗信息系统含文档/hospital_db.sql`

| 编号 | 字段名 | 类型 | 长度 | 是否非空 | 是否主键 | 注释 |
| --- | --- | --- | --- | --- | --- | --- |
| 1 | id | bigint | — | 是 | 是 | 医生ID |
| 2 | user_id | bigint | — | 是 | 否 | 用户ID |
| 3 | doctor_no | varchar | 20 | 是 | 否 | 医生编号 |
| 4 | name | varchar | 50 | 是 | 否 | 姓名 |
| 5 | department_id | bigint | — | 是 | 否 | 科室ID |
| 6 | title | varchar | 50 | 否 | 否 | 职称 |
| 7 | expertise | text | — | 否 | 否 | 专长 |
| 8 | introduction | text | — | 否 | 否 | 简介 |
| 9 | status | tinyint | — | 否 | 否 | 状态(0:离职,1:在职) |
| 10 | create_time | datetime | — | 否 | 否 | 创建时间 |
| 11 | update_time | datetime | — | 否 | 否 | 更新时间 |
