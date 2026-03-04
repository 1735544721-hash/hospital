# 患者表（patient）字段清单

来源：`hospital/医疗信息系统含文档/hospital_db.sql`

| 编号 | 字段名 | 类型 | 长度 | 是否非空 | 是否主键 | 注释 |
| --- | --- | --- | --- | --- | --- | --- |
| 1 | id | bigint | — | 是 | 是 | 患者ID |
| 2 | user_id | bigint | — | 否 | 否 | 用户ID |
| 3 | patient_no | varchar | 20 | 是 | 否 | 患者编号 |
| 4 | name | varchar | 50 | 是 | 否 | 姓名 |
| 5 | id_card | varchar | 20 | 否 | 否 | 身份证号 |
| 6 | birthday | date | — | 否 | 否 | 出生日期 |
| 7 | sex | varchar | 10 | 否 | 否 | 性别 |
| 8 | phone | varchar | 20 | 否 | 否 | 联系电话 |
| 9 | address | varchar | 200 | 否 | 否 | 住址 |
| 10 | medical_history | text | — | 否 | 否 | 病史 |
| 11 | allergies | text | — | 否 | 否 | 过敏史 |
| 12 | create_time | datetime | — | 否 | 否 | 创建时间 |
| 13 | update_time | datetime | — | 否 | 否 | 更新时间 |
