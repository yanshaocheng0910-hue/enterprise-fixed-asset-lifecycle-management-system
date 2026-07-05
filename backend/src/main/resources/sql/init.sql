CREATE DATABASE IF NOT EXISTS fixed_asset_lifecycle_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
    USE fixed_asset_lifecycle_system;

    SET NAMES utf8mb4;
    SET FOREIGN_KEY_CHECKS = 0;

    DROP TABLE IF EXISTS inventory_record;
    DROP TABLE IF EXISTS inventory_task;
    DROP TABLE IF EXISTS depreciation_record;
    DROP TABLE IF EXISTS asset_operation_log;
    DROP TABLE IF EXISTS asset;
    DROP TABLE IF EXISTS asset_category;
    DROP TABLE IF EXISTS sys_user_role;
    DROP TABLE IF EXISTS sys_role;
    DROP TABLE IF EXISTS sys_user;

    CREATE TABLE sys_user (
        id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
        username VARCHAR(64) NOT NULL UNIQUE COMMENT '用户名',
        password VARCHAR(255) NOT NULL COMMENT '密码',
        real_name VARCHAR(64) NOT NULL COMMENT '真实姓名',
        department VARCHAR(128) DEFAULT NULL COMMENT '所属部门',
        phone VARCHAR(32) DEFAULT NULL COMMENT '手机号',
        email VARCHAR(128) DEFAULT NULL COMMENT '邮箱',
        status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1启用 0禁用',
        created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
        updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
    ) COMMENT='系统用户表';

    CREATE TABLE sys_role (
        id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
        role_code VARCHAR(64) NOT NULL UNIQUE COMMENT '角色编码',
        role_name VARCHAR(64) NOT NULL COMMENT '角色名称',
        description VARCHAR(255) DEFAULT NULL COMMENT '描述',
        created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
        updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
    ) COMMENT='系统角色表';

    CREATE TABLE sys_user_role (
        id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
        user_id BIGINT NOT NULL COMMENT '用户ID',
        role_id BIGINT NOT NULL COMMENT '角色ID',
        UNIQUE KEY uk_user_role (user_id, role_id)
    ) COMMENT='用户角色关联表';

    CREATE TABLE asset_category (
        id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
        category_code VARCHAR(64) NOT NULL UNIQUE COMMENT '分类编码',
        category_name VARCHAR(128) NOT NULL COMMENT '分类名称',
        parent_id BIGINT NOT NULL DEFAULT 0 COMMENT '父级ID',
        depreciation_years INT NOT NULL DEFAULT 5 COMMENT '折旧年限',
        remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
        created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
        updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
    ) COMMENT='资产分类表';

    CREATE TABLE asset (
        id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
        asset_code VARCHAR(64) NOT NULL UNIQUE COMMENT '资产编号',
        asset_name VARCHAR(128) NOT NULL COMMENT '资产名称',
        category_id BIGINT NOT NULL COMMENT '分类ID',
        specification VARCHAR(255) DEFAULT NULL COMMENT '规格型号',
        brand VARCHAR(128) DEFAULT NULL COMMENT '品牌',
        purchase_date DATE NOT NULL COMMENT '购置日期',
        original_value DECIMAL(18,2) NOT NULL COMMENT '原值',
        useful_life INT NOT NULL COMMENT '使用年限',
        residual_rate DECIMAL(5,4) NOT NULL COMMENT '残值率',
        depreciation_method VARCHAR(32) NOT NULL DEFAULT 'straight_line' COMMENT '折旧方法',
        accumulated_depreciation DECIMAL(18,2) NOT NULL DEFAULT 0 COMMENT '累计折旧',
        net_value DECIMAL(18,2) NOT NULL DEFAULT 0 COMMENT '净值',
        department VARCHAR(128) DEFAULT NULL COMMENT '所属部门',
        keeper VARCHAR(64) DEFAULT NULL COMMENT '保管人',
        location VARCHAR(255) DEFAULT NULL COMMENT '存放地点',
        status VARCHAR(32) NOT NULL COMMENT '资产状态',
        qr_code VARCHAR(128) DEFAULT NULL COMMENT '二维码编码',
        rfid_code VARCHAR(128) DEFAULT NULL COMMENT 'RFID编码',
        photo_url VARCHAR(255) DEFAULT NULL COMMENT '图片地址',
        remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
        created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
        updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
        deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
        INDEX idx_asset_category_id (category_id),
        INDEX idx_asset_status (status),
        INDEX idx_asset_department (department)
    ) COMMENT='固定资产表';

    CREATE TABLE asset_operation_log (
        id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
        asset_id BIGINT NOT NULL COMMENT '资产ID',
        operation_type VARCHAR(64) NOT NULL COMMENT '操作类型',
        operation_name VARCHAR(128) NOT NULL COMMENT '操作名称',
        before_status VARCHAR(32) DEFAULT NULL COMMENT '变更前状态',
        after_status VARCHAR(32) DEFAULT NULL COMMENT '变更后状态',
        operator_id BIGINT DEFAULT NULL COMMENT '操作人ID',
        operator_name VARCHAR(64) DEFAULT NULL COMMENT '操作人姓名',
        operation_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
        remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
        INDEX idx_asset_operation_asset_id (asset_id),
        INDEX idx_asset_operation_time (operation_time)
    ) COMMENT='资产操作日志表';

    CREATE TABLE depreciation_record (
        id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
        asset_id BIGINT NOT NULL COMMENT '资产ID',
        depreciation_month VARCHAR(7) NOT NULL COMMENT '折旧月份',
        original_value DECIMAL(18,2) NOT NULL COMMENT '原值',
        monthly_depreciation DECIMAL(18,2) NOT NULL COMMENT '月折旧额',
        accumulated_depreciation DECIMAL(18,2) NOT NULL COMMENT '累计折旧',
        net_value DECIMAL(18,2) NOT NULL COMMENT '净值',
        created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
        INDEX idx_depreciation_asset_id (asset_id)
    ) COMMENT='折旧记录表';

    CREATE TABLE inventory_task (
        id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
        task_code VARCHAR(64) NOT NULL UNIQUE COMMENT '任务编号',
        task_name VARCHAR(128) NOT NULL COMMENT '任务名称',
        scope_type VARCHAR(32) DEFAULT NULL COMMENT '盘点范围类型',
        department VARCHAR(128) DEFAULT NULL COMMENT '部门范围',
        location VARCHAR(255) DEFAULT NULL COMMENT '地点范围',
        status VARCHAR(32) DEFAULT NULL COMMENT '任务状态',
        start_time DATETIME DEFAULT NULL COMMENT '开始时间',
        end_time DATETIME DEFAULT NULL COMMENT '结束时间',
        created_by BIGINT DEFAULT NULL COMMENT '创建人',
        created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
        updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
    ) COMMENT='盘点任务表';

    CREATE TABLE inventory_record (
        id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
        task_id BIGINT NOT NULL COMMENT '任务ID',
        asset_id BIGINT NOT NULL COMMENT '资产ID',
        expected_location VARCHAR(255) DEFAULT NULL COMMENT '应在地点',
        actual_location VARCHAR(255) DEFAULT NULL COMMENT '实际地点',
        expected_keeper VARCHAR(64) DEFAULT NULL COMMENT '应在保管人',
        actual_keeper VARCHAR(64) DEFAULT NULL COMMENT '实际保管人',
        result VARCHAR(32) DEFAULT NULL COMMENT '盘点结果',
        scanned_at DATETIME DEFAULT NULL COMMENT '扫码时间',
        remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
        INDEX idx_inventory_record_task_id (task_id),
        INDEX idx_inventory_record_asset_id (asset_id)
    ) COMMENT='盘点明细表';

    INSERT INTO sys_role (id, role_code, role_name, description, created_at, updated_at) VALUES
(1, 'ADMIN', '系统管理员', '系统超级管理员', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'ASSET_MANAGER', '资产管理员', '负责资产台账和盘点', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'FINANCE', '财务人员', '负责折旧与财务对接', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 'AUDITOR', '审计人员', '只读审计角色', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

    INSERT INTO sys_user (id, username, password, real_name, department, phone, email, status, created_at, updated_at) VALUES
(1, 'admin', '$2b$12$M1o3SnqOMgHBoaa1HKX2Lu5qlS9lb7.TMlycnQdwvrGzjvnVZvU8y', '系统管理员', '信息中心', '13800000000', 'admin@example.com', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

    INSERT INTO sys_user_role (id, user_id, role_id) VALUES
(1, 1, 1),
(2, 1, 2),
(3, 1, 3),
(4, 1, 4);

    INSERT INTO asset_category (id, category_code, category_name, parent_id, depreciation_years, remark, created_at, updated_at) VALUES
(1, 'OFFICE', '办公设备', 0, 5, '办公类固定资产', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'ELECTRONIC', '电子设备', 0, 4, '电子类固定资产', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'VEHICLE', '运输设备', 0, 8, '运输与公务车辆', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 'PRODUCTION', '生产设备', 0, 10, '生产运营设备', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 'BUILDING', '房屋及建筑物', 0, 20, '房屋及建筑物', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

    INSERT INTO asset (id, asset_code, asset_name, category_id, specification, brand, purchase_date, original_value, useful_life, residual_rate, depreciation_method, accumulated_depreciation, net_value, department, keeper, location, status, qr_code, rfid_code, photo_url, remark, created_at, updated_at, deleted) VALUES
(1, 'FA2024030001', '台式电脑-A01', 2, 'OptiPlex 7090', 'Dell', '2024-03-15', 6800.00, 4, 0.05, 'straight_line', 3768.24, 3031.76, '信息中心', '张伟', 'A座3层机房', 'IN_USE', 'QR20240001', NULL, NULL, '系统初始化资产数据', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(2, 'FA2024050002', '台式电脑-A02', 2, 'ThinkCentre M750', 'Lenovo', '2024-05-10', 6200.00, 4, 0.05, 'straight_line', 3190.46, 3009.54, '财务部', '李娜', 'B座2层财务室', 'IN_USE', 'QR20240002', NULL, NULL, '系统初始化资产数据', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(3, 'FA2025010003', '笔记本电脑-B01', 2, 'EliteBook 840', 'HP', '2025-01-08', 8600.00, 4, 0.05, 'straight_line', 3063.78, 5536.22, '综合办公室', '王涛', 'A座5层办公室', 'IDLE', 'QR20250003', NULL, NULL, '系统初始化资产数据', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(4, 'FA2025020004', '笔记本电脑-B02', 2, 'MacBook Pro 14', 'Apple', '2025-02-15', 14500.00, 4, 0.05, 'straight_line', 4878.66, 9621.34, '信息中心', '赵敏', 'A座4层研发室', 'IN_USE', 'QR20250004', NULL, NULL, '系统初始化资产数据', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(5, 'FA2023060005', '打印机-C01', 1, 'LaserJet 400', 'HP', '2023-06-20', 3200.00, 5, 0.05, 'straight_line', 1874.79, 1325.21, '综合办公室', '周芳', 'A座2层文印室', 'IN_USE', 'QR20230005', NULL, NULL, '系统初始化资产数据', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(6, 'FA2022090006', '打印机-C02', 1, 'M7405DW', 'Brother', '2022-09-12', 2800.00, 5, 0.05, 'straight_line', 2039.18, 760.82, '人力资源部', '陈静', 'B座4层人资室', 'REPAIRING', 'QR20220006', NULL, NULL, '系统初始化资产数据', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(7, 'FA2021070007', '空调-D01', 1, 'KFR-72LW', '格力', '2021-07-01', 9800.00, 8, 0.05, 'straight_line', 5818.80, 3981.20, '财务部', '孙磊', 'B座2层会议室', 'IN_USE', 'QR20210007', NULL, NULL, '系统初始化资产数据', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(8, 'FA2020080008', '空调-D02', 1, 'KFR-50GW', '美的', '2020-08-16', 7600.00, 8, 0.05, 'straight_line', 5339.91, 2260.09, '综合办公室', '刘佳', 'A座1层大厅', 'WAITING_SCRAP', 'QR20200008', NULL, NULL, '系统初始化资产数据', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(9, 'FA2024080009', '服务器-E01', 2, 'PowerEdge R750', 'Dell', '2024-08-06', 68000.00, 5, 0.05, 'straight_line', 24763.41, 43236.59, '信息中心', '钱坤', '数据中心1机柜', 'IN_USE', 'QR20240009', NULL, NULL, '系统初始化资产数据', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(10, 'FA2023110010', '服务器-E02', 2, 'FusionServer 2288H', '华为', '2023-11-18', 72000.00, 5, 0.05, 'straight_line', 36480.00, 35520.00, '信息中心', '吴昊', '数据中心2机柜', 'REPAIRING', 'QR20230010', NULL, NULL, '系统初始化资产数据', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(11, 'FA2024040011', '投影仪-F01', 1, 'CB-X06', 'Epson', '2024-04-09', 5600.00, 5, 0.05, 'straight_line', 2394.09, 3205.91, '综合办公室', '郑洁', 'A座6层培训室', 'IDLE', 'QR20240011', NULL, NULL, '系统初始化资产数据', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(12, 'FA2022030012', '投影仪-F02', 1, 'XGA-2200', 'BenQ', '2022-03-13', 4800.00, 5, 0.05, 'straight_line', 3952.00, 848.00, '人力资源部', '蒋雪', 'B座3层培训室', 'IN_USE', 'QR20220012', NULL, NULL, '系统初始化资产数据', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(13, 'FA2021050013', '办公桌椅-G01', 1, '主管桌椅套装', '震旦', '2021-05-22', 4500.00, 8, 0.05, 'straight_line', 2760.86, 1739.14, '资产管理部', '彭超', 'C座2层办公室', 'IN_USE', 'QR20210013', NULL, NULL, '系统初始化资产数据', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(14, 'FA2024100014', '办公桌椅-G02', 1, '工位桌椅套装', '圣奥', '2024-10-01', 3200.00, 8, 0.05, 'straight_line', 665.07, 2534.93, '财务部', '何琳', 'B座2层开放工位', 'IDLE', 'QR20240014', NULL, NULL, '系统初始化资产数据', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(15, 'FA2021100015', '公务车辆-H01', 3, '帕萨特 380TSI', '大众', '2021-10-11', 228000.00, 8, 0.05, 'straight_line', 128606.25, 99393.75, '综合办公室', '高峰', '1号停车位', 'IN_USE', 'QR20210015', NULL, NULL, '系统初始化资产数据', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(16, 'FA2023120016', '公务车辆-H02', 3, '比亚迪汉 EV', '比亚迪', '2023-12-02', 246000.00, 8, 0.05, 'straight_line', 75465.78, 170534.22, '综合办公室', '严雷', '2号停车位', 'WAITING_SCRAP', 'QR20230016', NULL, NULL, '系统初始化资产数据', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(17, 'FA2020020017', '生产设备-I01', 4, '数控加工中心', '沈阳机床', '2020-02-14', 320000.00, 10, 0.05, 'straight_line', 195066.41, 124933.59, '资产管理部', '马强', '一号车间', 'IN_USE', 'QR20200017', NULL, NULL, '系统初始化资产数据', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(18, 'FA2022060018', '生产设备-I02', 4, '自动包装机', '博锐', '2022-06-06', 185000.00, 10, 0.05, 'straight_line', 71764.42, 113235.58, '资产管理部', '韩梅', '二号车间', 'REPAIRING', 'QR20220018', NULL, NULL, '系统初始化资产数据', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(19, 'FA2018010019', '办公楼-J01', 5, '综合办公楼东楼', '自建', '2018-01-01', 5800000.00, 20, 0.05, 'straight_line', 2341749.66, 3458250.34, '综合办公室', '后勤处', '园区东侧', 'IN_USE', 'QR20180019', NULL, NULL, '系统初始化资产数据', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(20, 'FA2019070020', '仓库-J02', 5, '周转仓库', '自建', '2019-07-01', 2600000.00, 20, 0.05, 'straight_line', 864500.28, 1735499.72, '资产管理部', '仓储组', '园区北侧', 'IDLE', 'QR20190020', NULL, NULL, '系统初始化资产数据', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

    INSERT INTO asset_operation_log (id, asset_id, operation_type, operation_name, before_status, after_status, operator_id, operator_name, operation_time, remark) VALUES
(1, 1, 'INIT', '初始化导入', NULL, 'IN_USE', 1, '系统管理员', CURRENT_TIMESTAMP, '初始化资产台账数据'),
(2, 2, 'INIT', '初始化导入', NULL, 'IN_USE', 1, '系统管理员', CURRENT_TIMESTAMP, '初始化资产台账数据'),
(3, 3, 'INIT', '初始化导入', NULL, 'IDLE', 1, '系统管理员', CURRENT_TIMESTAMP, '初始化资产台账数据'),
(4, 4, 'INIT', '初始化导入', NULL, 'IN_USE', 1, '系统管理员', CURRENT_TIMESTAMP, '初始化资产台账数据'),
(5, 5, 'INIT', '初始化导入', NULL, 'IN_USE', 1, '系统管理员', CURRENT_TIMESTAMP, '初始化资产台账数据'),
(6, 6, 'INIT', '初始化导入', NULL, 'REPAIRING', 1, '系统管理员', CURRENT_TIMESTAMP, '初始化资产台账数据'),
(7, 7, 'INIT', '初始化导入', NULL, 'IN_USE', 1, '系统管理员', CURRENT_TIMESTAMP, '初始化资产台账数据'),
(8, 8, 'INIT', '初始化导入', NULL, 'WAITING_SCRAP', 1, '系统管理员', CURRENT_TIMESTAMP, '初始化资产台账数据'),
(9, 9, 'INIT', '初始化导入', NULL, 'IN_USE', 1, '系统管理员', CURRENT_TIMESTAMP, '初始化资产台账数据'),
(10, 10, 'INIT', '初始化导入', NULL, 'REPAIRING', 1, '系统管理员', CURRENT_TIMESTAMP, '初始化资产台账数据'),
(11, 11, 'INIT', '初始化导入', NULL, 'IDLE', 1, '系统管理员', CURRENT_TIMESTAMP, '初始化资产台账数据'),
(12, 12, 'INIT', '初始化导入', NULL, 'IN_USE', 1, '系统管理员', CURRENT_TIMESTAMP, '初始化资产台账数据'),
(13, 13, 'INIT', '初始化导入', NULL, 'IN_USE', 1, '系统管理员', CURRENT_TIMESTAMP, '初始化资产台账数据'),
(14, 14, 'INIT', '初始化导入', NULL, 'IDLE', 1, '系统管理员', CURRENT_TIMESTAMP, '初始化资产台账数据'),
(15, 15, 'INIT', '初始化导入', NULL, 'IN_USE', 1, '系统管理员', CURRENT_TIMESTAMP, '初始化资产台账数据'),
(16, 16, 'INIT', '初始化导入', NULL, 'WAITING_SCRAP', 1, '系统管理员', CURRENT_TIMESTAMP, '初始化资产台账数据'),
(17, 17, 'INIT', '初始化导入', NULL, 'IN_USE', 1, '系统管理员', CURRENT_TIMESTAMP, '初始化资产台账数据'),
(18, 18, 'INIT', '初始化导入', NULL, 'REPAIRING', 1, '系统管理员', CURRENT_TIMESTAMP, '初始化资产台账数据'),
(19, 19, 'INIT', '初始化导入', NULL, 'IN_USE', 1, '系统管理员', CURRENT_TIMESTAMP, '初始化资产台账数据'),
(20, 20, 'INIT', '初始化导入', NULL, 'IDLE', 1, '系统管理员', CURRENT_TIMESTAMP, '初始化资产台账数据');


    SET FOREIGN_KEY_CHECKS = 1;
