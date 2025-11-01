package com.ecoswap.ecoswap.exchange.controllers;

import com.ecoswap.ecoswap.exchange.models.dto.CreateExchangeRequestDTO;
import com.ecoswap.ecoswap.exchange.models.dto.ExchangeDTO;
import com.ecoswap.ecoswap.exchange.services.ExchangeService;
import com.ecoswap.ecoswap.product.models.dto.ProductDTO;
import com.ecoswap.ecoswap.user.models.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class ExchangeController {

    @Autowired
    private ExchangeService exchangeService;

    @PostMapping("/create-exchange")
    public ResponseEntity<ExchangeDTO> createRequestExchange(@RequestBody ExchangeDTO exchangeDTO){
        return ResponseEntity.status(HttpStatus.CREATED).body(exchangeService.createRequestExchange(exchangeDTO));
    }

    @PostMapping("/create-exchange-existing-product")
    public ResponseEntity<ExchangeDTO> createRequestExchangeWithExistingProduct(@RequestBody CreateExchangeRequestDTO request){
        return ResponseEntity.status(HttpStatus.CREATED).body(exchangeService.createRequestExchangeWithExistingProduct(request));
    }

    @PostMapping("/select-exchange")
    public ResponseEntity<ExchangeDTO> selectExchangeRequest(@RequestBody ExchangeDTO exchangeDTO){
        return ResponseEntity.ok(exchangeService.selectExchangeRequest(exchangeDTO));
    }

    @PostMapping("/exchanges")
    public ResponseEntity<List<ExchangeDTO>> getExchangeByProductTo(@RequestBody ProductDTO productDTO){
        return ResponseEntity.ok(exchangeService.findByProductTo(productDTO));
    }

    @GetMapping("/exchanges/counts")
    public ResponseEntity<Long> countExchanges(){
        return ResponseEntity.ok(exchangeService.countExchanges());
    }

    @GetMapping("/exchanges")
    public ResponseEntity<List<ExchangeDTO>> getAllExchanges(){
        return ResponseEntity.ok(exchangeService.getAllExchange());
    }

    @GetMapping("/completed/user/{userId}")
    public ResponseEntity<List<ExchangeDTO>> getCompletedExchangesByUser(@PathVariable Long userId) {
        List<ExchangeDTO> completedExchanges = exchangeService.getCompletedExchangesByUserId(userId);
        return ResponseEntity.ok(completedExchanges);
    }

    @PostMapping("/{exchangeId}/confirm")
    public ResponseEntity<ExchangeDTO> confirmReceived(@PathVariable Long exchangeId) {
        // Obtener el usuario autenticado desde el JWT
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User userDetails = (User) authentication.getPrincipal();
        Long userId = userDetails.getId();

        ExchangeDTO confirmedExchange = exchangeService.confirmReceived(exchangeId, userId);
        return ResponseEntity.ok(confirmedExchange);
    }

    @PostMapping("/{exchangeId}/cancel")
    public ResponseEntity<ExchangeDTO> cancelExchange(@PathVariable Long exchangeId){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User userDetails = (User) authentication.getPrincipal();
        Long userId = userDetails.getId();

        ExchangeDTO canceledExchange = exchangeService.cancelExchange(exchangeId, userId);
        return ResponseEntity.ok(canceledExchange);
    }


}
