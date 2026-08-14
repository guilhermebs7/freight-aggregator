package com.guilherme.freight_aggregator.application.ports.in;

public record CalculateFreightCommand(
        String cepOrigem,
        String CepDestino,
        double larguraCm,
        double comprimentoCm,
        double pesoKg
) {
}
