package com.ecoswap.ecoswap.ia.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecoswap.ecoswap.ia.services.IAChatService;

import java.util.Map;

@RestController
public class IAChatController {

    private final IAChatService iaChatService;

    @Autowired
    public IAChatController(IAChatService iaChatService) {
        this.iaChatService = iaChatService;
    }

    /**
     * Endpoint principal del chatbot con contexto completo de EcoSwap
     */
    @PostMapping({"/chat/assistant", "/api/v1/ai/assistant"})
    public Map<String, String> chatAssistant(@RequestBody Map<String, Object> request) {
        return iaChatService.chatAssistant(request);
    }
    
    /**
     * Endpoint especializado para búsqueda de productos específicos
     */
    @GetMapping("/chat/search-products")
    public Map<String, Object> searchProducts(@RequestParam("query") String query,
                                             @RequestParam(value = "category", required = false) String category) {
        return iaChatService.searchProducts(query, category);
    }

}
