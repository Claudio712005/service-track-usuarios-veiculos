SET SEARCH_PATH TO USUARIOS;

INSERT INTO USUARIOS (ID, DOCUMENTO, TIPO_DOCUMENTO, NOME, EMAIL, SENHA_HASH, TIPO_USUARIO, TELEFONE, ATIVO, DATA_CRIACAO, DATA_ATUALIZACAO) VALUES
    ('9f1c6b40-2d58-4a77-9c31-5e8b0a4d1f22', '52998224725', 'CPF', 'Joana Ferreira Lima', 'joana.lima@exemplo.com.br', '$2y$10$h72iZFuuQN18rjwRaP2aU.YAb/OhVEmJCmSNfTXK6dwgaRKPLJR5u', 'CLIENTE', '11988887777', TRUE, '2026-03-10T09:00:00Z', '2026-03-10T09:00:00Z'),
    ('4a7e2d13-88b6-4f09-a5d2-7c1f3b9e6a08', '19028736530', 'CPF', 'Marcos Antunes Silva', 'marcos.silva@exemplo.com.br', '$2y$10$h72iZFuuQN18rjwRaP2aU.YAb/OhVEmJCmSNfTXK6dwgaRKPLJR5u', 'MECANICO', '11977776666', TRUE, '2026-03-10T09:05:00Z', '2026-03-10T09:05:00Z'),
    ('c3b8f571-6e24-4d1a-8b90-2f5a7c0d9e44', '74158290341', 'CPF', 'Renata Mendes Costa', 'renata.costa@exemplo.com.br', '$2y$10$h72iZFuuQN18rjwRaP2aU.YAb/OhVEmJCmSNfTXK6dwgaRKPLJR5u', 'CLIENTE', '11966665555', FALSE, '2026-02-14T16:30:00Z', '2026-07-21T11:12:00Z')
ON CONFLICT (ID) DO NOTHING;

INSERT INTO USUARIO_ROLES (USUARIO_ID, ROLE) VALUES
    ('9f1c6b40-2d58-4a77-9c31-5e8b0a4d1f22', 'CLIENTE'),
    ('4a7e2d13-88b6-4f09-a5d2-7c1f3b9e6a08', 'MECANICO'),
    ('c3b8f571-6e24-4d1a-8b90-2f5a7c0d9e44', 'CLIENTE')
ON CONFLICT (USUARIO_ID, ROLE) DO NOTHING;

INSERT INTO VEICULOS (ID, CLIENTE_ID, PLACA, MARCA, MODELO, ANO_MODELO, COR, CHASSI, ATIVO, DATA_CRIACAO, DATA_ATUALIZACAO) VALUES
    ('e18d4a92-7c53-4b06-9f81-3a6c2d5e8b17', '9f1c6b40-2d58-4a77-9c31-5e8b0a4d1f22', 'RDC4F21', 'Volkswagen', 'Polo Highline 1.0 TSI', 2023, 'Prata', '9BWZZZ377VT004251', TRUE, '2026-03-10T09:10:00Z', '2026-03-10T09:10:00Z'),
    ('b5a90c38-1f47-4e2d-8a63-9d0b7f4c1e65', '9f1c6b40-2d58-4a77-9c31-5e8b0a4d1f22', 'GHT2089', 'Fiat', 'Argo Drive 1.3', 2021, 'Branco', '9BD15822AM6782143', TRUE, '2026-05-02T14:25:00Z', '2026-05-02T14:25:00Z')
ON CONFLICT (ID) DO NOTHING;

COMMENT ON TABLE USUARIOS IS 'Cadastro de pessoas. O seed abre com um cliente e um mecanico ativos porque sem usuario nao existe login, e nao ha rota publica que crie o primeiro: o servico nao fica atras do gateway (IAC-ADR-035) e o BFF so le. Renata Mendes Costa entra inativa de proposito, para exercitar o filtro de ativos sem desativar quem a demonstracao usa. A senha das tres e a mesma, documentada no README: e credencial de ambiente efemero, nao de producao.';

COMMENT ON TABLE VEICULOS IS 'Veiculos por cliente. Os dois pertencem a Joana Ferreira Lima, porque abrir ordem de servico exige cliente, mecanico e veiculo, e dois veiculos permitem demonstrar a escolha sem cadastrar nada a mao.';
