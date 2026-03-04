-- 医生表
CREATE TABLE IF NOT EXISTS doctor (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '医生ID',
    doctor_no VARCHAR(20) COMMENT '医生编号',
    name VARCHAR(50) NOT NULL COMMENT '医生姓名',
    department_id BIGINT COMMENT '所属科室ID',
    title VARCHAR(50) COMMENT '职称',
    expertise TEXT COMMENT '专长',
    introduction TEXT COMMENT '简介',
    user_id BIGINT COMMENT '关联用户ID',
    status INT DEFAULT 1 COMMENT '状态: 1-在职, 0-离职',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (department_id) REFERENCES department(id) ON DELETE SET NULL,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE SET NULL
) COMMENT '医生信息表'; 

-- 药品表
CREATE TABLE IF NOT EXISTS medicine (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '药品ID',
    name VARCHAR(100) NOT NULL COMMENT '药品名称',
    medicine_code VARCHAR(50) UNIQUE COMMENT '药品编码',
    specification VARCHAR(100) COMMENT '规格',
    price DECIMAL(10, 2) COMMENT '单价',
    stock INT DEFAULT 0 COMMENT '库存',
    manufacturer VARCHAR(200) COMMENT '生产厂家',
    production_date DATE COMMENT '生产日期',
    expiry_date DATE COMMENT '有效期',
    category VARCHAR(50) COMMENT '药品分类',
    is_prescription TINYINT(1) DEFAULT 0 COMMENT '是否处方药: 1-是, 0-否',
    status TINYINT(1) DEFAULT 1 COMMENT '状态: 1-正常, 0-下架',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT '药品信息表';

-- 排班表
CREATE TABLE IF NOT EXISTS schedule (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '排班ID',
    doctor_id BIGINT NOT NULL COMMENT '医生ID',
    schedule_date DATE NOT NULL COMMENT '排班日期',
    time_slot VARCHAR(20) NOT NULL COMMENT '时间段(上午/下午/晚上)',
    max_patients INT DEFAULT 20 COMMENT '最大接诊人数',
    current_patients INT DEFAULT 0 COMMENT '当前预约人数',
    status TINYINT(1) DEFAULT 1 COMMENT '状态: 1-正常, 0-停诊',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (doctor_id) REFERENCES doctor(id) ON DELETE CASCADE,
    UNIQUE KEY `uk_doctor_date_time` (`doctor_id`, `schedule_date`, `time_slot`) COMMENT '同一医生同一天同一时间段只能有一条排班'
) COMMENT '医生排班表';

-- 预约表
CREATE TABLE IF NOT EXISTS appointment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '预约ID',
    patient_id BIGINT NOT NULL COMMENT '患者ID',
    doctor_id BIGINT NOT NULL COMMENT '医生ID',
    schedule_id BIGINT NOT NULL COMMENT '排班ID',
    appointment_no VARCHAR(50) UNIQUE NOT NULL COMMENT '预约编号',
    appointment_date DATE NOT NULL COMMENT '预约日期',
    time_slot VARCHAR(20) NOT NULL COMMENT '时间段(上午/下午/晚上)',
    symptoms TEXT COMMENT '症状描述',
    status TINYINT(1) DEFAULT 1 COMMENT '状态: 1-待就诊, 2-已就诊, 0-已取消',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (patient_id) REFERENCES patient(id) ON DELETE CASCADE,
    FOREIGN KEY (doctor_id) REFERENCES doctor(id) ON DELETE CASCADE,
    FOREIGN KEY (schedule_id) REFERENCES schedule(id) ON DELETE CASCADE
) COMMENT '预约挂号表'; 