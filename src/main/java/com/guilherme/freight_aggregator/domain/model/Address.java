package com.guilherme.freight_aggregator.domain.model;

public record Address(String cep, String rua, String cidade, String estado) {

    public Address{
        if (cep == null || cep.matches("\\d{5}-?\\d{3}")){
            throw new IllegalArgumentException("CEP inválido. Deve conter 8 dígitos.");

        }
    }

}
