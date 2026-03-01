package com.luxinx.service.impl;

import com.luxinx.bean.BeanAccount;
import com.luxinx.bean.BeanCategory;
import com.luxinx.bean.BeanWater;
import com.luxinx.service.ServiceDataAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
public class DataAccountServiceImpl implements ServiceDataAccount {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public List<BeanWater> queryAccountInfoById(String id) {
        String sql = "SELECT WID,AID,TRDATE,TRADEKIND,TRTYPE,TRNUM,REMARK,UPDATETIME FROM T_WATER WHERE AID=? ORDER BY UPDATETIME DESC";
        return jdbcTemplate.query(sql, new BeanWater(), id);
    }

    @Override
    public List<BeanWater> queryAllWater() {
        String sql = "SELECT WID,AID,TRDATE,TRADEKIND,TRTYPE,TRNUM,REMARK,UPDATETIME,CID FROM T_WATER ORDER BY UPDATETIME DESC";
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
            
            // 账户存在，按原逻辑删除
            Map<String, Object> accountMap = accountList.get(0);
            String prop = (String) accountMap.get("PROP");
            BigDecimal currentBalance = (BigDecimal) accountMap.get("BALANCE");

            // 计算新余额：删除入金则减少余额，删除出金则增加余额
            BigDecimal newBalance = currentBalance;
            if ("1".equals(trtype)) {
                // 入金（收入），删除时减去
                newBalance = currentBalance.subtract(trnum);
            } else if ("2".equals(trtype)) {
                // 出金（支出），删除时加回
                newBalance = currentBalance.add(trnum);
            }

            // 删除流水
            String delwater = "DELETE FROM T_WATER WHERE WID=?";
            jdbcTemplate.update(delwater, id);

            // 更新账户余额
            jdbcTemplate.update("UPDATE T_ACCOUNT SET BALANCE =? WHERE AID=?", newBalance, aid);
            return "1";
        } catch (Exception e) {
            e.printStackTrace();
            return "0";
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

        String sql = "INSERT INTO T_WATER (AID,TRDATE,TRADEKIND,TRTYPE,WACCOUNT,WACCNAME,TRNUM,REMARK,OPPID,CREATETIME,UPDATETIME,IFAUTO,CID) VALUES(?,?,?,?,?,?,?,?,?,NOW(),NOW(),?,?)";

        jdbcTemplate.update(sql, aid, trdate, tradekind, trtype, waccount, waccname, trnum, remark, oppid, ifauto, cid);

        BigDecimal num = new BigDecimal(trnum);
        if ("1".equals(trtype)) {
            jdbcTemplate.update("UPDATE T_ACCOUNT SET BALANCE = BALANCE + ? WHERE AID=?", num, aid);
        } else {
            jdbcTemplate.update("UPDATE T_ACCOUNT SET BALANCE = BALANCE - ? WHERE AID=?", num, aid);
        }

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
                "WHERE (A.PROP='2' AND T.TRTYPE='0') OR A.PROP='1' " +
                "GROUP BY months ORDER BY months DESC";
        return jdbcTemplate.queryForList(sql);
    }

    @Override
    public List<Map<String, Object>> queryMonthItem(String datestr) {
        String sql = "SELECT A.accname, T.wid, trdate, T.remark, T.trnum, T.trtype " +
                "FROM T_WATER T LEFT JOIN T_ACCOUNT A ON T.AID=A.AID " +
                "WHERE ((A.PROP='2' AND T.TRTYPE='0') OR A.PROP='1') " +
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
                     "WHERE w.TRDATE LIKE ? AND w.TRTYPE = '2' " +
                     "GROUP BY c.CNAME ORDER BY value DESC";
        return jdbcTemplate.queryForList(sql, datestr + "%");
    }
}