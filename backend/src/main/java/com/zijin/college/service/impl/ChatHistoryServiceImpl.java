package com.zijin.college.service.impl;

import com.zijin.college.entity.ChatMessage;
import com.zijin.college.entity.ChatSession;
import com.zijin.college.mapper.ChatMessageMapper;
import com.zijin.college.mapper.ChatSessionMapper;
import com.zijin.college.service.ChatHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatHistoryServiceImpl implements ChatHistoryService {
    
    @Autowired
    private ChatSessionMapper chatSessionMapper;
    
    @Autowired
    private ChatMessageMapper chatMessageMapper;
    
    @Override
    public List<ChatSession> getUserSessions(Integer userId) {
        return chatSessionMapper.findByUserId(userId);
    }
    
    @Override
    public ChatSession getSession(String sessionId) {
        return chatSessionMapper.findBySessionId(sessionId);
    }
    
    @Override
    public List<ChatMessage> getSessionMessages(String sessionId) {
        return chatMessageMapper.findBySessionId(sessionId);
    }
    
    @Override
    public void saveSession(ChatSession session) {
        ChatSession existing = chatSessionMapper.findBySessionId(session.getSessionId());
        if (existing != null) {
            chatSessionMapper.update(session);
        } else {
            chatSessionMapper.insert(session);
        }
    }
    
    @Override
    public void saveMessage(ChatMessage message) {
        chatMessageMapper.insert(message);
    }
    
    @Override
    public void deleteSession(String sessionId) {
        chatMessageMapper.deleteBySessionId(sessionId);
        chatSessionMapper.deleteBySessionId(sessionId);
    }
}
