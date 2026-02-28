package com.luxinx.bean;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class BeanCategory implements RowMapper {
    private int cid;
    private String cname;
    private String ctype;
    private String cicon;
    private int csort;

    public int getCid() { return cid; }
    public void setCid(int cid) { this.cid = cid; }
    public String getCname() { return cname; }
    public void setCname(String cname) { this.cname = cname; }
    public String getCtype() { return ctype; }
    public void setCtype(String ctype) { this.ctype = ctype; }
    public String getCicon() { return cicon; }
    public void setCicon(String cicon) { this.cicon = cicon; }
    public int getCsort() { return csort; }
    public void setCsort(int csort) { this.csort = csort; }

    @Override
    public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
        BeanCategory cat = new BeanCategory();
        cat.setCid(rs.getInt("CID"));
        cat.setCname(rs.getString("CNAME"));
        cat.setCtype(rs.getString("CTYPE"));
        cat.setCicon(rs.getString("CICON"));
        cat.setCsort(rs.getInt("CSORT"));
        return cat;
    }
}
