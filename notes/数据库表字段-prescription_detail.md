# 处方明细表（prescription_detail）字段清单

来源：`hospital/医疗信息系统含文档/hospital_db.sql`

| 编号 | 字段名 | 类型 | 长度 | 是否非空 | 是否主键 | 注释 |
| --- | --- | --- | --- | --- | --- | --- |
| 1 | id | bigint | — | 是 | 是 | 明细ID |
| 2 | prescription_id | bigint | — | 是 | 否 | 处方ID |
| 3 | medicine_id | bigint | — | 是 | 否 | 药品ID |
| 4 | dosage | varchar | 50 | 否 | 否 | 用量 |
| 5 | frequency | varchar | 50 | 否 | 否 | 频次(一日三次/每日一次) |
| 6 | days | int | — | 否 | 否 | 用药天数 |
| 7 | quantity | int | — | 否 | 否 | 数量 |
| 8 | usage | varchar | 100 | 否 | 否 | 用法(口服/外用等) |
| 9 | notes | varchar | 200 | 否 | 否 | 备注 |
| 10 | create_time | datetime | — | 否 | 否 | 创建时间 |
| 11 | update_time | datetime | — | 否 | 否 | 更新时间 |
