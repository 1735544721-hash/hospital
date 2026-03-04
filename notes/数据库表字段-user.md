# 用户表（user）字段清单

来源：`hospital/医疗信息系统含文档/hospital_db.sql`

| 编号 | 字段名 | 类型 | 长度 | 是否非空 | 是否主键 | 注释 |
| --- | --- | --- | --- | --- | --- | --- |
| 1 | id | bigint | — | 是 | 是 | 用户ID |
| 2 | username | varchar | 50 | 是 | 否 | 用户名 |
| 3 | password | varchar | 100 | 是 | 否 | 密码(加密存储) |
| 4 | name | varchar | 50 | 否 | 否 | 真实姓名 |
| 5 | avatar | varchar | 200 | 否 | 否 | 头像地址 |
| 6 | sex | varchar | 10 | 否 | 否 | 性别 |
| 7 | phone | varchar | 20 | 否 | 否 | 手机号 |
| 8 | email | varchar | 100 | 否 | 否 | 邮箱 |
| 9 | role_code | varchar | 20 | 否 | 否 | 角色代码 |
| 10 | status | tinyint | — | 否 | 否 | 状态(0:禁用,1:启用) |
| 11 | create_time | datetime | — | 否 | 否 | 创建时间 |
| 12 | update_time | datetime | — | 否 | 否 | 更新时间 |
