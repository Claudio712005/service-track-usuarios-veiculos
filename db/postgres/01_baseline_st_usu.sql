CREATE SCHEMA IF NOT EXISTS USUARIOS;

SET SEARCH_PATH TO USUARIOS;

CREATE TABLE IF NOT EXISTS USUARIOS (
    ID               UUID         NOT NULL,
    DOCUMENTO        VARCHAR(14)  NOT NULL,
    TIPO_DOCUMENTO   VARCHAR(4)   NOT NULL,
    NOME             VARCHAR(120) NOT NULL,
    EMAIL            VARCHAR(255) NOT NULL,
    SENHA_HASH       VARCHAR(72)  NOT NULL,
    TIPO_USUARIO     VARCHAR(10)  NOT NULL,
    TELEFONE         VARCHAR(20),
    ATIVO            BOOLEAN      NOT NULL DEFAULT TRUE,
    DATA_CRIACAO     TIMESTAMPTZ(6) NOT NULL,
    DATA_ATUALIZACAO TIMESTAMPTZ(6) NOT NULL,
    PRIMARY KEY (ID),
    CONSTRAINT UQ_USUARIOS_DOCUMENTO UNIQUE (DOCUMENTO),
    CONSTRAINT UQ_USUARIOS_EMAIL UNIQUE (EMAIL),
    CONSTRAINT CK_USUARIOS_TIPO_DOCUMENTO CHECK (TIPO_DOCUMENTO IN ('CPF', 'CNPJ')),
    CONSTRAINT CK_USUARIOS_DOCUMENTO_FORMATO CHECK (DOCUMENTO ~ '^([0-9]{11}|[A-Z0-9]{12}[0-9]{2})$'),
    CONSTRAINT CK_USUARIOS_TIPO_USUARIO CHECK (TIPO_USUARIO IN ('MECANICO', 'CLIENTE')),
    CONSTRAINT CK_USUARIOS_MECANICO_E_PESSOA_FISICA CHECK (TIPO_USUARIO <> 'MECANICO' OR TIPO_DOCUMENTO = 'CPF')
);

CREATE TABLE IF NOT EXISTS USUARIO_ROLES (
    USUARIO_ID UUID        NOT NULL,
    ROLE       VARCHAR(20) NOT NULL,
    PRIMARY KEY (USUARIO_ID, ROLE),
    CONSTRAINT FK_ROLES_USUARIO FOREIGN KEY (USUARIO_ID) REFERENCES USUARIOS (ID) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS VEICULOS (
    ID               UUID         NOT NULL,
    CLIENTE_ID       UUID         NOT NULL,
    PLACA            VARCHAR(7)   NOT NULL,
    MARCA            VARCHAR(40)  NOT NULL,
    MODELO           VARCHAR(60)  NOT NULL,
    ANO_MODELO       INTEGER      NOT NULL,
    COR              VARCHAR(30),
    CHASSI           VARCHAR(17),
    ATIVO            BOOLEAN      NOT NULL DEFAULT TRUE,
    DATA_CRIACAO     TIMESTAMPTZ(6) NOT NULL,
    DATA_ATUALIZACAO TIMESTAMPTZ(6) NOT NULL,
    PRIMARY KEY (ID),
    CONSTRAINT UQ_VEICULOS_PLACA UNIQUE (PLACA),
    CONSTRAINT FK_VEICULOS_CLIENTE FOREIGN KEY (CLIENTE_ID) REFERENCES USUARIOS (ID),
    CONSTRAINT CK_VEICULOS_PLACA_FORMATO CHECK (PLACA ~ '^([A-Z]{3}[0-9]{4}|[A-Z]{3}[0-9][A-Z][0-9]{2})$'),
    CONSTRAINT CK_VEICULOS_CHASSI_FORMATO CHECK (CHASSI IS NULL OR CHASSI ~ '^[A-HJ-NPR-Z0-9]{17}$'),
    CONSTRAINT CK_VEICULOS_ANO CHECK (ANO_MODELO BETWEEN 1900 AND 2100)
);

CREATE INDEX IF NOT EXISTS IX_VEICULOS_DO_CLIENTE ON VEICULOS (CLIENTE_ID, PLACA) WHERE ATIVO;

CREATE INDEX IF NOT EXISTS IX_USUARIOS_ATIVOS ON USUARIOS (TIPO_USUARIO, NOME) WHERE ATIVO;

COMMENT ON TABLE USUARIOS IS 'Mecanicos e clientes da oficina. A mesma tabela serve aos dois porque ambos entram pelo mesmo caminho de credencial; o que difere e o TIPO_USUARIO e o que cada um pode fazer.';
COMMENT ON COLUMN USUARIOS.DOCUMENTO IS 'CPF (11 digitos) ou CNPJ (14 posicoes). Desde julho de 2026 o CNPJ aceita letra nas 12 primeiras posicoes; os dois digitos verificadores seguem numericos. Sem pontuacao. E a chave de login: a Fase 3 exige autenticacao por documento, nao por e-mail.';
COMMENT ON COLUMN USUARIOS.TIPO_DOCUMENTO IS 'Derivado do tamanho: 11 caracteres e CPF, 14 e CNPJ.';
COMMENT ON COLUMN USUARIOS.SENHA_HASH IS 'Hash bcrypt com custo 10. Senha em texto puro nunca e gravada nem registrada em log.';
COMMENT ON COLUMN USUARIOS.ATIVO IS 'Falso impede autenticacao e retira da listagem padrao. Nao se apaga usuario: a ordem de servico historica aponta para ele.';
COMMENT ON CONSTRAINT CK_USUARIOS_DOCUMENTO_FORMATO ON USUARIOS IS 'Barra no banco o que a aplicacao ja barra: CPF so digitos, e CNPJ com base alfanumerica e verificadores numericos.';
COMMENT ON CONSTRAINT CK_USUARIOS_MECANICO_E_PESSOA_FISICA ON USUARIOS IS 'Mecanico e pessoa fisica. A aplicacao ja recusa, e o banco recusa tambem: regra que importa mora nos dois lugares.';
COMMENT ON TABLE USUARIO_ROLES IS 'Papeis do usuario, lidos pela Lambda para montar o claim do token.';
COMMENT ON INDEX IX_USUARIOS_ATIVOS IS 'A listagem padrao filtra por ativo e ordena por nome; indice parcial nao cresce com o historico desativado.';
COMMENT ON TABLE VEICULOS IS 'Frota dos clientes. O veiculo e do cliente, nao da oficina: por isso CLIENTE_ID e obrigatorio e aponta para USUARIOS.';
COMMENT ON COLUMN VEICULOS.PLACA IS 'Sete posicoes, sem hifen: formato antigo ABC1234 ou Mercosul ABC1D23. Unica no servico.';
COMMENT ON COLUMN VEICULOS.CHASSI IS 'Dezessete posicoes. A norma nao usa I, O e Q, para nao confundir com 1 e 0.';
COMMENT ON COLUMN VEICULOS.ATIVO IS 'Falso retira da listagem padrao. Nao se apaga veiculo: a ordem de servico historica aponta para ele.';
COMMENT ON CONSTRAINT FK_VEICULOS_CLIENTE ON VEICULOS IS 'Sem ON DELETE CASCADE de proposito: usuario nao e apagado, e desativado.';
COMMENT ON INDEX IX_VEICULOS_DO_CLIENTE IS 'A consulta mais comum e a frota de um cliente; indice parcial nao cresce com o historico desativado.';
