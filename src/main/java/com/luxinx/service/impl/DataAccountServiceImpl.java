package com.luxinx.service.impl;

import com.luxinx.bean.BeanAccount;
import com.luxinx.bean.BeanCategory;
import com.luxinx.bean.BeanWater;
import com.luxinx.service.ServiceDataAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class DataAccountServiceImpl implements ServiceDataAccount {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public List<BeanWater> queryAccountInfoById(String id) {
        String sql = "SELECT WID,AID,TRDATE,TRADEKIND,TRTYPE,TRNUM,BALANCE,REMARK,UPDATETIME,CID FROM T_WATER WHERE AID=? ORDER BY UPDATETIME DESC";
        return jdbcTemplate.query(sql, new BeanWater(), id);
    }

    @Override
    public List<BeanWater> queryAllWater() {
        String sql = "SELECT WID,AID,TRDATE,TRADEKIND,TRTYPE,TRNUM,BALANCE,REMARK,UPDATETIME,CID FROM T_WATER ORDER BY UPDATETIME DESC";
        return jdbcTemplate.query(sql, new BeanWater());
    }

    @Override
    public List<BeanAccount> queryAccount() {
        String sql = "SELECT T.* FROM T_ACCOUNT T";
        NamedParameterJdbcTemplate npjt = new NamedParameterJdbcTemplate(this.jdbcTemplate.getDataSource());
        return npjt.query(sql, new BeanAccount());
    }

    @Override
    public Map<String, String> addAccount(Map<String, String> param) {
        try {
            String prop = param.get("PROP");
            String owner = param.get("OWNER");
            String accname = param.get("ACCNAME");
            String account = param.get("ACCOUNT");
            String balance = param.get("BALANCE");
            String remark = param.get("REMARK");

            System.out.println("=== addAccount params ===");
            System.out.println("PROP: " + prop);
            System.out.println("OWNER: " + owner);
            System.out.println("ACCNAME: " + accname);
            System.out.println("ACCOUNT: " + account);
            System.out.println("BALANCE: " + balance);
            System.out.println("REMARK: " + remark);

            String sql = "INSERT INTO T_ACCOUNT (PROP,OWNER,ACCNAME,ACCOUNT,BALANCE,REMARK,OPERATER,CREATETIME,UPDATETIME) VALUES (?,?,?,?,?,?,?,NOW(),NOW())";

            int result = jdbcTemplate.update(sql, prop, owner, accname, account, balance, remark, "admin");
            System.out.println("=== insert result: " + result + " ===");

            Map<String, String> resultMap = new HashMap<>();
            resultMap.put("code", "0");
            resultMap.put("msg", "创建成功");
            return resultMap;
        } catch (Exception e) {
            System.out.println("=== addAccount error: " + e.getMessage() + " ===");
            e.printStackTrace();
            Map<String, String> resultMap = new HashMap<>();
            resultMap.put("code", "1");
            resultMap.put("msg", "创建失败: " + e.getMessage());
            return resultMap;
        }
    }

    @Override
    public String delAccount(String id) {
        String sql = "DELETE FROM T_ACCOUNT WHERE AID=?";
        jdbcTemplate.update(sql, id);
        return "";
    }

    @Override
    public String delDetail(String id) {
        try {
            // 先查询流水详情
            String querydetail = "SELECT AID, TRTYPE, TRNUM FROM T_WATER WHERE WID = ?";
            Map<String, Object> waterMap = jdbcTemplate.queryForMap(querydetail, id);
            if (waterMap == null || waterMap.isEmpty()) {
                return "0";
            }

            int aid = (Integer) waterMap.get("AID");
            String trtype = (String) waterMap.get("TRTYPE");
            BigDecimal trnum = (BigDecimal) waterMap.get("TRNUM");

            // 查询账户是否存在
            String queryAccount = "SELECT PROP, BALANCE FROM T_ACCOUNT WHERE AID = ?";
            List<Map<String, Object>> accountList = jdbcTemplate.queryForList(queryAccount, aid);
            
            if (accountList.isEmpty()) {
                // 孤儿流水，直接删除
                String delwater = "DELETE FROM T_WATER WHERE WID=?";
                jdbcTemplate.update(delwater, id);
                return "1";
            }
            
            // 账户存在，按账户类型计算新余额
            Map<String, Object> accountMap = accountList.get(0);
            String prop = (String) accountMap.get("PROP");
            BigDecimal currentBalance = (BigDecimal) accountMap.get("BALANCE");

            // 计算新余额
            // 资产类型(PROP=1): 删除入金减少余额，删除出金增加余额
            // 负债类型(PROP=2): 删除出金减少负债(余额增加)，删除入金增加负债(余额减少)
            BigDecimal newBalance = currentBalance;
            if ("1".equals(trtype)) {
                // 入金
                if ("2".equals(prop)) {
                    newBalance = currentBalance.add(trnum); // 负债类型删除入金（还款）增加负债
                } else {
                    newBalance = currentBalance.subtract(trnum); // 资产类型删除入金减少余额
                }
            } else if ("2".equals(trtype)) {
                // 出金
                if ("2".equals(prop)) {
                    newBalance = currentBalance.subtract(trnum); // 负债类型删除出金（消费）减少负债
                } else {
                    newBalance = currentBalance.add(trnum); // 资产类型删除出金增加余额
                }
            }

            // 删除流水
            String delwater = "DELETE FROM T_WATER WHERE WID=?";
            jdbcTemplate.update(delwater, id);

            // 更新账户余额
            jdbcTemplate.update("UPDATE T_ACCOUNT SET BALANCE =? WHERE AID=?", newBalance, aid);
            
            // 重新计算并更新该账户所有流水记录的余额
            recalculateWaterBalances(aid);
            
            return "1";
        } catch (Exception e) {
            e.printStackTrace();
            return "0";
        }
    }
    
    // 重新计算流水的余额
    private void recalculateWaterBalances(int aid) {
        // 按时间正序获取该账户所有流水记录
        String sql = "SELECT WID, TRTYPE, TRNUM FROM T_WATER WHERE AID = ? ORDER BY CREATETIME ASC, WID ASC";
        List<Map<String, Object>> waterList = jdbcTemplate.queryForList(sql, aid);
        
        BigDecimal balance = BigDecimal.ZERO;
        for (Map<String, Object> w : waterList) {
            int wid = (Integer) w.get("WID");
            String trtype = (String) w.get("TRTYPE");
            BigDecimal trnum = (BigDecimal) w.get("TRNUM");
            
            if ("1".equals(trtype)) {
                balance = balance.add(trnum); // 入金
            } else {
                balance = balance.subtract(trnum); // 出金
            }
            
            // 更新该条流水的余额
            jdbcTemplate.update("UPDATE T_WATER SET BALANCE = ? WHERE WID = ?", balance, wid);
        }
    }

    @Override
    public Map<String, String> addDetail(Map<String, String> param) {
        String aid = param.get("AID");
        String trdate = param.get("TRDATE");
        String tradekind = param.get("TRKIND");
        String trtype = param.get("TRTYPE");
        String waccount = param.get("WACCOUNT");
        String waccname = param.get("WACCNAME");
        String trnum = param.get("TRNUM");
        String remark = param.get("REMARK");
        String oppid = param.get("OPPID");
        String ifauto = param.get("IFAUTO");
        String cid = param.get("CID");

        if (tradekind == null) tradekind = "0";
        if (trtype == null) trtype = "0";
        if (ifauto == null) ifauto = "0";

        // 获取当前账户余额和类型
        BigDecimal currentBalance = new BigDecimal("0");
        String prop = "1"; // 默认为资产类型
        try {
            Map<String, Object> accountMap = jdbcTemplate.queryForMap("SELECT BALANCE, PROP FROM T_ACCOUNT WHERE AID = ?", aid);
            currentBalance = (BigDecimal) accountMap.get("BALANCE");
            prop = (String) accountMap.get("PROP");
        } catch (Exception e) {
            // ignore
        }

        // 计算流水后的余额
        // 资产类型( PROP=1 ): 入金增加，出金减少
        // 负债类型( PROP=2 ): 入金减少，出金增加（信用卡消费相当于还款，额度增加）
        BigDecimal num = new BigDecimal(trnum);
        BigDecimal newBalance = currentBalance;
        if ("1".equals(trtype)) {
            // 入金
            if ("2".equals(prop)) {
                newBalance = currentBalance.subtract(num); // 负债类型入金（还款）减少负债
            } else {
                newBalance = currentBalance.add(num); // 资产类型入金增加余额
            }
        } else {
            // 出金
            if ("2".equals(prop)) {
                newBalance = currentBalance.add(num); // 负债类型出金（消费）增加负债（额度减少）
            } else {
                newBalance = currentBalance.subtract(num); // 资产类型出金减少余额
            }
        }

        // 插入流水记录，包含余额
        String sql = "INSERT INTO T_WATER (AID,TRDATE,TRADEKIND,TRTYPE,WACCOUNT,WACCNAME,TRNUM,BALANCE,REMARK,OPPID,CREATETIME,UPDATETIME,IFAUTO,CID) VALUES(?,?,?,?,?,?,?,?,?,?,NOW(),NOW(),?,?)";
        jdbcTemplate.update(sql, aid, trdate, tradekind, trtype, waccount, waccname, trnum, newBalance, remark, oppid, ifauto, cid);

        // 更新账户余额
        jdbcTemplate.update("UPDATE T_ACCOUNT SET BALANCE = ? WHERE AID=?", newBalance, aid);

        Map<String, String> result = new HashMap<>();
        result.put("code", "0");
        result.put("msg", "添加成功");
        return result;
    }

    @Override
    public List<Map<String, Object>> queryKind() {
        String sql = "SELECT DICKEY AS VALUE, DICVAL AS TEXT FROM T_DICT WHERE TEAM='A' ORDER BY ORDENUM";
        return jdbcTemplate.queryForList(sql);
    }

    @Override
    public List<Map<String, Object>> queryMonth() {
        String sql = "SELECT DATE_FORMAT(TRDATE,'%Y-%m') AS months, " +
                "SUM(CASE TRTYPE WHEN '0' THEN -TRNUM ELSE 0 END) AS outcome, " +
                "SUM(CASE TRTYPE WHEN '1' THEN TRNUM ELSE 0 END) AS income, " +
                "SUM(CASE TRTYPE WHEN '0' THEN -TRNUM ELSE TRNUM END) AS total " +
                "FROM T_WATER T LEFT JOIN T_ACCOUNT A ON T.AID=A.AID " +
                "WHERE ((A.PROP='2' AND T.TRTYPE='0') OR A.PROP='1') " +
                "AND T.CID NOT IN (20, 21) " +
                "GROUP BY months ORDER BY months DESC";
        return jdbcTemplate.queryForList(sql);
    }

    @Override
    public List<Map<String, Object>> queryMonthItem(String datestr) {
        String sql = "SELECT A.accname, T.wid, trdate, T.remark, T.trnum, T.trtype " +
                "FROM T_WATER T LEFT JOIN T_ACCOUNT A ON T.AID=A.AID " +
                "WHERE ((A.PROP='2' AND T.TRTYPE='0') OR A.PROP='1') " +
                "AND T.CID NOT IN (20, 21) " +
                "AND DATE_FORMAT(TRDATE,'%Y-%m') = ? ORDER BY T.UPDATETIME DESC";
        return jdbcTemplate.queryForList(sql, datestr);
    }

    @Override
    public List<Map<String, Object>> queryYearReport(String datestr) {
        return new ArrayList<>();
    }

    @Override
    public List<Map<String, Object>> queryTouziInfo(String ttype) {
        return new ArrayList<>();
    }

    @Override
    public void updateTouziInfo(String tcode, String aid, Map<String, String> touziMap) {
    }

    @Override
    public void updateTouziTime(String datestr) {
    }

    @Override
    public List<Map<String, Object>> queryTodayTouziMoney(String datestr) {
        return new ArrayList<>();
    }

    @Override
    public List<Map<String, Object>> queryHeatMapMoney(String datestr) {
        return new ArrayList<>();
    }

    @Override
    public List<Map<String, Object>> queryDayFunds() {
        return new ArrayList<>();
    }

    @Override
    public List<Map<String, Object>> monthFunds(String datestr) {
        return new ArrayList<>();
    }

    @Override
    public List<Map<String, Object>> earnTotal(String datestr) {
        return new ArrayList<>();
    }

    @Override
    public List<Map<String, Object>> fenbu() {
        return new ArrayList<>();
    }

    @Override
    public List<String> everymonth(String yyyy) {
        return new ArrayList<>();
    }

    @Override
    public boolean isBeakDay() {
        return false;
    }

    @Override
    public void resetDate() {
    }

    @Override
    public List<BeanCategory> queryCategory(String ctype) {
        String sql = "SELECT * FROM T_CATEGORY WHERE CTYPE=? ORDER BY CSORT";
        return jdbcTemplate.query(sql, new BeanCategory(), ctype);
    }

    @Override
    public List<Map<String, Object>> queryCategoryStats(String datestr) {
        String sql = "SELECT c.CNAME as name, SUM(w.TRNUM) as value " +
                     "FROM T_WATER w LEFT JOIN T_CATEGORY c ON w.CID = c.CID " +
                     "WHERE w.TRDATE LIKE ? AND w.TRTYPE = '2' AND w.CID NOT IN (20, 21) " +
                     "GROUP BY c.CNAME ORDER BY value DESC";
        return jdbcTemplate.queryForList(sql, datestr + "%");
    }

    @Override
    @Transactional
    public Map<String, String> transfer(String fromAid, String toAid, String amount, String trdate, String remark) {
        Map<String, String> result = new HashMap<>();
        
        try {
            BigDecimal num = new BigDecimal(amount);
            if (num.compareTo(BigDecimal.ZERO) <= 0) {
                result.put("code", "1");
                result.put("msg", "转账金额必须大于0");
                return result;
            }

            // 获取转出账户信息
            Map<String, Object> fromAccount;
            try {
                fromAccount = jdbcTemplate.queryForMap("SELECT ACCNAME, BALANCE, PROP FROM T_ACCOUNT WHERE AID = ?", fromAid);
            } catch (Exception e) {
                result.put("code", "1");
                result.put("msg", "转出账户不存在");
                return result;
            }

            // 获取转入账户信息
            Map<String, Object> toAccount;
            try {
                toAccount = jdbcTemplate.queryForMap("SELECT ACCNAME, BALANCE, PROP FROM T_ACCOUNT WHERE AID = ?", toAid);
            } catch (Exception e) {
                result.put("code", "1");
                result.put("msg", "转入账户不存在");
                return result;
            }

            String fromAccname = (String) fromAccount.get("ACCNAME");
            String toAccname = (String) toAccount.get("ACCNAME");
            BigDecimal fromBalance = (BigDecimal) fromAccount.get("BALANCE");
            BigDecimal toBalance = (BigDecimal) toAccount.get("BALANCE");
            String fromProp = (String) fromAccount.get("PROP");
            String toProp = (String) toAccount.get("PROP");

            // 计算转出后余额
            BigDecimal newFromBalance = fromBalance;
            if ("1".equals(fromProp)) {
                // 资产类型出金减少余额
                newFromBalance = fromBalance.subtract(num);
            } else {
                // 负债类型出金增加余额（信用卡出金相当于还款，额度恢复）
                newFromBalance = fromBalance.add(num);
            }

            if (newFromBalance.compareTo(BigDecimal.ZERO) < 0) {
                result.put("code", "1");
                result.put("msg", "转出账户余额不足");
                return result;
            }

            // 计算转入后余额
            BigDecimal newToBalance = toBalance;
            if ("1".equals(toProp)) {
                // 资产类型入金增加余额
                newToBalance = toBalance.add(num);
            } else {
                // 负债类型入金减少负债（信用卡入金相当于消费，额度减少）
                newToBalance = toBalance.subtract(num);
            }

            // 获取当前时间
            String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            if (trdate == null || trdate.isEmpty()) {
                trdate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            }

            // 插入转出流水（出金，还款类别）
            String sqlFrom = "INSERT INTO T_WATER (AID,TRDATE,TRADEKIND,TRTYPE,WACCOUNT,WACCNAME,TRNUM,BALANCE,REMARK,OPPID,CREATETIME,UPDATETIME,IFAUTO,CID) VALUES(?,?,'0','2',?,?,?,?,?,?,NOW(),NOW(),'0',20)";
            jdbcTemplate.update(sqlFrom, fromAid, trdate, fromAid, fromAccname, num, newFromBalance, "转出到" + toAccname + (remark != null ? "：" + remark : ""), toAid);

            // 插入转入流水（入金，还款类别）
            String sqlTo = "INSERT INTO T_WATER (AID,TRDATE,TRADEKIND,TRTYPE,WACCOUNT,WACCNAME,TRNUM,BALANCE,REMARK,OPPID,CREATETIME,UPDATETIME,IFAUTO,CID) VALUES(?,?,'0','1',?,?,?,?,?,?,NOW(),NOW(),'0',21)";
            jdbcTemplate.update(sqlTo, toAid, trdate, toAid, toAccname, num, newToBalance, "从" + fromAccname + "转入" + (remark != null ? "：" + remark : ""), fromAid);

            // 更新转出账户余额
            jdbcTemplate.update("UPDATE T_ACCOUNT SET BALANCE = ? WHERE AID = ?", newFromBalance, fromAid);

            // 更新转入账户余额
            jdbcTemplate.update("UPDATE T_ACCOUNT SET BALANCE = ? WHERE AID = ?", newToBalance, toAid);

            result.put("code", "0");
            result.put("msg", "转账成功：" + fromAccname + " -> " + toAccname + " " + amount + "元");
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", "1");
            result.put("msg", "转账失败: " + e.getMessage());
            return result;
        }
    }
}