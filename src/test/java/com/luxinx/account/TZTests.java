package com.luxinx.account;

import com.luxinx.cron.Tzcrond;
import com.luxinx.service.ServiceDataAccount;
import com.luxinx.service.impl.DataAccountServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Map;


@ExtendWith(SpringExtension.class)
@SpringBootTest
class TZTests {

    @Autowired
    private Tzcrond tzcrond;
    @Autowired
    private ServiceDataAccount dataAccountService;
    @Test
    public void testCrond(){
        tzcrond.configureTaskFund();
    }

    @Test
    public void testHoliday(){
        System.out.println(dataAccountService.isBeakDay());
    }

    @Test
    public void testQueryYearReport(){
        List<Map<String, Object>> rst = dataAccountService.queryYearReport("2021");
        System.out.println(rst);
    }
}
