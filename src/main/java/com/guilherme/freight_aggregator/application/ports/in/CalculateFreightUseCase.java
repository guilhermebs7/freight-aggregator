package com.guilherme.freight_aggregator.application.ports.in;

import com.guilherme.freight_aggregator.domain.model.FreightQuote;

public interface CalculateFreightUseCase {
    FreightQuote calculate(CalculateFreightCommand command);
}
