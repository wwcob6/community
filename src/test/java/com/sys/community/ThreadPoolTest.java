package com.sys.community;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.test.context.ContextConfiguration;

import java.util.Date;
import java.util.concurrent.*;

@SpringBootTest
@ContextConfiguration(classes =  CommunityApplication.class)
public class ThreadPoolTest {

    private final static Logger logger = LoggerFactory.getLogger(ThreadPoolTest.class);

    // jdk普通线程池
    private ExecutorService executorService = Executors.newFixedThreadPool(5);
    // jdk可执行定时任务的线程池
    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(5);

    // spring普通线程池
    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;
    // spring可执行定时任务的线程池
    @Autowired
    private ThreadPoolTaskScheduler taskScheduler;
    private void sleep(long m) {
        try {
            Thread.sleep(m);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    // jdk
    /*@Test
    public void testExecutorService() {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                logger.info("hello,executorService");
            }
        };

        for (int i = 0; i < 10; i++) {
            executorService.submit(task);
        }

        sleep(10000);
    }
    @Test
    public void testScheduledExecutorService() {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                logger.info("hello,scheduledExecutorService");
            }
        };
        scheduledExecutorService.scheduleAtFixedRate(task, 10000, 1000, TimeUnit.MILLISECONDS);
        sleep(30000);
    }*/
    //spring
    @Test
    public void testThreadPoolTaskExecutor() {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                logger.info("hello,ThreadPoolTaskExecutor");
            }
        };
        for (int i = 0; i < 10; i++) {
            taskExecutor.submit(task);
        }
        sleep(10000);
    }
    @Test
    public void testThreadPoolTaskScheduler() {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                logger.info("hello,ThreadPoolTaskScheduler");
            }
        };
        Date date = new Date(System.currentTimeMillis() + 5000);
        taskScheduler.scheduleAtFixedRate(task, date, 1000);
        sleep(30000);
    }
}
