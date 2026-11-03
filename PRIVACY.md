# Política de Privacidade — Smart Nav Pill

Última atualização: 2026

## Resumo

O **Smart Nav Pill não coleta, não armazena e não envia nenhum dado pessoal.**
Tudo funciona 100% no seu aparelho, offline.

## O que o app acessa

- **Serviço de acessibilidade:** usado apenas para posicionar uma camada
  invisível sobre a barra de navegação e detectar o toque duplo / toque longo
  nessa região. O app **não lê o conteúdo das suas telas**
  (`canRetrieveWindowContent = false`).
- **Lista de apps instalados:** usada somente para você escolher, dentro do app,
  qual aplicativo cada gesto abre. Essa lista não sai do aparelho.
- **Preferências locais:** suas escolhas (apps selecionados, tamanho da faixa,
  etc.) ficam salvas localmente no próprio aparelho (DataStore).

## O que o app NÃO faz

- Não usa internet para enviar dados.
- Não coleta dados de uso, analytics ou telemetria.
- Não acessa contatos, localização, câmera, microfone, fotos ou mensagens.
- Não compartilha nada com terceiros.

## Permissões declaradas

- `QUERY_ALL_PACKAGES` — para listar os apps instalados que você pode escolher.
- `VIBRATE` — para o feedback tátil opcional ao acionar um gesto.
- `BIND_ACCESSIBILITY_SERVICE` — concedida pelo sistema ao vincular o serviço de
  acessibilidade.

## Contato

Dúvidas sobre privacidade? Abra uma issue no repositório do projeto.
