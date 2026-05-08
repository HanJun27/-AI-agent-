package com.zijin.college.service;

import com.zijin.college.entity.ChatMessage;
import com.zijin.college.entity.ChatSession;

import java.util.List;

public interface ChatHistoryService {
    
    List<ChatSession> getUserSessions(Integer userId);
    
    ChatSession getSession(String sessionId);
    
    List<ChatMessage> getSessionMessages(String sessionId);
    
    void saveSession(ChatSession session);
    
    void saveMessage(ChatMessage message);
    
    void deleteSession(String sessionId);
}
