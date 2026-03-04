# 药品分类表（medicine_category）字段清单

来源：`hospital/医疗信息系统含文档/hospital_db.sql`

| 编号 | 字段名 | 类型 | 长度 | 是否非空 | 是否主键 | 注释 |
| --- | --- | --- | --- | --- | --- | --- |
| 1 | id | bigint | — | 是 | 是 | 分类ID |
| 2 | category_name | varchar | 50 | 是 | 否 | 分类名称 |
| 3 | category_code | varchar | 20 | 是 | 否 | 分类编码 |
| 4 | description | varchar | 200 | 否 | 否 | 分类描述 |
| 5 | status | tinyint | — | 否 | 否 | 状态(0:禁用,1:启用) |
| 6 | create_time | datetime | — | 否 | 否 | 创建时间 |
| 7 | update_time | datetime | — | 否 | 否 | 更新时间 |
