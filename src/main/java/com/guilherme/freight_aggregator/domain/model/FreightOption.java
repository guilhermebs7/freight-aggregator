package com.guilherme.freight_aggregator.domain.model;

public record FreightOption(
        String nomeTransportadora,
        String nomeServico,
        double preco,
        int diasEntrega
) {
    public FreightOption comMargem(double percentualMargem){
        double novoPreco= this.preco * (1+(percentualMargem / 100.0));
        return new FreightOption(this.nomeTransportadora,this.nomeServico,Math.round(novoPreco * 100.0)/100.0, this.diasEntrega);
    }

}
