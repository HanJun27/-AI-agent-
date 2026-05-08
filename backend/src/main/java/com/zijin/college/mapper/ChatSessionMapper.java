package com.zijin.college.mapper;

import com.zijin.college.entity.ChatSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChatSessionMapper {
    
    List<ChatSession> findByUserId(@Param("userId") Integer userId);
    
    ChatSession findBySessionId(@Param("sessionId") String sessionId);
    
    int insert(ChatSession session);
    
    int update(ChatSession session);
    
    int deleteBySessionId(@Param("sessionId") String sessionId);
}
