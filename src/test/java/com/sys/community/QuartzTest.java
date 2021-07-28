package com.sys.community;

import org.junit.jupiter.api.Test;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes =  CommunityApplication.class)
public class QuartzTest {

    @Autowired
    private Scheduler scheduler;

    @Test
    public void deleteJob() {
        try {
            boolean b = scheduler.deleteJob(new JobKey("alohaJob", "alphaJobGroup"));
            System.out.println(b);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

}
