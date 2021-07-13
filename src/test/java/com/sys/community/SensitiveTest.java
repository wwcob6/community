package com.sys.community;

import com.sys.community.util.SensitiveFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes =  CommunityApplication.class)
public class SensitiveTest {

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Test
    public void test1() {
        String s = "草☆泥☆马草你吗nmsl☆习☆近☆平☆吸毒嫖娼";

        String filter = sensitiveFilter.filter(s);
        System.out.println(filter);
    }
}
