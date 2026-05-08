package com.zijin.college.dto;

import lombok.Data;

@Data
public class DepartmentQueryDTO {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String keyword;
}
