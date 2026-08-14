package com.guilherme.freight_aggregator.application.usecases;

import com.guilherme.freight_aggregator.application.ports.in.CalculateFreightCommand;
import com.guilherme.freight_aggregator.application.ports.in.CalculateFreightUseCase;
import com.guilherme.freight_aggregator.application.ports.out.CarrierIntegrationPort;
import com.guilherme.freight_aggregator.application.ports.out.FreightRepositoryPort;
import com.guilherme.freight_aggregator.domain.model.Address;
import com.guilherme.freight_aggregator.domain.model.FreightOption;
import com.guilherme.freight_aggregator.domain.model.FreightQuote;
import com.guilherme.freight_aggregator.domain.model.PackageDimension;
import com.guilherme.freight_aggregator.domain.service.FreightCalculatorDomainService;

import java.util.List;
import java.util.Optional;

public class CalculateFreightService implements CalculateFreightUseCase {

    private final List<CarrierIntegrationPort>  portasOperadora;
    private final FreightRepositoryPort  portaRepositorio;
    private final FreightCalculatorDomainService servicoDominio;

    public CalculateFreightService(List<CarrierIntegrationPort> portasOperadora, FreightRepositoryPort portaRepositorio, FreightCalculatorDomainService servicoDominio) {
        this.portasOperadora = portasOperadora;
        this.portaRepositorio = portaRepositorio;
        this.servicoDominio = servicoDominio;
    }


    @Override
    public FreightQuote calculate(CalculateFreightCommand command) {
        Address origem = new Address(command.cepOrigem(),null,null,null);     // converte DTO do Comando em Objetos de valor do Domínio
        Address destino = new Address(command.CepDestino(),null,null,null);
        PackageDimension dimensao= new PackageDimension(command.larguraCm(),command.alturaCm(),
                command.comprimentoCm(), command.pesoKg());

        List<FreightOption> opcoes= portasOperadora.stream()           //chama TODAS as transportadoras em paralelo/ lista de portas de saída
                .map(operadora-> operadora.calcularTaxa(origem,destino,dimensao))
                .flatMap(Optional::stream)             //filtra apenas as que responderam com sucesso
                .toList();


        List<FreightOption>  opcoesProcessadas= servicoDominio.ProcessarClassificarOpcoes(opcoes);

        FreightQuote quote = new FreightQuote(origem,destino,dimensao,opcoesProcessadas);   //Cria a entidade do Agregado de Cotação

        return portaRepositorio.save(quote);




        }
    }

