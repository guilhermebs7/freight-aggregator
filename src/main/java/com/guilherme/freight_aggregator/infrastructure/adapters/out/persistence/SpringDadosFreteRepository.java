package com.guilherme.freight_aggregator.infrastructure.adapters.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDadosFreteRepository extends JpaRepository<CotacaoFreteJpaEntity,String> {
}
