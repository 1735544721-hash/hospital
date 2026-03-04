# 排班表（schedule）字段清单

来源：`hospital/医疗信息系统含文档/hospital_db.sql`

| 编号 | 字段名 | 类型 | 长度 | 是否非空 | 是否主键 | 注释 |
| --- | --- | --- | --- | --- | --- | --- |
| 1 | id | bigint | — | 是 | 是 | 排班ID |
| 2 | doctor_id | bigint | — | 是 | 否 | 医生ID |
| 3 | schedule_date | date | — | 是 | 否 | 排班日期 |
| 4 | time_slot | varchar | 20 | 是 | 否 | 时间段(上午/下午/晚上) |
| 5 | max_patients | int | — | 否 | 否 | 最大接诊人数 |
| 6 | current_patients | int | — | 否 | 否 | 当前预约人数 |
| 7 | status | tinyint | — | 否 | 否 | 状态(0:停诊,1:正常) |
| 8 | create_time | datetime | — | 否 | 否 | 创建时间 |
| 9 | update_time | datetime | — | 否 | 否 | 更新时间 |
