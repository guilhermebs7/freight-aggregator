package com.guilherme.freight_aggregator.domain.model;

public record PackageDimension(double larguraCm,double alturaCm, double comprimentoCm,double pesoKg) {

    public PackageDimension{
        if (larguraCm <=0 || alturaCm <=0 || comprimentoCm <=0 || pesoKg <=0){
            throw new IllegalArgumentException("Dimensões e peso devem ser maiores que zero");
        }
    }

    public double obterPesoCubico(){                                      // Cálculo do peso cubado( Fator de cubagem padrão: 6000)
        return (larguraCm * alturaCm * comprimentoCm) / 6000.0;
    }

    public double obterPesoEfetivo(){
        return Math.max(pesoKg, obterPesoCubico());
    }
}
