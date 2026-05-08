package com.zijin.college.entity;

import lombok.Data;
import java.util.Date;

@Data
public class Department {
    private Integer id;
    private String deptNo;
    private String deptName;
    private String location;
    private String manager;
    private Date createTime;
    private Date updateTime;
}
