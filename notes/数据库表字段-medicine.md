# 药品表（medicine）字段清单

来源：`hospital/医疗信息系统含文档/hospital_db.sql`

| 编号 | 字段名 | 类型 | 长度 | 是否非空 | 是否主键 | 注释 |
| --- | --- | --- | --- | --- | --- | --- |
| 1 | id | bigint | — | 是 | 是 | 药品ID |
| 2 | medicine_code | varchar | 20 | 是 | 否 | 药品编码 |
| 3 | medicine_name | varchar | 100 | 是 | 否 | 药品名称 |
| 4 | specification | varchar | 50 | 否 | 否 | 规格 |
| 5 | dosage_form | varchar | 20 | 否 | 否 | 剂型(片剂/胶囊/注射剂等) |
| 6 | manufacturer | varchar | 100 | 否 | 否 | 生产厂家 |
| 7 | category | varchar | 50 | 否 | 否 | 类别(处方药/非处方药) |
| 8 | price | decimal | 10,2 | 否 | 否 | 单价 |
| 9 | stock | int | — | 否 | 否 | 库存量 |
| 10 | instructions | text | — | 否 | 否 | 使用说明 |
| 11 | status | tinyint | — | 否 | 否 | 状态(0:下架,1:上架) |
| 12 | create_time | datetime | — | 否 | 否 | 创建时间 |
| 13 | update_time | datetime | — | 否 | 否 | 更新时间 |
| 14 | category_id | int | — | 否 | 否 | 分类id |
