package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OrderDetailMapper {
    /**
     * 批量插入订单菜品详情
     * @param orderDetailList
     */
    void insertBatch(List<OrderDetail> orderDetailList);

    /**
     * 根据订单id查询订单中的菜品详情
     * @param orderId
     * @return
     */
    @Select("select * from order_detail where order_id = #{orderId};")
    List<OrderDetail> getByOrderId(Long orderId);

    /**
     * 获取多个订单中的菜品详情
     * @param page
     * @return
     */
    List<OrderDetail> getByOrders(List<Orders> page);
}
