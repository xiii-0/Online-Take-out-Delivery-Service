package com.sky.service.impl;

import com.sky.constant.StatusConstant;
import com.sky.service.ShopService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ShopServiceImpl implements ShopService {
    public static final String key = "sky:SHOP_STATUS";
    @Autowired
    private RedisTemplate redisTemplate;
    /**
     * 设置店铺营业状态
     * @param status
     */
    public void setStatus(Integer status){
        redisTemplate.opsForValue().set(key, status);
    }

    /**
     * 查询店铺状态
     * @return
     */
    public Integer getStatus(){
        Integer status = (Integer) redisTemplate.opsForValue().get(key);
        if (status == null){ // 如果redis中没有，则设置为0 (打烊中)
            status = 0;
            setStatus(status);
        }
        return status;
    }
}
