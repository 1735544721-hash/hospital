# 处方表（prescription）字段清单

来源：`hospital/医疗信息系统含文档/hospital_db.sql`

| 编号 | 字段名 | 类型 | 长度 | 是否非空 | 是否主键 | 注释 |
| --- | --- | --- | --- | --- | --- | --- |
| 1 | id | bigint | — | 是 | 是 | 处方ID |
| 2 | prescription_no | varchar | 20 | 是 | 否 | 处方编号 |
| 3 | patient_id | bigint | — | 是 | 否 | 患者ID |
| 4 | doctor_id | bigint | — | 是 | 否 | 医生ID |
| 5 | record_id | bigint | — | 是 | 否 | 就诊记录ID |
| 6 | prescription_date | date | — | 是 | 否 | 处方日期 |
| 7 | diagnosis | varchar | 200 | 否 | 否 | 诊断 |
| 8 | notes | text | — | 否 | 否 | 备注 |
| 9 | status | tinyint | — | 否 | 否 | 状态(0:未取药,1:已取药) |
| 10 | create_time | datetime | — | 否 | 否 | 创建时间 |
| 11 | update_time | datetime | — | 否 | 否 | 更新时间 |
