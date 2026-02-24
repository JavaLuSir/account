package com.luxinx.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtil {
    /**
     * 获取上个交易日的基金净值如果当天为周一获取上周五时间
     *
     * @return dateStr yyyy-mm-dd
     */
    public static String getLastDate() {
        String[] weeks = {"星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"};
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date dt = new Date();
        Calendar cd = Calendar.getInstance();
        cd.setTime(dt);
        String wk = weeks[cd.get(Calendar.DAY_OF_WEEK) - 1];
        if (wk.equals("星期一")) {
            cd.add(Calendar.DATE, -3);
        } else {
            cd.add(Calendar.DATE, -1);
        }
        return sdf.format(cd.getTime());
    }


    public static String getTodayDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date dt = new Date();
        return sdf.format(dt);
    }
}
