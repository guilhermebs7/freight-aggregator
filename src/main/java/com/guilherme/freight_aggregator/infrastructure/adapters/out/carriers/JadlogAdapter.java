package com.guilherme.freight_aggregator.infrastructure.adapters.out.carriers;

import com.guilherme.freight_aggregator.application.ports.out.CarrierIntegrationPort;
import com.guilherme.freight_aggregator.domain.model.Address;
import com.guilherme.freight_aggregator.domain.model.FreightOption;
import com.guilherme.freight_aggregator.domain.model.PackageDimension;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class JadlogAdapter implements CarrierIntegrationPort {
    @Override
    public Optional<FreightOption> calcularTaxa(Address origem, Address destino, PackageDimension dimensaoPacote) {

        try{
            double preco= 15.0 +(dimensaoPacote.obterPesoEfetivo() * 6.0);
            int diaEntrega=2;

            return Optional.of(new FreightOption("Jadlog","Package Express",preco,diaEntrega));
        }catch (Exception e){
            return Optional.empty();
        }

    }
}
