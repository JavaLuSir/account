-- 初始化测试数据
INSERT INTO T_ACCOUNT (PROP, OWNER, ACCNAME, ACCOUNT, BALANCE, MTYPE, REMARK, OPERATER) VALUES
('1', '张三', '现金', 'CASH001', 5000.00, '现金', '手持现金', 'admin'),
('1', '张三', '中国银行', 'BOC123456', 15000.00, '储蓄卡', '中国银行储蓄卡', 'admin'),
('1', '张三', '支付宝', 'ALIPAY001', 8000.00, '第三方支付', '支付宝账户', 'admin'),
('1', '张三', '微信钱包', 'WECHAT001', 3000.00, '第三方支付', '微信支付', 'admin'),
('1', '张三', '建设银行', 'CCB123456', 20000.00, '储蓄卡', '建设银行储蓄卡', 'admin'),
('2', '张三', '信用卡', 'CREDIT001', -5000.00, '信用卡', '招商银行信用卡', 'admin'),
('2', '张三', '花呗', 'HUABEI001', -2000.00, '消费信贷', '支付宝花呗', 'admin'),
('2', '张三', '京东白条', 'JDBT001', -1000.00, '消费信贷', '京东白条', 'admin');

INSERT INTO T_WATER (AID, TRDATE, TRADEKIND, TRTYPE, TRNUM, REMARK, OPERATER, IFAUTO) VALUES
(1, CURDATE(), '1', '1', 3000.00, '工资收入', 'admin', '0'),
(2, CURDATE(), '0', '1', 15000.00, '工资入账', 'admin', '0'),
(3, CURDATE(), '2', '1', 500.00, '退款', 'admin', '0'),
(1, CURDATE(), '3', '0', 200.00, '交通出行', 'admin', '0'),
(3, CURDATE(), '2', '0', 150.00, '日常消费', 'admin', '0'),
(5, CURDATE(), '5', '0', 2000.00, '信用卡还款', 'admin', '0'),
(2, DATE_SUB(CURDATE(), INTERVAL 1 DAY), '4', '1', 10000.00, '理财收益', 'admin', '0'),
(3, DATE_SUB(CURDATE(), INTERVAL 2 DAY), '2', '0', 80.00, '网购', 'admin', '0'),
(1, DATE_SUB(CURDATE(), INTERVAL 3 DAY), '1', '0', 500.00, '日常支出', 'admin', '0'),
(6, DATE_SUB(CURDATE(), INTERVAL 1 DAY), '5', '0', 500.00, '花呗还款', 'admin', '0');
