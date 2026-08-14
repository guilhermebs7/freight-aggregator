package com.guilherme.freight_aggregator.application.ports.out;

import com.guilherme.freight_aggregator.domain.model.FreightQuote;

import java.util.Optional;                    //define o que precisamos salvar e buscar no banco de dados , sem saber  se estamos usando PostgreSQL, MongoDB ...

public interface FreightRepositoryPort {
    FreightQuote save(FreightQuote quote);
    Optional<FreightQuote> findById(String id);
}
