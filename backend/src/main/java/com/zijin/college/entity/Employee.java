package com.zijin.college.entity;

import lombok.Data;
import java.util.Date;

@Data
public class Employee {
    private Integer id;
    private String empNo;
    private String empName;
    private String gender;
    private Integer age;
    private String phone;
    private String email;
    private String position;
    private Integer deptId;
    private Date hireDate;
    private Integer status;
    private Date createTime;
    private Date updateTime;
}
