package com.guilherme.freight_aggregator.infrastructure.config;


import com.guilherme.freight_aggregator.application.ports.out.CarrierIntegrationPort;
import com.guilherme.freight_aggregator.application.ports.out.FreightRepositoryPort;
import com.guilherme.freight_aggregator.application.usecases.CalculateFreightService;
import com.guilherme.freight_aggregator.domain.service.FreightCalculatorDomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class BeanConfiguration {

    @Bean
    public FreightCalculatorDomainService ServicoDominioCalculoFrete(){
        return new FreightCalculatorDomainService();
    }

    @Bean
    public CalculateFreightService calculaFreteDominio(
            List<CarrierIntegrationPort> transportadoraPorts,
            FreightRepositoryPort repositoryPort,
            FreightCalculatorDomainService domainService
    ){
        return new CalculateFreightService(transportadoraPorts,repositoryPort,domainService);   // o spring injeta automaticamente todos os Beans de adaptadores que implementam CarrierIntegrationPort e FreightRepositoryPort
    }
}
