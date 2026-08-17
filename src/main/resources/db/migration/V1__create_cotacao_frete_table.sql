create table cotacao_frete(
                              id varchar(36) NOT NULL ,
                              cep_origem varchar(10) NOT NULL ,
                              cep_destino varchar(10) NOT NULL ,
                              peso_kg DOUBLE PRECISION NOT NULL ,
                              opcoes_total INT NOT NULL ,
                              criado_em TIMESTAMP NOT NULL ,


                              CONSTRAINT pk_cotacao_frete PRIMARY KEY (id)

)