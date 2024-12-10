package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.entity.DishFlavor;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper
public interface DishFlavorMapper {
    /**
     * 批量新增菜品口味
     * @param flavors
     */
//    @AutoFill(value = OperationType.INSERT) 口味没有相关字段
    void insertBatch(List<DishFlavor> flavors);

    /**
     * 根据id批量删除口味
     * @param ids
     */
    void deleteBatch(List<Long> ids);

    /**
     * 根据菜品id获取对应口味
     * @param dishId
     * @return
     */
    @Select("select * from dish_flavor where dish_id = #{dishId}")
    List<DishFlavor> getByDishId(Long dishId);
}
