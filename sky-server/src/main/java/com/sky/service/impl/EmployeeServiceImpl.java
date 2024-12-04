package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;
import com.sky.service.EmployeeService;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        // password to md5
        password = DigestUtils.md5Hex(password.getBytes());
        if (!password.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }

    /**
     * 新增员工
     * @param employeeDTO
     */
    public void save(EmployeeDTO employeeDTO){
        Employee employee = new Employee();
        // 将所有DTO的属性复制到entity
        BeanUtils.copyProperties(employeeDTO, employee);

        // 设置其他数据的值

        // 状态默认为1
        employee.setStatus(StatusConstant.ENABLE);
        // 密码默认为123456，使用MD5加密
        employee.setPassword(DigestUtils.md5Hex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));

        // 插入数据库
        employeeMapper.insert(employee);
    }

    /**
     * 员工分页查询
     * @param employeePageQueryDTO
     * @return
     */
    public PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO){
        PageHelper.startPage(employeePageQueryDTO.getPage(), employeePageQueryDTO.getPageSize());
        Page<Employee> page = employeeMapper.pageQuery(employeePageQueryDTO);
        PageResult result = new PageResult(page.getTotal(), page.getResult());
        return result;
    }

    /**
     * 启用/禁用员工账号 （状态1启用，0禁用）
     * @param status
     * @param id
     */
    public void startOrStop(Integer status, Long id){
        Employee employee = Employee.builder()
                .id(id).status(status)
                .build();

        employeeMapper.update(employee);
    }

    /**
     * 根据id查询员工信息
     * @param id
     * @return
     */
    public Employee getById(Long id){
        Employee employee = employeeMapper.getById(id);
//        加强安全性，不传输密码给前端
        employee.setPassword("******");
        return employee;
    }

    /**
     * 修改员工信息
     * @param employeeDTO
     */
    public void update(EmployeeDTO employeeDTO){
        Employee employee = new Employee();
        // 拷贝DTO中需要修改的属性，调用mapper中通用的update方法
        BeanUtils.copyProperties(employeeDTO, employee);

        employeeMapper.update(employee);
    }
}
