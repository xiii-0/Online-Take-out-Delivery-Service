package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class ShoppingCartServiceImpl implements ShoppingCartService {
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private DishMapper dishMapper;

    /**
     * 添加购物车
     * @param shoppingCartDTO
     */
    public void addIntoCart(ShoppingCartDTO shoppingCartDTO){
        // 先判断要加入的物品是否已经存在购物车中
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);

        List<ShoppingCart> items = shoppingCartMapper.list(shoppingCart);

        if (items != null && items.size() > 0){
            // 若物品已经在购物车中，则直接对其数量+1
            ShoppingCart item = items.get(0);
            item.setNumber(item.getNumber() + 1);
            shoppingCartMapper.updateNumber(item);
        }else{
            // 若不存在，则将其插入数据库中
            // 先判断是菜品还是套餐
            if (shoppingCart.getDishId() != null){
                Dish dish = dishMapper.getById(shoppingCart.getDishId());
                shoppingCart.setName(dish.getName());
                shoppingCart.setImage(dish.getImage());
                shoppingCart.setAmount(dish.getPrice());
                shoppingCart.setNumber(1);
                shoppingCart.setCreateTime(LocalDateTime.now());
                shoppingCartMapper.insert(shoppingCart);
            }else {
                // 将套餐加入购物车中，逻辑同上
            }
        }
    }


    /**
     * 查看购物车
     * @return
     */
    public List<ShoppingCart> list(){
        // 根据用户id查询购物车的内容
        ShoppingCart shoppingCart = new ShoppingCart();
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);
        List<ShoppingCart> items = shoppingCartMapper.list(shoppingCart);
        return items;
    }


    /**
     * 清空购物车
     */
    public void clear(){
        Long userId = BaseContext.getCurrentId();
        shoppingCartMapper.deleteByUserId(userId);
    }
}
