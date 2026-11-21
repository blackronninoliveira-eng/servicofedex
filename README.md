# ServicoFedex

Aplicativo Android para controle diário de ordens de serviço, suprimentos,
quilometragem e ganhos. Interface simples, 100% offline, com build automatizado
via GitHub Actions.

> O enunciado menciona "4 telas", porém descreve **5** (Home, Registro,
> Histórico, Ganhos e Configurações). Todas as cinco foram implementadas.

---

## Funcionalidades

### 1. Home (dashboard)
- Quantidade de ordens **feitas**, **canceladas** e de **suprimentos** no dia,
  na semana vigente e no mês vigente.
- Valor a receber no dia, na semana e o total acumulativo do mês.
- KM rodados no dia/semana/mês e consumo estimado (quando a autonomia é informada).

### 2. Registro
- Campos: data (`AAAA-MM-DD`), ordens feitas, canceladas, suprimentos, KM inicial e KM final.
- Prévia em tempo real com **total de ordens** (feitas + suprimentos) e **valor a receber**.
- Salvar, limpar e excluir o registro da data. Validações de data, números e KM.

### 3. Histórico
- Lista de todas as entradas com filtro: todos / hoje / semana / mês.
- Totalizadores de ordens, valor e KM por filtro.
- Exportação em **PDF** com detalhamento por **dia, semana ou mês**, com seletor
  de período e opção de **incluir ou não o KM rodado**.
- Opções de **visualizar** e **compartilhar** o PDF gerado (FileProvider).
- Exclusão de registros diretamente na lista.

### 4. Ganhos
- Valores em reais do dia, da semana e do mês calculados automaticamente.
- Barras comparativas e memória de cálculo (valor unitário de serviço e suprimento).

### 5. Configurações
- Valor de cada ordem de serviço (padrão R$ 16,00) e de cada suprimento (padrão R$ 5,00).
- Nome do veículo e autonomia (km/l) — usada nas estimativas de consumo.
- Nome e foto do usuário (a foto é copiada para o armazenamento interno do app).
- Tema **escuro** ou **claro** (escolha do usuário, sem depender do tema do sistema).
- Limpar todo o histórico com confirmação.

---

## Regras de cálculo

| Item | Regra |
| --- | --- |
| Ordens pagas | `serviços feitos + suprimentos feitos` |
| Valor a receber | `serviços feitos × valor da ordem + suprimentos × valor do suprimento` |
| Canceladas | Entram apenas na contagem, **não** pagam |
| KM rodado | `KM final - KM inicial` (nunca negativo) |
| Consumo estimado | `KM rodado ÷ autonomia` (somente se autonomia > 0) |

Semana = segunda-feira a domingo. Mês = dia 1 até o último dia do mês corrente.

---

## Paleta de cores (obrigatória e exclusiva)

| Cor | Hex | Uso principal |
| --- | --- | --- |
| Amarelo | `#F8DD1B` | Destaques, primary no tema escuro |
| Amarelo escuro | `#F5CE0D` | Secondary, fundo do tema claro |
| Preto-azulado | `#121519` | Fundo do tema escuro e texto sobre amarelo |
| Cinza escuro | `#2E3137` | Superfícies/cartões |

Todos os papéis do `ColorScheme` do Material 3 são preenchidos explicitamente
com essas quatro cores, inclusive nos PDFs, ícones e barras do sistema. Não há
branco ou preto puros, nem cores do tema padrão do Android.

---

## Arquitetura e tecnologias

- **Kotlin 2.0.21** + **Jetpack Compose** (Compose BOM 2024.12.01 / Material 3).
- **AGP 8.6.1**, Gradle **8.10.2** (wrapper versionado), JDK **17**.
- `compileSdk`/`targetSdk` 35, `minSdk` 26.
- `MainViewModel` + `StateFlow` para estado da UI.
- Persistência local via `SharedPreferences` + `org.json` (sem banco externo,
  sem dependências extras e sem necessidade de internet).
- PDF gerado com `android.graphics.pdf.PdfDocument` (API nativa, sem bibliotecas).
- Navegação por barra inferior com 5 abas.
- Testes unitários JUnit 4 dos cálculos e agrupamentos de período.

### Estrutura principal

```
app/src/main/java/com/servicofedex/app/
├── MainActivity.kt            # Scaffold, navegação e barras do sistema
├── MainViewModel.kt           # Estado e ações
├── data/
│   ├── Models.kt              # DailyRecord, AppSettings, Totals, PeriodOption
│   └── PreferencesRepository.kt
├── ui/
│   ├── HomeScreen.kt
│   ├── RegisterScreen.kt
│   ├── HistoryScreen.kt
│   ├── EarningsScreen.kt
│   ├── SettingsScreen.kt
│   ├── components/Common.kt
│   └── theme/{Color,Theme,Type}.kt
└── util/{DateUtils,StatsCalculator,PdfExporter,PdfShare}.kt
```

---

## Build

O projeto está pronto para gerar o APK no GitHub Actions. Veja o guia completo:

**[BUILD_GITHUB_ACTIONS.md](BUILD_GITHUB_ACTIONS.md)**

Resumo: faça `git push` para `main`/`master` (ou dispare manualmente em
**Actions → Build Android APK (ServicoFedex) → Run workflow**) e baixe o artefato
`servicofedex-debug-apk` na página da execução.

Build local (opcional):

```bash
export JAVA_HOME=/caminho/jdk-17
export ANDROID_HOME=/caminho/android-sdk
./gradlew testDebugUnitTest assembleDebug
```

---

## Observações

- Os dados ficam apenas no dispositivo (`SharedPreferences` e arquivo da foto).
- O PDF é salvo no cache do app e compartilhado via `FileProvider`.
- O APK de release gerado pelo CI não é assinado; o guia de build ensina como
  configurar assinatura se desejar publicar.
