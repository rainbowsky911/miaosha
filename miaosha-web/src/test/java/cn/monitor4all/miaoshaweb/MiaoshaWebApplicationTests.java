package cn.monitor4all.miaoshaweb;

import cn.monitor4all.miaoshaservice.service.OrderService;
import cn.monitor4all.miaoshaservice.service.StockService;
import com.alibaba.fastjson.JSONObject;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.Assert;

import java.util.concurrent.ExecutorService;


@SpringBootTest
class MiaoshaWebApplicationTests {

    private static Logger logger = LoggerFactory.getLogger(MiaoshaWebApplicationTests.class);
    private static ExecutorService executorServicePool;
    private static String url = "http://127.0.0.1:8080/createOptimisticLimitOrder/1" ;

    @Autowired
    private OrderService orderService;
    @Autowired
    private AmqpTemplate rabbitTemplate;
    @Autowired
    private StockService stockService;
    @Test
    public  void testOrder() throws Exception {
        Boolean info = orderService.checkUserOrderInfoInCache(1, 1);
        System.out.println(info);
    }

    @Test
    public  void testGetStockCount() throws Exception {
        Integer stockCount = stockService.getStockCount(1);
        System.out.println(stockCount);
    }

    @Test
    public void testSendToOrderQueue() throws Exception {
        int sid=1;
        int userId=1;
        Integer count = stockService.getStockCount(sid);
        if (count == 0) {
            throw  new Exception( "秒杀请求失败，库存不足.....");
        }

        logger.info("有库存：[{}]", count);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("sid", sid);
        jsonObject.put("userId", userId);
        sendToOrderQueue(jsonObject.toJSONString());
    }

    /**
     * 向消息队列orderQueue发送消息
     * @param message
     */
    private void sendToOrderQueue(String message) throws InterruptedException {
        logger.info("这就去通知消息队列开始下单：[{}]", message);
        this.rabbitTemplate.convertAndSend("orderQueue", message);
        Thread.sleep(10000);
    }

}
