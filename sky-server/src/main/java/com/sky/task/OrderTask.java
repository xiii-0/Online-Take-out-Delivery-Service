package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;

@Component
@Slf4j
public class OrderTask {
    @Autowired
    private OrderMapper orderMapper;

    /**
     * 处理超时未支付订单
     */
    @Scheduled(cron = "0 0/5 * * * ?") // 每5分钟触发一次
    public void processTimedOutOrder(){
        log.info("定时处理超时订单：{}", LocalDateTime.now());
        HashMap<String, Object> map = new HashMap<>();

        LocalDateTime time = LocalDateTime.now().plusMinutes(-15); // 查询15分钟前的订单
        Orders order = Orders.builder()
                .status(Orders.CANCELLED)
                .cancelReason("Payment Timeout")
                .cancelTime(LocalDateTime.now())
                .build();

        map.put("order", order);
        map.put("status", Orders.PENDING_PAYMENT);
        map.put("time", time);

        orderMapper.updateOrderStatus(map);
    }

    /**
     * 处理异常派送状态订单
     */
    @Scheduled(cron = "0 0 0 * * ?") // 每天0点触发，将所有仍在派送中的订单设置为已完成
    public void processInDeliveryOrder(){
        log.info("定时处理异常派送状态订单：{}", LocalDateTime.now());
        HashMap<String, Object> map = new HashMap<>();

        Orders order = Orders.builder()
                .status(Orders.COMPLETED)
                .build();

        map.put("order", order);
        map.put("status", Orders.DELIVERY_IN_PROGRESS);
        map.put("time", LocalDateTime.now());

        orderMapper.updateOrderStatus(map);
    }
}
