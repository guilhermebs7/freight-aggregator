package com.guilherme.freight_aggregator.infrastructure.adapters.in.web;

import com.guilherme.freight_aggregator.application.ports.in.CalculateFreightCommand;
import com.guilherme.freight_aggregator.application.ports.in.CalculateFreightUseCase;
import com.guilherme.freight_aggregator.domain.model.FreightQuote;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/freights")
public class FreightController {

    private final CalculateFreightUseCase calculateFreightUseCase;


    public FreightController(CalculateFreightUseCase calculateFreightUseCase) {
        this.calculateFreightUseCase = calculateFreightUseCase;
    }

    @PostMapping("/calculate")
    public ResponseEntity<FreightReponse> calcularFrete(@Valid @RequestBody FreightRequestDTO request){
        //converte DTO HTTP para o command do Caso de Uso
        CalculateFreightCommand command= new CalculateFreightCommand(
                request.cepOrigem(),
                request.cepDestino(),
                request.larguraCm(),
                request.alturaCm(),
                request.comprimentoCm(),
                request.pesoKg()
        );

        FreightQuote quote= calculateFreightUseCase.calculate(command);  // executa o Caso de uso

        FreightReponse response= new FreightReponse(
                quote.getId(),
                quote.getOrigem().cep(),
                quote.getDestino().cep(),
                quote.getOpcao().stream()
                        .map(opt -> new FreightReponse.FreightOptionDTO(opt.nomeTransportadora(), opt.nomeServico(), opt.preco(), opt.diasEntrega())).toList()
        );
        return ResponseEntity.ok(response);
    }
}
