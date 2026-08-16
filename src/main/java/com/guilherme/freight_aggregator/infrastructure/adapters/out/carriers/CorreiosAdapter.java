package com.guilherme.freight_aggregator.infrastructure.adapters.out.carriers;

import com.guilherme.freight_aggregator.application.ports.out.CarrierIntegrationPort;
import com.guilherme.freight_aggregator.domain.model.Address;
import com.guilherme.freight_aggregator.domain.model.FreightOption;
import com.guilherme.freight_aggregator.domain.model.PackageDimension;

import java.util.Optional;

public class CorreiosAdapter implements CarrierIntegrationPort {
    @Override
    public Optional<FreightOption> calcularTaxa(Address origem, Address destino, PackageDimension dimensaoPacote) {
        try {
            // aqui seria feita a chamada HTTP para API dos Correios.
            // Simulação de cálculo baseado no peso efeitivo
            double preco= 20.0 + (dimensaoPacote.obterPesoEfetivo()* 4.5);
            int diaEntrega=5;

             return Optional.of(new FreightOption("Correios", "SEDEX",preco,diaEntrega));

        }catch (Exception e){

            //em caso de falha na API do parceiro, retornamos Optional.empty() sem quebrar o sistema
            return Optional.empty();
        }
    }
}
