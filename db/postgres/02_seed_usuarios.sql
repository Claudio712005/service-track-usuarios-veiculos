SET SEARCH_PATH TO USUARIOS;

INSERT INTO USUARIOS (ID, DOCUMENTO, TIPO_DOCUMENTO, NOME, EMAIL, SENHA_HASH, TIPO_USUARIO, TELEFONE, ATIVO, DATA_CRIACAO, DATA_ATUALIZACAO) VALUES
    ('9f1c6b40-2d58-4a77-9c31-5e8b0a4d1f22', '52998224725', 'CPF', 'Joana Ferreira Lima', 'joana.lima@exemplo.com.br', '$2y$10$h72iZFuuQN18rjwRaP2aU.YAb/OhVEmJCmSNfTXK6dwgaRKPLJR5u', 'CLIENTE', '11988887777', TRUE, '2026-03-10T09:00:00Z', '2026-03-10T09:00:00Z'),
    ('4a7e2d13-88b6-4f09-a5d2-7c1f3b9e6a08', '19028736530', 'CPF', 'Marcos Antunes Silva', 'marcos.silva@exemplo.com.br', '$2y$10$h72iZFuuQN18rjwRaP2aU.YAb/OhVEmJCmSNfTXK6dwgaRKPLJR5u', 'MECANICO', '11977776666', TRUE, '2026-03-10T09:05:00Z', '2026-03-10T09:05:00Z'),
    ('c3b8f571-6e24-4d1a-8b90-2f5a7c0d9e44', '74158290341', 'CPF', 'Renata Mendes Costa', 'renata.costa@exemplo.com.br', '$2y$10$h72iZFuuQN18rjwRaP2aU.YAb/OhVEmJCmSNfTXK6dwgaRKPLJR5u', 'CLIENTE', '11966665555', FALSE, '2026-02-14T16:30:00Z', '2026-07-21T11:12:00Z'),
    ('7d2a4e86-3c19-4b5f-8e07-1d6b9f2a3c50', '39051782659', 'CPF', 'Paulo Henrique Braga', 'paulo.braga@exemplo.com.br', '$2y$10$h72iZFuuQN18rjwRaP2aU.YAb/OhVEmJCmSNfTXK6dwgaRKPLJR5u', 'CLIENTE', '11955554444', TRUE, '2026-04-02T08:40:00Z', '2026-04-02T08:40:00Z'),
    ('2f6c9b13-5a74-4e28-9d61-8b3e0c7a5d92', '47263091534', 'CPF', 'Luciana Prado Esteves', 'luciana.esteves@exemplo.com.br', '$2y$10$h72iZFuuQN18rjwRaP2aU.YAb/OhVEmJCmSNfTXK6dwgaRKPLJR5u', 'CLIENTE', '11944443333', TRUE, '2026-05-19T13:15:00Z', '2026-05-19T13:15:00Z'),
    ('9a3f7c52-8d61-4a09-b7e3-4f0c2d8b6e15', '51820476308', 'CPF', 'Eduardo Nakamura Reis', 'eduardo.reis@exemplo.com.br', '$2y$10$h72iZFuuQN18rjwRaP2aU.YAb/OhVEmJCmSNfTXK6dwgaRKPLJR5u', 'CLIENTE', '11933332222', TRUE, '2026-06-28T10:05:00Z', '2026-06-28T10:05:00Z'),
    ('5b8e1d47-2f93-4c60-8a15-7d9b3e0f4c28', '62938104731', 'CPF', 'Simone Vilela Andrade', 'simone.andrade@exemplo.com.br', '$2y$10$h72iZFuuQN18rjwRaP2aU.YAb/OhVEmJCmSNfTXK6dwgaRKPLJR5u', 'MECANICO', '11922221111', TRUE, '2026-03-11T07:50:00Z', '2026-03-11T07:50:00Z'),
    ('8c4a2f09-7b35-4d81-9e62-3a1f5c8d0b74', '74105293850', 'CPF', 'Ricardo Teixeira Lopes', 'ricardo.lopes@exemplo.com.br', '$2y$10$h72iZFuuQN18rjwRaP2aU.YAb/OhVEmJCmSNfTXK6dwgaRKPLJR5u', 'MECANICO', '11911110000', TRUE, '2026-07-07T09:30:00Z', '2026-07-07T09:30:00Z')
ON CONFLICT (ID) DO NOTHING;

INSERT INTO USUARIO_ROLES (USUARIO_ID, ROLE) VALUES
    ('9f1c6b40-2d58-4a77-9c31-5e8b0a4d1f22', 'CLIENTE'),
    ('4a7e2d13-88b6-4f09-a5d2-7c1f3b9e6a08', 'MECANICO'),
    ('c3b8f571-6e24-4d1a-8b90-2f5a7c0d9e44', 'CLIENTE'),
    ('7d2a4e86-3c19-4b5f-8e07-1d6b9f2a3c50', 'CLIENTE'),
    ('2f6c9b13-5a74-4e28-9d61-8b3e0c7a5d92', 'CLIENTE'),
    ('9a3f7c52-8d61-4a09-b7e3-4f0c2d8b6e15', 'CLIENTE'),
    ('5b8e1d47-2f93-4c60-8a15-7d9b3e0f4c28', 'MECANICO'),
    ('8c4a2f09-7b35-4d81-9e62-3a1f5c8d0b74', 'MECANICO')
ON CONFLICT (USUARIO_ID, ROLE) DO NOTHING;

INSERT INTO VEICULOS (ID, CLIENTE_ID, PLACA, MARCA, MODELO, ANO_MODELO, COR, CHASSI, ATIVO, DATA_CRIACAO, DATA_ATUALIZACAO) VALUES
    ('e18d4a92-7c53-4b06-9f81-3a6c2d5e8b17', '9f1c6b40-2d58-4a77-9c31-5e8b0a4d1f22', 'RDC4F21', 'Volkswagen', 'Polo Highline 1.0 TSI', 2023, 'Prata', '9BWZZZ377VT004251', TRUE, '2026-03-10T09:10:00Z', '2026-03-10T09:10:00Z'),
    ('b5a90c38-1f47-4e2d-8a63-9d0b7f4c1e65', '9f1c6b40-2d58-4a77-9c31-5e8b0a4d1f22', 'GHT2089', 'Fiat', 'Argo Drive 1.3', 2021, 'Branco', '9BD15822AM6782143', TRUE, '2026-05-02T14:25:00Z', '2026-05-02T14:25:00Z'),
    ('3c7f1b58-9d02-4a46-8e71-5b2d4f0a9c63', '7d2a4e86-3c19-4b5f-8e07-1d6b9f2a3c50', 'KQP7D43', 'Chevrolet', 'Onix LT 1.0', 2022, 'Vermelho', '9BGKS48T0NG104772', TRUE, '2026-04-02T08:45:00Z', '2026-04-02T08:45:00Z'),
    ('6e1d8a24-4b70-4c93-9f58-2a7c0e5b3d81', '2f6c9b13-5a74-4e28-9d61-8b3e0c7a5d92', 'RTB1E90', 'Hyundai', 'HB20 Comfort 1.0', 2023, 'Cinza', '9BHBG51CAPP213409', TRUE, '2026-05-19T13:20:00Z', '2026-05-19T13:20:00Z'),
    ('4a0b6f39-7c81-4e52-8d14-9f3a2c6b5e07', '2f6c9b13-5a74-4e28-9d61-8b3e0c7a5d92', 'MXS5G12', 'Toyota', 'Corolla XEi 2.0', 2020, 'Preto', '9BRBL3HE2L0381554', FALSE, '2026-01-23T15:40:00Z', '2026-09-08T10:10:00Z'),
    ('1f9c3d67-5e24-4a08-be71-3c5d9a208f46', '9a3f7c52-8d61-4a09-b7e3-4f0c2d8b6e15', 'JDA2H67', 'Renault', 'Kwid Zen 1.0', 2024, 'Laranja', '93YRBA00MPJ672210', TRUE, '2026-06-28T10:10:00Z', '2026-06-28T10:10:00Z')
ON CONFLICT (ID) DO NOTHING;

COMMENT ON TABLE USUARIOS IS 'Cadastro de pessoas. O seed abre com quatro clientes e tres mecanicos ativos porque sem usuario nao existe login, e nao ha rota publica que crie o primeiro: o servico nao fica atras do gateway (IAC-ADR-035) e o BFF so le. Renata Mendes Costa entra inativa de proposito, para exercitar o filtro de ativos sem desativar quem a demonstracao usa. Joana Ferreira Lima e Marcos Antunes Silva sao os que a colecao Postman referencia por id: nao remover nem trocar o id deles. A senha de todos e a mesma, documentada no README: e credencial de ambiente efemero, nao de producao.';

COMMENT ON TABLE VEICULOS IS 'Veiculos por cliente. Os dois primeiros pertencem a Joana Ferreira Lima, porque abrir ordem de servico exige cliente, mecanico e veiculo, e dois veiculos permitem demonstrar a escolha sem cadastrar nada a mao. Luciana Prado Esteves tem dois, um deles inativo, para que o filtro de ativos tenha caso dentro de um mesmo cliente. O Corolla inativo nao deve ser usado em demonstracao.';
