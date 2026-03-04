# 预约表（appointment）字段清单

来源：`hospital/医疗信息系统含文档/hospital_db.sql`

| 编号 | 字段名 | 类型 | 长度 | 是否非空 | 是否主键 | 注释 |
| --- | --- | --- | --- | --- | --- | --- |
| 1 | id | bigint | — | 是 | 是 | 预约ID |
| 2 | patient_id | bigint | — | 是 | 否 | 患者ID |
| 3 | doctor_id | bigint | — | 是 | 否 | 医生ID |
| 4 | schedule_id | bigint | — | 是 | 否 | 排班ID |
| 5 | appointment_no | varchar | 20 | 是 | 否 | 预约编号 |
| 6 | appointment_date | date | — | 是 | 否 | 预约日期 |
| 7 | time_slot | varchar | 20 | 是 | 否 | 时间段(上午/下午/晚上) |
| 8 | symptoms | text | — | 否 | 否 | 症状描述 |
| 9 | status | tinyint | — | 否 | 否 | 状态(0:取消,1:待就诊,2:已就诊) |
| 10 | create_time | datetime | — | 否 | 否 | 创建时间 |
| 11 | update_time | datetime | — | 否 | 否 | 更新时间 |
