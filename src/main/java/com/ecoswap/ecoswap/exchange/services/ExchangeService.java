package com.ecoswap.ecoswap.exchange.services;

import com.ecoswap.ecoswap.exchange.models.dto.CreateExchangeRequestDTO;
import com.ecoswap.ecoswap.exchange.models.dto.ExchangeDTO;
import com.ecoswap.ecoswap.product.models.dto.ProductDTO;
import com.ecoswap.ecoswap.user.models.dto.UserDTO;

import java.util.List;

public interface ExchangeService {
    ExchangeDTO createRequestExchange(ExchangeDTO requestExchange);
    ExchangeDTO createRequestExchangeWithExistingProduct(CreateExchangeRequestDTO request);
    ExchangeDTO selectExchangeRequest(ExchangeDTO requestExchange);
    List<ExchangeDTO> findByProductTo(ProductDTO productDTO);
    Long countExchanges();
    List<ExchangeDTO> getAllExchange();
    List<ExchangeDTO> getCompletedExchangesByUserId(Long userId);
    ExchangeDTO confirmReceived(Long exchangeId, Long userId);
    ExchangeDTO cancelExchange(Long exchangeId, Long userId);
}
