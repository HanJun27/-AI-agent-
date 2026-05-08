package com.zijin.college.entity;

import lombok.Data;
import java.util.Date;

@Data
public class User {
    private Integer id;
    private String username;
    private String password;
    private String realName;
    private String avatar;
    private Integer status;
    private Date createTime;
    private Date updateTime;
}
