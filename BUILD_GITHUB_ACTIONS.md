# Como gerar o APK do ServicoFedex com GitHub Actions

Este documento explica, passo a passo, como colocar o projeto no GitHub e fazer a
build automática na nuvem. **Você não precisa ter Java, Android Studio ou SDK
instalado no seu computador** — o GitHub Actions instala tudo.

---

## 1. O que já está pronto no projeto

| Arquivo | Função |
| --- | --- |
| `.github/workflows/build.yml` | Define o job de build: checkout → JDK 17 → Android SDK → testes → APKs → artefatos |
| `gradle/wrapper/gradle-wrapper.jar` e `gradle-wrapper.properties` | Gradle 8.10.2 fixado; garante a mesma versão no seu PC e no GitHub |
| `gradlew` / `gradlew.bat` | Script de execução do Gradle (não é preciso instalar Gradle) |
| `build.gradle.kts` (raiz) | AGP 8.6.1, Kotlin 2.0.21 e plugin Compose 2.0.21 fixados |
| `app/build.gradle.kts` | compileSdk 35, minSdk 26, targetSdk 35, Compose BOM 2024.12.01 |
| `local.properties` | **Não versionado** (`.gitignore`); no GitHub o SDK vem da variável `ANDROID_HOME` |

O workflow usa `android-actions/setup-android@v3`, que instala o SDK e aceita as
licenças automaticamente. Por isso não é necessário enviar `local.properties`.

---

## 2. Criar o repositório no GitHub

1. Acesse <https://github.com> e faça login.
2. Clique em **New repository**.
3. Nome sugerido: `servicofedex`.
4. Deixe **Public** ou **Private** (ambos funcionam com Actions; em plano free
   privado há limite de minutos, suficiente para este projeto).
5. **Não** marque "Add a README file", ".gitignore" nem "License" — o projeto já
   possui esses arquivos e isso evita conflito.
6. Clique em **Create repository** e copie a URL que aparece (ex.:
   `https://github.com/SEU-USUARIO/servicofedex.git`).

---

## 3. Enviar o código para o GitHub

Abra um terminal na pasta onde está o projeto (`servicofedex`) e execute:

```bash
cd servicofedex

git init -b main
git add .
git commit -m "ServicoFedex: app Android com 5 telas e build via GitHub Actions"
git remote add origin https://github.com/SEU-USUARIO/servicofedex.git
git push -u origin main
```

> Se o Git pedir usuário/senha, use um **Personal Access Token** (GitHub →
> Settings → Developer settings → Personal access tokens) ou configure o Git
> Credential Manager. Senha de conta não é aceita mais.

Se você já tinha um repositório criado com README, em vez de `git init` faça:

```bash
git clone https://github.com/SEU-USUARIO/servicofedex.git
# copie os arquivos deste projeto para dentro da pasta clonada
cd servicofedex
git add .
git commit -m "ServicoFedex: versão inicial"
git push
```

---

## 4. Disparar a build

O workflow tem três gatilhos (`on:` no YAML):

1. **Push automático**: todo `git push` para `main` ou `master` inicia a build.
2. **Pull request**: PRs para `main`/`master` também compilam (útil paravalidar alterações).
3. **Manual**: aba **Actions** → selecione **Build Android APK (ServicoFedex)** →
   botão **Run workflow** → escolha o branch → **Run workflow**.

Depois do `git push`, acesse:

```
https://github.com/SEU-USUARIO/servicofedex/actions
```

Você verá o job **Testar e gerar APK** em execução.

---

## 5. Acompanhar a execução

1. Clique no item mais recente da lista (mensagem do commit).
2. Clique em **Testar e gerar APK** para ver os passos.
3. Cada etapa aparece com os nomes numerados do workflow:
   1. Baixar código-fonte
   2. Instalar JDK 17
   3. Instalar Android SDK
   4. Permissão do Gradle
   5. Testes + build
   6/7/8. Upload dos artefatos
4. Em caso de erro, clique na etapa vermelha para ler o log completo.
5. O job é considerado concluído quando todos os passos ficam verdes (✓).

Tempo típico: 3 a 6 minutos na primeira execução (download de dependências);
execuções seguintes são mais rápidas por causa do cache do Gradle.

---

## 6. Baixar o APK gerado

1. Na página da execução concluída, role até a seção **Artifacts**.
2. Baixe **`servicofedex-debug-apk`** — é o instalável para teste.
3. O arquivo vem dentro de um `.zip`; extraia para obter `app-debug.apk`.

Também são gerados:

- `servicofedex-release-unsigned-apk` — build de release **sem assinatura**,
  serve para conferir o código de produção, mas **não instala** diretamente no
  celular (ver seção 9).
- `servicofedex-test-results` — relatório HTML dos testes unitários.

---

## 7. Instalar no celular Android

1. Transfira `app-debug.apk` para o celular (cabo USB, Drive, WhatsApp, e-mail...).
2. No celular, abra o arquivo.
3. O Android pedirá permissão para instalar fontes desconhecidas — autorize
   (Configurações → Segurança → Instalar apps desconhecidos).
4. Abra o app **ServicoFedex**.

Alternativa com cabo (com `adb` instalado):

```bash
adb install -r app-debug.apk
```

---

## 8. Rodar a build localmente (opcional)

Se você tiver JDK 17 + Android SDK instalados:

```bash
cd servicofedex

# Linux/macOS
export JAVA_HOME=/caminho/para/jdk-17
export ANDROID_HOME=/caminho/para/android-sdk

./gradlew testDebugUnitTest
./gradlew assembleDebug
```

O APK fica em `app/build/outputs/apk/debug/app-debug.apk`.
No Windows use `gradlew.bat` e defina `JAVA_HOME`/`ANDROID_HOME` nas variáveis
de ambiente do sistema.

---

## 9. (Opcional) Gerar um release assinado para publicação

O workflow atual já gera o release, porém sem assinatura — ideal para validar a
build. Para publicar na Play Store ou distribuir um APK instalável de produção:

### 9.1 Criar a keystore uma única vez (no seu computador)

```bash
keytool -genkeypair -v \
  -keystore servicofedex.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias servicofedex
```

Guarde a senha e o alias em local seguro. **Nunca** envie o `.jks` para o GitHub.

### 9.2 Criar secrets no GitHub

GitHub → repositório → **Settings → Secrets and variables → Actions → New repository secret**:

| Nome | Valor |
| --- | --- |
| `KEYSTORE_BASE64` | `base64 -w 0 servicofedex.jks` (Linux) ou `base64 servicofedex.jks` (macOS) |
| `KEYSTORE_PASSWORD` | senha da keystore |
| `KEY_ALIAS` | `servicofedex` |
| `KEY_PASSWORD` | senha da chave (pode ser igual à da keystore) |

### 9.3 Decodificar a keystore no workflow

Adicione antes do passo 5 em `.github/workflows/build.yml`:

```yaml
      - name: Preparar keystore de release
        env:
          KEYSTORE_BASE64: ${{ secrets.KEYSTORE_BASE64 }}
        run: echo "$KEYSTORE_BASE64" | base64 --decode > app/servicofedex.jks
```

E configure o `signingConfigs` em `app/build.gradle.kts`:

```kotlin
signingConfigs {
    create("release") {
        storeFile = file("servicofedex.jks")
        storePassword = System.getenv("KEYSTORE_PASSWORD")
        keyAlias = System.getenv("KEY_ALIAS")
        keyPassword = System.getenv("KEY_PASSWORD")
    }
}
buildTypes {
    getByName("release") {
        signingConfig = signingConfigs.getByName("release")
        isMinifyEnabled = false
        proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
}
```

Passe as variáveis no passo da build:

```yaml
      - name: 5. Rodar testes unitários e gerar APKs
        env:
          KEYSTORE_PASSWORD: ${{ secrets.KEYSTORE_PASSWORD }}
          KEY_ALIAS: ${{ secrets.KEY_ALIAS }}
          KEY_PASSWORD: ${{ secrets.KEY_PASSWORD }}
        run: ./gradlew testDebugUnitTest assembleDebug assembleRelease --no-daemon --stacktrace
```

---

## 10. Solução de problemas comuns

| Sintoma | Causa provável | Solução |
| --- | --- | --- |
| `Permission denied: ./gradlew` | Script sem permissão de execução no repositório | O workflow já roda `chmod +x ./gradlew`; localmente rode o mesmo |
| `SDK location not found` | `ANDROID_HOME` não definido | Use `android-actions/setup-android@v3` (já configurado) ou crie `local.properties` local com `sdk.dir=...` |
| `Could not determine java version` | JDK errado | Use JDK 17 (`actions/setup-java@v4` com `distribution: temurin`) |
| `Failed to install the following Android SDK packages... licenses not accepted` | Licenças não aceitas | O `setup-android@v3` aceita automaticamente; localmente rode `sdkmanager --licenses` |
| Build falha só na segunda execução | Cache corrompido | Actions → **Re-run all jobs**; se persistir, apague o cache em Settings → Actions → Caches |
| `Artifact not found` | Nome do APK mudou | Confira `app/build/outputs/apk/...` no log; o workflow usa `if-no-files-found: error` para avisar |
| Workflow não aparece | Actions desabilitados no repositório | Settings → Actions → General → **Allow all actions** |
| Erro `403` no `git push` | Token inválido | Gere um novo PAT com escopo `repo` |

---

## 11. Resumo do fluxo

```
git push  →  GitHub Actions  →  JDK 17 + SDK 35  →  testes  →  assembleDebug/Release
                                                        ↓
                                          Artefatos .apk para download
```

Pronto: cada alteração enviada ao GitHub gera automaticamente um novo APK testado.
