package com.guilherme.freight_aggregator.domain.service;

import com.guilherme.freight_aggregator.domain.model.FreightOption;

import java.util.Comparator;
import java.util.List;

public class FreightCalculatorDomainService {

    private static final double Porcentagem_Margem_Plataforma=10.0;  //10% de margem sobre a cotação da transportadora

    public List<FreightOption> ProcessarClassificarOpcoes(List<FreightOption> opcoes){
        if (opcoes == null || opcoes.isEmpty()){
            return List.of();

        }

        return opcoes.stream()
                //1. Aplica a taxa da plataforma sobre o valor da transportadora
                .map(option ->option.comMargem(Porcentagem_Margem_Plataforma))

                //2. ordena do mais barato para o mais caro
                .sorted(Comparator.comparingDouble(FreightOption::preco))
                .toList();

    }

}
