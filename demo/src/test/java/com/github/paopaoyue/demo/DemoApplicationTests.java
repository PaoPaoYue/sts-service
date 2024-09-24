package com.github.paopaoyue.demo;

import com.github.paopaoyue.demo.api.IDemoCaller;
import com.github.paopaoyue.demo.proto.DemoProto;
import io.github.paopaoyue.mesh.rpc.api.CallOption;
import io.github.paopaoyue.mesh.rpc.util.RespBaseUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StopWatch;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.Fail.fail;

@SpringBootTest
class DemoApplicationTests {

    private static final Logger logger = LogManager.getLogger(DemoApplicationTests.class);

    @Autowired
    IDemoCaller demoCaller;

    @Test
    void smokeTest() {
        Random random = new Random();
        for (int i = 0; i < 20; i++) {
            testRequest(1, 100, false);
            try {
                Thread.sleep(random.nextInt(800) + 200);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
//        testRequest(1, 1, false);
//        try {
//            Thread.sleep(2000);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
    }

    private void testRequest(int threadNum, int requestNum, boolean dedicatedConnection) {
        CountDownLatch latch = new CountDownLatch(threadNum);
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        AtomicInteger success = new AtomicInteger();
        for (int i = 0; i < threadNum; i++) {
            String threadName = "thread-" + i;
            new Thread(() -> {
                for (int j = 0; j < requestNum; j++) {
                    var callOption = new CallOption();
                    if (dedicatedConnection) {
                        callOption.setConnectionTag(threadName);
                    }
                    var resp = demoCaller.echo(DemoProto.EchoRequest.newBuilder().setText("hello world").build(), callOption);
                    if (RespBaseUtil.isOK(resp.getBase())) {
                        success.addAndGet(1);
                    } else {
                        logger.info("message: {}", resp.getBase().getMessage());
                    }
                }
                latch.countDown();
            }, threadName).start();
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
//            fail("exception", e);
        }
        stopWatch.stop();
        logger.info("success: {}", success.get());
//        assertThat(success.get() == requestNum * threadNum).isTrue();
        logger.info("test consumed time: {}", stopWatch.getTotalTimeMillis() / 1000.0);
    }

}
