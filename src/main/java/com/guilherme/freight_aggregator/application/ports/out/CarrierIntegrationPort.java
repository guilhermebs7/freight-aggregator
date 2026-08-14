package com.guilherme.freight_aggregator.application.ports.out;

import com.guilherme.freight_aggregator.domain.model.Address;
import com.guilherme.freight_aggregator.domain.model.FreightOption;
import com.guilherme.freight_aggregator.domain.model.PackageDimension;

import java.util.Optional;

public interface CarrierIntegrationPort {

    Optional<FreightOption> calcularTaxa(Address origem, Address destino, PackageDimension dimensaoPacote);  // calcula a taxa de entrega de acordo com as oções de transportadora

}
