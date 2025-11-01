package com.ecoswap.ecoswap.exchange.models.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateExchangeRequestDTO {
    private Long productFromId; // ID del producto existente del usuario que hace la solicitud
    private Long productToId;   // ID del producto que quiere intercambiar
}