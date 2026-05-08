package com.zijin.college.mapper;

import com.zijin.college.entity.ApiConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ApiConfigMapper {
    
    List<ApiConfig> findAll();
    
    ApiConfig findByProvider(@Param("provider") String provider);
    
    int insert(ApiConfig config);
    
    int update(ApiConfig config);
    
    int deleteByProvider(@Param("provider") String provider);
}
