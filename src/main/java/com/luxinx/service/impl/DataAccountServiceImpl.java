package com.luxinx.service.impl;

import com.luxinx.bean.BeanAccount;
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
        String sql = "SELECT WID,AID,TRDATE,TRADEKIND,TRTYPE,TRNUM,REMARK,UPDATETIME FROM T_WATER ORDER BY UPDATETIME DESC";
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
        String querydetail = "SELECT T.AID,T.PROP,W.WID,T.BALANCE,W.TRTYPE,W.TRNUM FROM T_ACCOUNT T LEFT JOIN T_WATER W ON T.AID = W.AID WHERE W.WID = ?";
        Map<String, Object> mapdetail = jdbcTemplate.queryForMap(querydetail, id);
        if (mapdetail == null || mapdetail.isEmpty()) {
            return "0";
        }

        String prop = (String) mapdetail.get("PROP");
        String trtype = (String) mapdetail.get("TRTYPE");
        BigDecimal trnum = (BigDecimal) mapdetail.get("TRNUM");

        String delaccount = "DELETE FROM T_WATER WHERE WID=?";
        jdbcTemplate.update(delaccount, id);

        int aid = (Integer) mapdetail.get("AID");
        BigDecimal resultBalance = trnum;
        if ("2".equals(prop) && "0".equals(trtype)) {
            resultBalance = resultBalance.negate();
        } else if ("1".equals(prop) && "1".equals(trtype)) {
            resultBalance = resultBalance.negate();
        }

        jdbcTemplate.update("UPDATE T_ACCOUNT SET BALANCE =? WHERE AID=?", resultBalance, aid);

        return resultBalance.toString();
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

        if (tradekind == null) tradekind = "0";
        if (trtype == null) trtype = "0";
        if (ifauto == null) ifauto = "0";

        String sql = "INSERT INTO T_WATER (AID,TRDATE,TRADEKIND,TRTYPE,WACCOUNT,WACCNAME,TRNUM,REMARK,OPPID,CREATETIME,UPDATETIME,IFAUTO) VALUES(?,?,?,?,?,?,?,?,?,NOW(),NOW(),?)";

        jdbcTemplate.update(sql, aid, trdate, tradekind, trtype, waccount, waccname, trnum, remark, oppid, ifauto);

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
}