package com.guilherme.freight_aggregator.infrastructure.adapters.out.persistence;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "cotacao_frete")
public class CotacaoFreteJpaEntity {

    @Id
    private String id;
    private String cepOrigem;
    private String cepDestino;
    private double pesoKg;
    private int opcoesTotal;
    private LocalDateTime criadoEm;

    public CotacaoFreteJpaEntity() {
    }

    public CotacaoFreteJpaEntity(String id, String cepOrigem, String cepDestino, double pesoKg, int opcoesTotal, LocalDateTime criadoEm) {
        this.id = id;
        this.cepOrigem = cepOrigem;
        this.cepDestino = cepDestino;
        this.pesoKg = pesoKg;
        this.opcoesTotal = opcoesTotal;
        this.criadoEm = criadoEm;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCepOrigem() {
        return cepOrigem;
    }

    public void setCepOrigem(String cepOrigem) {
        this.cepOrigem = cepOrigem;
    }

    public String getCepDestino() {
        return cepDestino;
    }

    public void setCepDestino(String cepDestino) {
        this.cepDestino = cepDestino;
    }

    public double getPesoKg() {
        return pesoKg;
    }

    public void setPesoKg(double pesoKg) {
        this.pesoKg = pesoKg;
    }

    public int getOpcoesTotal() {
        return opcoesTotal;
    }

    public void setOpcoesTotal(int opcoesTotal) {
        this.opcoesTotal = opcoesTotal;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }
}
