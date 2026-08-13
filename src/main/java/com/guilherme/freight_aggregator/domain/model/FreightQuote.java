package com.guilherme.freight_aggregator.domain.model;

import java.time.LocalDateTime;
import java.util.List;

public class FreightQuote {
    private final String id;
    private final Address origem;
    private final Address destino;
    private final PackageDimension dimensaoEmbalagem;
    private final List<FreightOption> opcao;
    private final LocalDateTime criadoEm;

    public FreightQuote(String id, Address origem, Address destino, PackageDimension dimensaoEmbalagem, List<FreightOption> opcao, LocalDateTime criadoEm) {
        this.id = id;
        this.origem = origem;
        this.destino = destino;
        this.dimensaoEmbalagem = dimensaoEmbalagem;
        this.opcao = opcao;
        this.criadoEm = criadoEm;
    }

    public String getId() {
        return id;
    }

    public Address getOrigem() {
        return origem;
    }

    public Address getDestino() {
        return destino;
    }

    public PackageDimension getDimensaoEmbalagem() {
        return dimensaoEmbalagem;
    }

    public List<FreightOption> getOpcao() {
        return opcao;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }
}

