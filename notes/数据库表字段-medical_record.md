# 就诊记录表（medical_record）字段清单

来源：`hospital/医疗信息系统含文档/hospital_db.sql`

| 编号 | 字段名 | 类型 | 长度 | 是否非空 | 是否主键 | 注释 |
| --- | --- | --- | --- | --- | --- | --- |
| 1 | id | bigint | — | 是 | 是 | 记录ID |
| 2 | record_no | varchar | 20 | 是 | 否 | 记录编号 |
| 3 | patient_id | bigint | — | 是 | 否 | 患者ID |
| 4 | doctor_id | bigint | — | 是 | 否 | 医生ID |
| 5 | appointment_id | bigint | — | 否 | 否 | 预约ID |
| 6 | diagnosis | text | — | 否 | 否 | 诊断结果 |
| 7 | treatment | text | — | 否 | 否 | 治疗方案 |
| 8 | record_date | date | — | 是 | 否 | 就诊日期 |
| 9 | notes | text | — | 否 | 否 | 医生备注 |
| 10 | follow_up | date | — | 否 | 否 | 随访日期 |
| 11 | create_time | datetime | — | 否 | 否 | 创建时间 |
| 12 | update_time | datetime | — | 否 | 否 | 更新时间 |
