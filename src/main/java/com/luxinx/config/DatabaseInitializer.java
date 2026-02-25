package com.luxinx.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        // 创建表
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS T_ACCOUNT (" +
            "AID INTEGER PRIMARY KEY AUTOINCREMENT," +
            "PROP VARCHAR(2) DEFAULT '1'," +
            "OWNER VARCHAR(50)," +
            "ACCNAME VARCHAR(100)," +
            "ACCOUNT VARCHAR(100)," +
            "BALANCE REAL DEFAULT 0," +
            "MTYPE VARCHAR(20)," +
            "REMARK VARCHAR(500)," +
            "CREATETIME DATETIME DEFAULT CURRENT_TIMESTAMP," +
            "UPDATETIME DATETIME DEFAULT CURRENT_TIMESTAMP," +
            "OPERATER VARCHAR(50))");

        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS T_WATER (" +
            "WID INTEGER PRIMARY KEY AUTOINCREMENT," +
            "AID INTEGER NOT NULL," +
            "TRDATE DATE," +
            "TRADEKIND VARCHAR(2) DEFAULT '0'," +
            "TRTYPE VARCHAR(2) DEFAULT '0'," +
            "WACCOUNT VARCHAR(100)," +
            "WACCNAME VARCHAR(100)," +
            "TRNUM REAL DEFAULT 0," +
            "REMARK VARCHAR(500)," +
            "CREATETIME DATETIME DEFAULT CURRENT_TIMESTAMP," +
            "UPDATETIME DATETIME DEFAULT CURRENT_TIMESTAMP)");

        // 创建索引
        try { jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_account_prop ON T_ACCOUNT(PROP)"); } catch (Exception e) {}
        try { jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_water_aid ON T_WATER(AID)"); } catch (Exception e) {}
        try { jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_water_trdate ON T_WATER(TRDATE)"); } catch (Exception e) {}

        // 检查是否有数据
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM T_ACCOUNT", Integer.class);
        if (count != null && count == 0) {
            // 插入测试数据
            jdbcTemplate.execute("INSERT INTO T_ACCOUNT (PROP, OWNER, ACCNAME, ACCOUNT, BALANCE, MTYPE, REMARK, OPERATER) VALUES ('1', '张三', '现金', 'CASH001', 5000.00, '现金', '手持现金', 'admin')");
            jdbcTemplate.execute("INSERT INTO T_ACCOUNT (PROP, OWNER, ACCNAME, ACCOUNT, BALANCE, MTYPE, REMARK, OPERATER) VALUES ('1', '张三', '中国银行', 'BOC123456', 15000.00, '储蓄卡', '中国银行储蓄卡', 'admin')");
            jdbcTemplate.execute("INSERT INTO T_ACCOUNT (PROP, OWNER, ACCNAME, ACCOUNT, BALANCE, MTYPE, REMARK, OPERATER) VALUES ('1', '张三', '支付宝', 'ALIPAY001', 8000.00, '第三方支付', '支付宝账户', 'admin')");
            jdbcTemplate.execute("INSERT INTO T_ACCOUNT (PROP, OWNER, ACCNAME, ACCOUNT, BALANCE, MTYPE, REMARK, OPERATER) VALUES ('2', '张三', '信用卡', 'CREDIT001', -5000.00, '信用卡', '招商银行信用卡', 'admin')");
        }
        
        System.out.println("SQLite database initialized successfully!");
    }
}
