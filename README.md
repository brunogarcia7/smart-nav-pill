# Smart Nav Pill

Transforme a **barrinha de navegação por gestos** do Android (a "pill" na borda
inferior da tela) em um atalho: **toque duas vezes** ou **pressione e segure** na
barrinha para abrir qualquer app que você escolher — sem root.

Criado especialmente para dar um atalho rápido ao **Círculo para Pesquisar /
MICS** em aparelhos Motorola com Android 15, onde esse recurso não vem de
fábrica, mas funciona com qualquer app instalado.

---

## ⚠️ Aviso importante — instalação por sua conta e risco

Este é um app **de código aberto, distribuído fora da Play Store**, instalado por
*sideload* (APK). Ao instalar, você assume toda a responsabilidade:

- A instalação e o uso são **por sua conta e risco**.
- O app é fornecido **"como está" (as is)**, sem nenhuma garantia (ver [LICENSE](LICENSE)).
- Ele exige o **serviço de acessibilidade** para funcionar (explicado abaixo). Só
  ative serviços de acessibilidade em apps em que você confia.
- Não coletamos, enviamos nem armazenamos nenhum dado seu (ver
  [PRIVACY.md](PRIVACY.md)).

Se não concorda com isso, não instale.

---

## 📥 Download

👉 **[Baixar o APK mais recente (SmartNavPill-1.2.apk — ~2 MB)](https://github.com/brunogarcia7/smart-nav-pill/releases/latest)**

Ou veja todas as versões na aba **[Releases](https://github.com/brunogarcia7/smart-nav-pill/releases)**.

---

## ✨ O que faz

- **Toque duplo** na barrinha → abre o app que você escolher.
- **Toque longo** (pressionar e segurar) na barrinha → abre outro app.
- A ação dispara **apenas na área da barra de navegação** (borda inferior), não na
  tela toda.
- **Não atrapalha os gestos do sistema**: o swipe para Início, Voltar e Recentes
  continuam funcionando normalmente (um *toque* é diferente de um *deslize*).
- Faixa de toque ajustável (largura, altura) e opção de **centralizar na
  barrinha**.
- Tempo do toque longo configurável.
- Vibração opcional ao acionar.
- Interface em português, com tema claro/escuro e Material You.

---

## 📱 Requisitos

- Android **10 a 17** (minSdk 29).
- Navegação **por gestos** ativada (não os 3 botões).
- **Sem root.**
- Testado principalmente no **Motorola Edge 30 Ultra (Android 15)**.

---

## 🚀 Como instalar e configurar

1. Baixe o APK na aba **Releases**.
2. No celular, permita a instalação de apps de fontes desconhecidas quando o
   sistema pedir.
3. Instale o APK e abra o **Smart Nav Pill**.
4. Toque em **Abrir Acessibilidade** e ative o serviço **Smart Nav Pill** em
   *Configurações → Acessibilidade*.
5. Volte ao app e:
   - Escolha o app do **Toque duplo**.
   - Escolha o app do **Toque longo**.
6. Ligue o botão **Ativar recurso**.
7. Pronto: toque duas vezes (ou segure) na barrinha para abrir seus apps.

Dica: se tocar sem querer, ative **Centralizar na barrinha** e reduza a largura
para diminuir toques acidentais.

---

## 🔒 Por que o app pede acessibilidade?

A única forma de um app comum (sem root) capturar um toque exatamente sobre a
barra de navegação do Android é através de um `AccessibilityService`, que permite
colocar uma camada invisível (`TYPE_ACCESSIBILITY_OVERLAY`) sobre essa região.

O Smart Nav Pill **não lê o conteúdo da sua tela** (`canRetrieveWindowContent` é
`false`). Ele só observa toques na faixa da barrinha para abrir o app escolhido.

---

## ⚙️ Limitações conhecidas (transparência)

- **Botão de energia (power):** não é possível capturar o duplo clique do botão
  power sem root — o Android reserva esse evento ao sistema. Por isso o app usa
  gestos na barrinha, não o botão power.
- **Conflito com outros apps de acessibilidade:** se você tiver outro app que
  também cria um overlay na mesma região (ex: *Touch The Notch*), pode haver
  disputa pelo toque. Se o toque falhar sobre apps, tente desativar o outro.
- **Apps em tela cheia / imersivos:** em alguns apps que escondem a barra, o
  reconhecimento pode ser menos consistente.

---

## 🛠️ Compilar a partir do código

Requisitos: **JDK 17**, **Android SDK** (API 36) e Git.

```bash
git clone <url-do-repositorio>
cd smart-nav-pill

# (opcional) gerar uma keystore propria para assinar o release:
keytool -genkeypair -v -keystore smartnavpill-test.keystore \
  -storepass snp12345 -keypass snp12345 -alias snptest \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -dname "CN=Smart Nav Pill, O=SmartNavPill, C=BR"

# compilar o APK de debug
./gradlew assembleDebug

# ou o release (se tiver a keystore acima)
./gradlew assembleRelease
```

O APK sai em `app/build/outputs/apk/`.

**Stack:** Kotlin, Jetpack Compose, Material 3, DataStore, AGP 8.13, Gradle 9.3.

---

## 🧪 Procuramos testadores!

Este app está em fase de testes com a comunidade. Se você testar, abra uma
**[Issue](../../issues)** contando:

- Modelo do aparelho e versão do Android.
- Se os gestos funcionaram (toque duplo / toque longo).
- Qualquer travamento ou comportamento estranho.

Seu retorno ajuda a melhorar o app. 🙏

---

## 🙏 Créditos e inspiração

A ideia foi inspirada no comportamento do app *Touch The Notch* (que usa a área
da câmera). Este projeto é uma implementação **independente e original**, voltada
à barra de navegação, sem usar código de terceiros.

---

## 📄 Licença

Distribuído sob a licença **MIT**. Veja [LICENSE](LICENSE).
