package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.config.RedisConfiguration;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private RedisTemplate redisTemplate;


    /**
     * 新增菜品
     *
     * @param dishDTO
     */
    @Transactional
    public void insertWithFlavor(DishDTO dishDTO) {
        clearCache(dishDTO.getId());

        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);

        // 新增一个菜品
        dishMapper.insert(dish);

        // 新增一个或多个菜品口味
        Long dishId = dish.getId(); // 获取insert后生成的dish的id
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && flavors.size() > 0) {
            flavors.forEach(
                    (flavor) -> flavor.setDishId(dishId)
            );
            dishFlavorMapper.insertBatch(flavors);
        }
    }


    /**
     * 菜品分页查询
     *
     * @param dishPageQueryDTO
     * @return
     */
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 批量删除菜品
     *
     * @param ids
     */
    @Transactional
    public void deleteBatch(List<Long> ids) {
        // TODO 该处可优化：不应该在循环中调用数据库查询（性能）；不应该批量删除中因为个别异常放弃全部删除操作（业务）

        clearCache(); // 清除全部缓存

        // 判断菜品状态
        for (Long id : ids) {
            Dish dish = dishMapper.getById(id);//后绪步骤实现
            if (dish.getStatus() == StatusConstant.ENABLE) {
                //当前菜品处于起售中，不能删除
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }
        // 判断是否有关联的套餐
        List<Long> setmealIds = setmealDishMapper.getSetmealIdsByDishIds(ids);
        if (setmealIds != null && setmealIds.size() > 0) {
            //当前菜品被套餐关联了，不能删除
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }
        // 删除菜品
        dishMapper.deleteBatch(ids);
        // 删除关联的口味（如有）
        dishFlavorMapper.deleteBatch(ids);
    }

    /**
     * 根据id获取菜品及对应口味
     *
     * @param id
     * @return
     */
    public DishVO getById(Long id) {
        Dish dish = dishMapper.getById(id);
        List<DishFlavor> flavors = dishFlavorMapper.getByDishId(id);
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish, dishVO);
        dishVO.setFlavors(flavors);
        return dishVO;
    }


    /**
     * 修改菜品信息
     *
     * @param dishDTO
     */
    public void update(DishDTO dishDTO) {
        clearCache(dishDTO.getId()); // 清除缓存

        // 修改菜品基本信息
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.update(dish);

        // 修改菜品对应口味 （由于每个菜品对应多个口味，直接先删除原有口味，再重新添加新口味）
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && flavors.size() > 0) {
            Long dishId = dishDTO.getId();
            List<Long> ids = new ArrayList<>();
            ids.add(dishId);
            flavors.forEach(flavor -> flavor.setDishId(dishId));
            dishFlavorMapper.deleteBatch(ids);
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    /**
     * 条件查询菜品和口味
     *
     * @param dish
     * @return
     */
    public List<DishVO> listWithFlavor(Dish dish) {
        // 查询该分类下的菜品是否已经缓存
        String key = "sky:dishes:cat_" + dish.getCategoryId();
        List<DishVO> dishVOList = (List<DishVO>) redisTemplate.opsForValue().get(key);
        if (dishVOList != null && dishVOList.size() != 0) {
            return dishVOList;
        }

        List<Dish> dishList = dishMapper.list(dish);

        dishVOList = new ArrayList<>();

        for (Dish d : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d, dishVO);

            //根据菜品id查询对应的口味
            List<DishFlavor> flavors = dishFlavorMapper.getByDishId(d.getId());

            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }
        // 将查询到的结果缓存，key设置为该分类的id
        redisTemplate.opsForValue().set(key, dishVOList);

        return dishVOList;
    }


    /**
     * 开始或停止菜品的售卖
     *
     * @param status
     * @param id
     */
    public void startOrStop(Integer status, Long id) {
        clearCache(id); // 清除缓存
        Dish dish = Dish.builder().id(id).status(status).build();
        dishMapper.update(dish);
    }

    /**
     * 根据菜品id清除无效缓存
     * @param id
     */
    private void clearCache(Long id){
        log.info("清除菜品{}缓存", id);
        if (id != null){
            // 根据菜品ID查分类ID
            Dish dish = dishMapper.getById(id);
            String key = "sky:dishes:cat_" + dish.getCategoryId();
            redisTemplate.delete(key);
        }else{
            clearCache();
        }

    }

    /**
     * 清除所有分类菜品缓存
     */
    private void clearCache(){
        log.info("清除全部菜品缓存");
        Set<Long> keys = redisTemplate.keys("sky:dishes:cat_*");
        redisTemplate.delete(keys);
    }
}
