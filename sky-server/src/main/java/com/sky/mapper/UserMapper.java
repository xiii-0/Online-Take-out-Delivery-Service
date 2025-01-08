package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {

    /**
     * 根据openid查询用户信息
     * @param openid
     * @return
     */
    @Select("select * from user where openid = #{openid}")
    User getByOpenid(String openid);


    /**
     * 创建用户
     * @param user
     */
    void addUser(User user);

    /**
     * 根据id查询用户信息
     * @param id
     * @return
     */
    @Select("select * from user where id = #{id}")
    User getById(Long id);
}
