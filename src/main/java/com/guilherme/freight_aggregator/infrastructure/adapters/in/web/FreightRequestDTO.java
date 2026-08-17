package com.guilherme.freight_aggregator.infrastructure.adapters.in.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record FreightRequestDTO(
        @NotBlank(message= "CEP de origem é obrigatório") String cepOrigem,
        @NotBlank(message= "CEP de destino é obrigatório") String cepDestino,
        @Positive double larguraCm,
        @Positive double alturaCm,
        @Positive double comprimentoCm,
        @Positive double pesoKg

) {
}
