package com.zijin.college.mapper;

import com.zijin.college.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChatMessageMapper {
    
    List<ChatMessage> findBySessionId(@Param("sessionId") String sessionId);
    
    int insert(ChatMessage message);
    
    int batchInsert(@Param("messages") List<ChatMessage> messages);
    
    int deleteBySessionId(@Param("sessionId") String sessionId);
}
