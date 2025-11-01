package com.ecoswap.ecoswap.ia.services;

import java.util.Map;

import org.springframework.web.bind.annotation.RequestBody;

public interface IAChatService {
 
    Map<String, String> chatAssistant(@RequestBody Map<String, Object> request);
 
    Map<String, Object> searchProducts(String query, String category);
   
    String buildCurrentProductsContext(Long userId);
   
    String getEcoSwapSystemPrompt();
    
    String buildContextualPrompt(String userMessage, String context, Long userId);
    
    String getFallbackResponse(String userMessage);
    
    String searchProductsInDatabase(String searchTerm, String category);
    
    String getAvailableProductsByCategory();
}
