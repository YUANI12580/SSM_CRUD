package com.atguigu.crud.controller;

import com.atguigu.crud.bean.Employee;
import com.atguigu.crud.bean.Msg;
import com.atguigu.crud.service.EmployeeService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @program: SSM_CRUD
 * @description: 处理员工增删改查
 * @Author: Yuan
 * @Date: 2022-07-20 09:03
 **/
@Controller
public class EmployeeController {

    @Autowired
    EmployeeService employeeService;

    @RequestMapping("/emps")
    @ResponseBody
    public Msg getEmpsWithJson(@RequestParam(value = "pn", defaultValue = "1") Integer pn) {
        // 引入PageHelper插件
        // 在查询之前调用, 传入页码以及每页大小
        PageHelper.startPage(pn, 5);
        // startPage 后紧跟的查询就是分页查询
        List<Employee> emps = employeeService.getAll();
        // 使用 pageInfo 包装查询后的结果, 只需要将 pageInfo 交给页面
        // 封装了详细的分页信息, 包括查询数据, 传入连续显示的页数
        PageInfo page = new PageInfo<>(emps, 5);
        return Msg.success().add("pageInfo", page);
    }

    // 查询员工数据(分页查询)
//    @RequestMapping("/emps")
    public String getEmp(@RequestParam(value = "pn", defaultValue = "1") Integer pn, Model model) {
        // 引入PageHelper插件
        // 在查询之前调用, 传入页码以及每页大小
        PageHelper.startPage(pn, 5);
        // startPage 后紧跟的查询就是分页查询
        List<Employee> emps = employeeService.getAll();
        // 使用 pageInfo 包装查询后的结果, 只需要将 pageInfo 交给页面
        // 封装了详细的分页信息, 包括查询数据, 传入连续显示的页数
        PageInfo page = new PageInfo<>(emps, 5);
        model.addAttribute("pageInfo", page);
        return "list";
    }

    // 员工保存
    @RequestMapping(value = "/emp", method = RequestMethod.POST)
    @ResponseBody
    public Msg saveEmp(@Valid Employee employee, BindingResult result) {
        if (result.hasErrors()) {
            // 返回失败, 在模态框中显示错误信息
            Map<String, Object> map = new HashMap<>();
            List<FieldError> errors = result.getFieldErrors();
            for (FieldError fieldError : errors) {
                System.out.println("错误的字段名: " + fieldError.getField());
                System.out.println("错误信息: " + fieldError.getDefaultMessage());
                map.put(fieldError.getField(), fieldError.getDefaultMessage());
            }
            return Msg.fail().add("errorFields", map);
        } else {
            employeeService.saveEmp(employee);
            return Msg.success();
        }
    }

    // 检验用户名是否可用
    @RequestMapping("/checkUser")
    @ResponseBody
    public Msg checkUser(@RequestParam("empName") String empName) {
        // 先判断用户名是否是合法的表达式
        String regx = "(^[a-zA-Z0-9_-]{6,16}$)|(^[\\u2E80-\\u9FFF]{2,5})";
        if (!empName.matches(regx)) {
            return Msg.fail().add("val_msg", "用户名必须是2-5位中文或者6-16位英文和数字的组合");
        }
        // 数据库用户名重复校验
        boolean b = employeeService.checkUser(empName);
        if (b) {
            return Msg.success();
        } else {
            return Msg.fail().add("val_msg", "用户名不可用");
        }
    }

    // 根据id查询员工信息
    @RequestMapping(value = "/emp/{id}", method = RequestMethod.GET)
    @ResponseBody
    public Msg getEmp(@PathVariable("id") Integer id) {
        Employee employee = employeeService.getEmp(id);
        return Msg.success().add("emp", employee);
    }

    // 更新员工信息

    /**
     * AJAX直接发送PUT请求的数据时, TOMCAT不会对其进行封装为MAP
     * 配置HttpPutFormContentFilter, 将请求体中的数据解析包装成一个map, 重写request
     *
     * @param employee
     * @return
     */
    @RequestMapping(value = "/emp/{empId}", method = RequestMethod.PUT)
    @ResponseBody
    public Msg saveEmp(Employee employee) {
        employeeService.updateEmp(employee);
        return Msg.success();
    }

    /**
     * 单个批量二合一
     * 批量删除: 1-2-3
     * 单个删除: 1
     * @param ids
     * @return
     */
    @RequestMapping(value = "/emp/{ids}", method = RequestMethod.DELETE)
    @ResponseBody
    public Msg deleteEmpById(@PathVariable("ids") String ids) {
        // 批量删除
        if (ids.contains("-")) {
            String[] str_ids = ids.split("-");
            List<Integer> del_ids = new ArrayList<>();
            for (String str : str_ids) {
                del_ids.add(Integer.parseInt(str));
            }
            employeeService.deleteBatch(del_ids);
            // 单个删除
        } else {
            Integer id = Integer.parseInt(ids);
            employeeService.deleteEmp(id);
        }
        return Msg.success();
    }


}