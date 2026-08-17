package com.guilherme.freight_aggregator.infrastructure.adapters.in.web;

import java.util.List;

public record FreightReponse(
        String quoteId,
        String cepOrigem,
        String cepDestino,
        List<FreightOptionDTO> opcoes
) {
    public record FreightOptionDTO(
            String transportadoraNome,
            String servicoNome,
            double preco,
            int diaEntrega
    ){}
}
