-- =============================================
-- 家庭账本数据库表结构
-- =============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS account DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE account;

-- =============================================
-- 账户表 T_ACCOUNT
-- =============================================
DROP TABLE IF EXISTS T_ACCOUNT;

CREATE TABLE T_ACCOUNT (
    AID INT(11) NOT NULL AUTO_INCREMENT COMMENT '账户ID',
    PROP VARCHAR(2) DEFAULT '1' COMMENT '属性: 1-资产 2-负债',
    OWNER VARCHAR(50) DEFAULT NULL COMMENT '所有人',
    ACCNAME VARCHAR(100) DEFAULT NULL COMMENT '账户名称',
    ACCOUNT VARCHAR(100) DEFAULT NULL COMMENT '账号',
    BALANCE DECIMAL(15,2) DEFAULT 0.00 COMMENT '余额',
    MTYPE VARCHAR(20) DEFAULT NULL COMMENT '账户类型',
    REMARK VARCHAR(500) DEFAULT NULL COMMENT '备注',
    CREATETIME DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UPDATETIME DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    OPERATER VARCHAR(50) DEFAULT NULL COMMENT '操作人',
    PRIMARY KEY (AID),
    KEY idx_prop (PROP),
    KEY idx_owner (OWNER),
    KEY idx_accname (ACCNAME)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='账户表';

-- =============================================
-- 流水表 T_WATER
-- =============================================
DROP TABLE IF EXISTS T_WATER;

CREATE TABLE T_WATER (
    WID INT(11) NOT NULL AUTO_INCREMENT COMMENT '流水ID',
    AID INT(11) NOT NULL COMMENT '账户ID',
    TRDATE DATE DEFAULT NULL COMMENT '交易日期',
    TRADEKIND VARCHAR(2) DEFAULT '0' COMMENT '交易分类: 0-转账 1-居家 2-食品 3-交通 4-投资 5-还款',
    TRTYPE VARCHAR(2) DEFAULT '0' COMMENT '交易类型: 0-出金 1-入金',
    WACCOUNT VARCHAR(100) DEFAULT NULL COMMENT '对方账户',
    WACCNAME VARCHAR(100) DEFAULT NULL COMMENT '对方账户名',
    TRNUM DECIMAL(15,2) DEFAULT 0.00 COMMENT '交易金额',
    REMARK VARCHAR(500) DEFAULT NULL COMMENT '备注',
    CREATETIME DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UPDATETIME DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (WID),
    KEY idx_aid (AID),
    KEY idx_trdate (TRDATE),
    KEY idx_trtype (TRTYPE),
    KEY idx_tradekind (TRADEKIND)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='流水表';

-- =============================================
-- 初始化测试数据
-- =============================================

-- 插入测试账户
INSERT INTO T_ACCOUNT (PROP, OWNER, ACCNAME, ACCOUNT, BALANCE, MTYPE, REMARK, OPERATER) VALUES
('1', '张三', '现金', 'CASH001', 5000.00, '现金', '手持现金', 'admin'),
('1', '张三', '中国银行', 'BOC123456', 15000.00, '储蓄卡', '中国银行储蓄卡', 'admin'),
('1', '张三', '支付宝', 'ALIPAY001', 8000.00, '第三方支付', '支付宝账户', 'admin'),
('1', '张三', '微信钱包', 'WECHAT001', 3000.00, '第三方支付', '微信支付', 'admin'),
('2', '张三', '信用卡', 'CREDIT001', -5000.00, '信用卡', '招商银行信用卡', 'admin'),
('2', '张三', '花呗', 'HUABEI001', -2000.00, '消费信贷', '支付宝花呗', 'admin');

-- 插入测试流水
INSERT INTO T_WATER (AID, TRDATE, TRADEKIND, TRTYPE, TRNUM, REMARK, OPERATER) VALUES
(1, CURDATE(), '1', '1', 3000.00, '工资收入', 'admin'),
(2, CURDATE(), '0', '1', 15000.00, '工资入账', 'admin'),
(3, CURDATE(), '2', '1', 500.00, '退款', 'admin'),
(1, CURDATE(), '3', '0', 200.00, '交通出行', 'admin'),
(3, CURDATE(), '2', '0', 150.00, '日常消费', 'admin'),
(5, CURDATE(), '5', '0', 2000.00, '信用卡还款', 'admin'),
(2, DATE_SUB(CURDATE(), INTERVAL 1 DAY), '4', '1', 10000.00, '理财收益', 'admin'),
(3, DATE_SUB(CURDATE(), INTERVAL 2 DAY), '2', '0', 80.00, '网购', 'admin'),
(1, DATE_SUB(CURDATE(), INTERVAL 3 DAY), '1', '0', 500.00, '日常支出', 'admin');

-- =============================================
-- 视图：账户流水汇总
-- =============================================
DROP VIEW IF EXISTS V_ACCOUNT_WATER_SUMMARY;

CREATE VIEW V_ACCOUNT_WATER_SUMMARY AS
SELECT 
    a.AID,
    a.ACCNAME,
    a.PROP,
    a.BALANCE,
    a.OWNER,
    COUNT(w.WID) AS WATER_COUNT,
    COALESCE(SUM(CASE WHEN w.TRTYPE = '1' THEN w.TRNUM ELSE 0 END), 0) AS TOTAL_INCOME,
    COALESCE(SUM(CASE WHEN w.TRTYPE = '0' THEN w.TRNUM ELSE 0 END), 0) AS TOTAL_EXPENSE
FROM T_ACCOUNT a
LEFT JOIN T_WATER w ON a.AID = w.AID
GROUP BY a.AID, a.ACCNAME, a.PROP, a.BALANCE, a.OWNER;
