# Finanças em Dia

Aplicativo Android em **Kotlin** para organizar as finanças pessoais.

## Objetivo

O app ajuda a pessoa a controlar o dinheiro do mês. Com ele é possível:

- registrar receitas e despesas, com categoria, data e observação;
- ver o **saldo do mês** (receitas − despesas) e navegar entre os meses;
- filtrar as transações por tipo e abrir os detalhes de cada uma para editar, excluir ou compartilhar;
- ler **dicas financeiras** com passo a passo. As dicas sobre a categoria em que a pessoa mais gastou no mês aparecem primeiro.

Todos os dados ficam salvos no próprio aparelho (Room). O app funciona sem internet.

**Fluxo completo:** ver a lista do mês → registrar uma transação (formulário validado) → abrir os detalhes → editar ou excluir → fechar e reabrir o app com os dados mantidos.

## Como rodar o projeto

### Pré-requisitos
- **Android Studio** recente (Narwhal 2025.1.3 ou mais novo). A JDK 21 que vem com ele já serve.
- Android SDK **API 36** (o Android Studio instala sozinho, se faltar).
- Emulador ou aparelho com **Android 8.0 (API 26)** ou superior.
- Internet apenas para baixar as dependências na primeira compilação.

### Passo a passo
1. Clone o repositório:
   ```bash
   git clone <url-do-repositorio>
   ```
2. No Android Studio, use **File → Open** e escolha a pasta do projeto.
3. Espere o **Gradle Sync** terminar. O wrapper baixa o Gradle 8.14.3 automaticamente.
4. Escolha um emulador ou aparelho e clique em **Run ▶** (configuração `app`).

### Pela linha de comando (opcional)
```bash
./gradlew assembleDebug
```
O APK fica em `app/build/outputs/apk/debug/app-debug.apk`. No Windows, use `gradlew.bat`.

### Testes
```bash
./gradlew testDebugUnitTest
```
```bash
./gradlew connectedDebugAndroidTest
```
O primeiro comando roda os testes unitários na JVM. O segundo roda os testes de interface em Compose e precisa de um emulador ou aparelho conectado.

> **Sem chaves nem segredos.** O app não usa API externa nem senhas, então nenhum arquivo `.env` é necessário. O `.gitignore` ignora `local.properties`, `.env` e keystores por precaução.

## Arquitetura

```
app/src/main/java/br/com/financasemdia/
├── FinancasApplication.kt     # cria o AppContainer
├── di/AppContainer.kt         # injeção manual de dependências
├── domain/                    # regras de negócio puras (sem Android)
│   ├── model/                 # data classes imutáveis: Transaction, Category, MonthlySummary...
│   ├── Money.kt               # conversão texto ⇄ centavos ⇄ R$
│   └── TransactionValidator.kt
├── data/
│   ├── local/                 # Room: entidade, DAO e AppDatabase
│   ├── Mappers.kt             # Entity ⇄ modelo de domínio
│   └── repository/            # interface TransactionRepository + implementação
├── ui/                        # Etapa 2 — Jetpack Compose
│   ├── MainActivity.kt
│   ├── navigation/            # Navigation 3 (rotas + NavDisplay)
│   ├── transactions/ detail/ form/   # telas + ViewModels
│   ├── components/            # componentes reutilizáveis (estados, item, resumo)
│   └── theme/
└── tips/                      # Parcial — Android Views (XML)
    ├── TipsActivity.kt        # lista (RecyclerView)
    ├── TipDetailActivity.kt   # detalhe, hospeda o Fragment
    ├── TipDetailFragment.kt
    └── MockTips.kt            # dados simulados
```

**Padrão MVVM com fluxo unidirecional de dados:**
`Tela (Compose) → evento → ViewModel → Repository → Room` e, no sentido inverso, `Room (Flow) → Repository → ViewModel (StateFlow<UiState>) → Tela`.

- Cada tela tem um `UiState` imutável, exposto como `StateFlow` e coletado com `collectAsStateWithLifecycle()`.
- Os `ViewModel`s sobrevivem à rotação, então o estado da tela é preservado (texto digitado no formulário, mês e filtro selecionados).
- O Room expõe `Flow`s. Qualquer inserção, edição ou exclusão atualiza a lista e o saldo na hora.
- Os valores são guardados em **centavos (`Long`)** para evitar erros de arredondamento de `Double`.
- **Coroutines:** salvar, carregar e excluir rodam no `viewModelScope`, que é cancelado quando a tela sai da pilha. O `flatMapLatest` cancela a consulta do mês anterior quando a pessoa troca de mês. Toques repetidos em "Salvar" ou "Excluir" não disparam operações duplicadas, e os `CancellationException` nunca são engolidos.

## Onde cada requisito é atendido

### Parcial — Android Views (XML), navegação e Intent
| Requisito | Onde |
|---|---|
| Duas telas em XML com Views e ViewGroups | `activity_tips.xml` (LinearLayout, TextView, RecyclerView) e `activity_tip_detail.xml` + `fragment_tip_detail.xml` (FrameLayout, ImageView, Space, NestedScrollView) |
| Navegação por Intent explícita com dados | `TipsActivity.newIntent(...)` recebe a categoria de maior gasto (vinda da tela Compose) e `TipDetailActivity.newIntent(context, tipId)` recebe o id da dica |
| ViewBinding e interação que atualiza a UI | Só ViewBinding, sem `findViewById`. Marcar um passo atualiza o texto e a barra de progresso |
| `data class` imutável e valores opcionais | `FinancialTip` (`readingMinutes: Int?`, `source: String?`) e o extra opcional da Intent (`TipCategory.fromNameOrNull`) |
| Mocks | `MockTips.kt` |
| Opcionais | Componentes XML reutilizáveis (`view_category_tag.xml` via `<include>` e `view_tip_step.xml` inflado dinamicamente) e um `Fragment` com ViewBinding e ciclo de vida (`onDestroyView`, `onSaveInstanceState`) |

### Etapa 2 — Jetpack Compose
| Requisito | Onde |
|---|---|
| Navigation 3 (rotas, entries, back stack, argumentos) | `ui/navigation/AppRoute.kt` e `FinancasNavDisplay.kt` (`TransactionDetail(transactionId)`, `TransactionForm(transactionId?)`) |
| `LazyColumn` | Lista de transações (`TransactionListScreen`) |
| Formulário com validação e feedback | `TransactionFormScreen` + `TransactionValidator`: erro por campo, mensagem geral, botão com progresso e snackbar em caso de falha |
| ViewModel + UiState/StateFlow + `collectAsStateWithLifecycle` + UDF | Todos os `*ViewModel.kt` e `*Route` |
| Estados de carregamento, conteúdo, vazio e erro | `ui/components/StateViews.kt`, usados em `TransactionListContent` e `DetailContent` (incluindo o vazio por filtro) |
| Repository | `TransactionRepository` (interface) + `OfflineTransactionRepository` |
| Operação assíncrona com coroutines e cancelamento | Operações `suspend` no Room dentro do `viewModelScope`, `flatMapLatest` na troca de mês e controle de `Job` em salvar/excluir |
| Persistência local reativa | Room (tabela `transactions`) com `Flow` |
| Acessibilidade | Paleta com contraste ≥ 4.5:1, `contentDescription` nos ícones de ação, `heading()`, `liveRegion` nos estados, erros expostos com `semantics { error() }`, áreas de toque ≥ 48dp, textos em `sp` com layouts roláveis para fontes grandes, item da lista sem depender só da cor (sinal + / − e o rótulo "Receita"/"Despesa") |
| Sem segredos no repositório | Nenhuma chave ou senha no projeto |

**Opcionais da Etapa 2 implementados:** testes automatizados; código seguro (validação e filtragem de entradas, sem logs de dados); funcionamento 100% offline; animações implícitas (`animateItem`, `AnimatedContent`, `AnimatedVisibility`, `animateContentSize`); layout adaptável (largura máxima e conteúdo centralizado em telas grandes ou em paisagem); injeção manual de dependências com interface de Repository e fake nos testes; compartilhamento via Intent (`ACTION_SEND`).

## Testes automatizados
- **Unitários (`app/src/test`)**: `MoneyTest` e `TransactionValidatorTest` (regras de negócio), `TransactionFormViewModelTest` e `TransactionListViewModelTest` (ViewModels com um repository fake).
- **Interface (`app/src/androidTest`)**: `ComposeUiTest` testa componentes reutilizáveis (`EmptyState`, `ErrorState`), o clique em um item da lista e as mensagens de erro do formulário.

## Bibliotecas externas

| Biblioteca | Para que serve no projeto |
|---|---|
| **Jetpack Compose** (BOM 2025.12.00, Material 3) | Toolkit declarativo usado nas telas da Etapa 2 |
| **Material Icons Extended** | Ícones de categorias e ações nas telas Compose |
| **Navigation 3** (`navigation3-runtime`, `navigation3-ui`) | Navegação em Compose baseada em uma back stack controlada pelo app |
| **Lifecycle** (ViewModel, `runtime-compose`, `viewmodel-navigation3`) | ViewModels, `collectAsStateWithLifecycle` e um ViewModel próprio para cada entrada da navegação |
| **Room** (+ KSP) | Banco SQLite local com DAO e consultas reativas via `Flow` |
| **Kotlinx Serialization** | Serializa as rotas do Navigation 3 para salvar e restaurar a pilha de navegação |
| **Kotlin Coroutines** | Operações assíncronas e fluxos (`Flow`/`StateFlow`) |
| **AppCompat**, **Material Components**, **Fragment KTX**, **RecyclerView** | Telas em Android Views (XML) da Parcial |
| **JUnit 4**, **kotlinx-coroutines-test**, **Compose UI Test** | Testes unitários e de interface |
