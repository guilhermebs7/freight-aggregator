package com.guilherme.freight_aggregator.infrastructure.adapters.out.persistence;

import com.guilherme.freight_aggregator.application.ports.out.FreightRepositoryPort;
import com.guilherme.freight_aggregator.domain.model.FreightQuote;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class PostgresFreightRepositoryAdapter implements FreightRepositoryPort {

    private final SpringDadosFreteRepository repository;

    public PostgresFreightRepositoryAdapter(SpringDadosFreteRepository repository) {
        this.repository = repository;
    }

    @Override
    public FreightQuote save(FreightQuote quote) {
        // Mapeia o modelo de Domínio puro para a Entidade ralacional JPA
        CotacaoFreteJpaEntity entity= new CotacaoFreteJpaEntity(
                quote.getId(),
                quote.getOrigem().cep(),
                quote.getDestino().cep(),
                quote.getDimensaoEmbalagem().pesoKg(),
                quote.getOpcao().size(),
                quote.getCriadoEm()
        );
        repository.save(entity);
        return quote;                        // retorna a entidade de domínio
    }

    @Override
    public Optional<FreightQuote> findById(String id) {
        return Optional.empty();
    }
}
