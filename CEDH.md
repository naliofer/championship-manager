# cEDH Multiplayer

Android: selecionar **cEDH Multiplayer** no seletor de modalidades. PWA: selecionar a modalidade no cabeçalho. Duplas mantém seu motor, telas e snapshot originais.

1. Cadastrar jogadores, comandantes e decks. Moxfield é opcional. Usar um múltiplo de quatro jogadores; comandantes iguais são permitidos.
2. Configurar rodadas, pontos, tempo regulamentar e adicional, empate por acordo e Top 4. A final tem tempos próprios e exige vencedor; não é encerrada automaticamente pelo relógio.
3. Em Mesas, gerar a prévia e confirmar. O cadastro e as configurações ficam bloqueados após a primeira rodada.
4. Registrar vitória, empate por tempo/acordo ou resultado administrativo. Eliminações, causas, duração, observações e ajustes administrativos de pontos são registrados por mesa.
5. Todos os resultados da rodada devem estar registrados antes do próximo sorteio. Correções substituem o resultado e recalculam os totais, preservando confrontos anteriores.
6. Sem Top 4, o primeiro colocado ao concluir as rodadas é campeão. Com Top 4, a mesa final seleciona os quatro primeiros e seu vencedor é campeão. A classificação exibida continua sendo a classificatória.

## Regras e persistência

- Prioridades do otimizador: validade e unicidade obrigatórias; depois diferença de pontos, adversários/composição repetidos e posições. Busca local com múltiplos reinícios, sem garantia matemática de ótimo global. Primeira rodada usa embaralhamento uniforme. As posições são a ordem dos quatro assentos.
- Desempates cEDH: pontos, vitórias, OMW%. OMW é a média de vitórias/partidas dos adversários únicos efetivamente enfrentados nas classificatórias, sem piso percentual. ID estável resolve igualdade completa para produzir uma ordem determinística.
- Empates pontuam os quatro jogadores, incluindo eliminados. Ajustes administrativos são somados à pontuação calculada; eliminações nunca determinam vitória.
- Android: snapshot JSON versionado `cedh_current`, arquivos históricos `cedh_archive_<id>`, no SharedPreferences existente. PWA: registros equivalentes no IndexedDB existente, sem substituir `current` de duplas. Não há migração destrutiva nem mudança no formato legado.
- Pontuação é derivada dos resultados, sem contadores incrementais. O relógio persiste milissegundos acumulados e horário de retomada. Pausas congelam a contagem; fechar o app durante execução não congela o tempo. Alterar o relógio do aparelho afeta cronômetros baseados em horário civil.
- Novo campeonato arquiva o anterior. A lista de arquivados permite reabri-lo. A PWA também exporta/restaura JSON.
- O shell offline inclui ambos os motores e interfaces. IndexedDB e SharedPreferences são locais ao aparelho; não há sincronização entre plataformas.

## Validação

Android: `./gradlew.bat testDebugUnitTest assembleDebug`. PWA, em `web`: `npm ci`, `npm test`, `npm run build`.

Os testes cobrem motores, codecs, repositórios, isolamento de duplas, correção, cronômetro e fluxos da PWA com DOM + IndexedDB. Testes com 8 e 12 participantes incluem reabertura, rodadas seguintes e final. Os motores Kotlin e JavaScript mantêm a mesma política, mas são implementações separadas devido aos runtimes atuais.

Validação visual em Android físico/emulador e Safari/iPhone deve ser feita em aparelhos disponíveis; os testes de DOM não substituem essa checagem.
