# 🏋️‍♂️ ZENKAI – Android App
Android app for workout progression and performance analysis built with Kotlin and Jetpack Compose.

> **Projeto de referência** que simula um produto comercial real, construído com as práticas mais atuais de desenvolvimento Android.

---  

## 👀 Visão geral  
**ZENKAI** é um aplicativo Android escrito em **Kotlin + Jetpack Compose** que permite ao usuário:

* Registrar séries de exercícios (Exercise → ExerciseSet)  
* Visualizar evolução de cargas, volume e estimativas de 1RM  
* Detectar períodos de estagnação e sugerir ajustes de periodização  
* Exportar métricas para futuras análises com IA (TensorFlow Lite)

O código foi estruturado como um **produto real**, aplicando boas práticas de arquitetura, testes e escalabilidade.

---  

## 🎯 Objetivo técnico  
Demonstrar domínio prático das seguintes áreas:

| Área | Como foi abordada |
|------|-------------------|
| **Desenvolvimento Android moderno** | Kotlin, Coroutines, Flow, Material 3 |
| **Arquitetura MVVM** | Camada de UI → ViewModel → Repository → Room |
| **UI declarativa** | Jetpack Compose com StateFlow & `collectAsState` |
| **Persistência local** | Room (entidades, DAO, relacionamentos 1:N, cascade delete) |
| **Análise de dados** | Algoritmos para 1RM, volume total, trendlines |
| **Preparação para IA** | Estrutura pronta para integrar TensorFlow Lite sem acoplar a UI |

---  

## 🏗️ Arquitetura  

```
UI (Jetpack Compose)
│
└─ ViewModel (AndroidViewModel)
   │   • StateFlow + UI‑reactiva
   │   • Business rules
   │   • Coroutines (ViewModelScope)
   │
   └─ Repository
       │   • DAO (Room)
       │
       └─ Database (Room)
           • Entities: Exercise, ExerciseSet
           • Relations 1:N, cascade delete
           • Exposição via Flow
```

* **Separação de responsabilidades** – UI não contém nenhuma regra de domínio.  
* **Estado previsível** – Todo fluxo de dados passa por `StateFlow`, garantindo UI determinística.  

---  

## 🗃️ Persistência de dados  

```kotlin
@Entity
data class Exercise(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val name: String,
    val target: String
)
```

* **Room** – biblioteca oficial de SQLite com compile‑time verification.  
* **Relação 1:N** – `Exercise` possui múltiplas `ExerciseSet`.  
* **Cascade delete** – remoção automática de séries ao excluir um exercício.  
* **Observabilidade** – consultas retornam `Flow<List<…>>`, atualizando a UI em tempo real.  

---  

## 🔄 Gerenciamento de estado  

| Estratégia | Uso no projeto |
|------------|----------------|
| `remember` / `rememberSaveable` | Preservação de UI state entre recomposições e processos. |
| `StateFlow` + `collectAsState()` | Fonte única de verdade para a camada de UI. |
| Navegação baseada em estado | Fluxo de telas gerenciado pelo ViewModel, evitando lógica de navegação espalhada. |

**Benefícios**: menos bugs, UI previsível, manutenção simplificada.  

---  

## 🎨 UI / UX  

* **Jetpack Compose + Material 3** – design system consistente e responsivo.  
* **Componentização** – cada tela e widget são módulos reutilizáveis.  
* **Animações sutis** – feedback visual ao inserir séries, salvar exercícios, etc.  
* **Canvas custom** – plotagem de trendlines e volume total.  

> *Projeto pensado para uso diário, com leitura rápida de métricas e visualização clara da evolução.*  

---  

## 📊 Lógica de negócio (diferencial)  

* **Estimativa de 1RM** (Epley, Brzycki, etc.).  
* **Detecção de progressão** – identificação automática de platôs ou regressões.  
* **Cálculo de volume (tonelagem)** – soma de carga × repetições por sessão.  
* **Trendline** – linha de tendência gerada por regressão linear sobre os dados de carga.  
* **Periodização baseada em histórico** – sugestões de ajustes de volume/intensidade.  

Tudo isso funciona **offline**, sem necessidade de backend.  

---  

## 🤖 Preparação para IA  

* Camada de **UseCases** pronta para receber algoritmos de *pattern detection* ou modelos de **TensorFlow Lite**.  
* Código de domínio **puro**, desacoplado da camada de UI – facilita a inserção de testes e experimento de ML.  

---  

## 🧪 Qualidade de código  

* **Modularidade** – pacotes bem delimitados (`ui`, `viewmodel`, `domain`, `data`).  
* **Funções puras** – regras de negócio encapsuladas em funções testáveis.  
* **Naming semântico** – nomes claros, refletindo a responsabilidade.  
* **Reutilização** – componentes Compose reutilizáveis e funções de extensão.  

---  

## 🛠️ Stack tecnológico  

| Tecnologia | Uso |
|-----------|-----|
| **Kotlin** | Linguagem principal |
| **Jetpack Compose** | UI declarativa |
| **Material 3** | Sistema de design |
| **Room** | Persistência local |
| **Coroutines** | Operações assíncronas |
| **Flow** | Reatividade de dados |
| **MVVM** | Arquitetura |
| **Hilt (roadmap)** | Injeção de dependências (planejado) |
| **TensorFlow Lite (roadmap)** | IA local (planejado) |

---  

## 🗺️ Roadmap técnico  

| Etapa | O que será adicionado |
|-------|------------------------|
| ✅ **Camada Repository formal** | Interfaces claras, implementação baseada em DAO |
| ✅ **UseCases** | Encapsulamento de regras de negócio |
| ✅ **Testes unitários** | Cobertura de ViewModel & UseCases |
| ✅ **DataStore** | Armazenamento de preferências (ex.: unidade de medida) |
| ⚙️ **DI com Hilt** | Injeção de dependências e testes mais fáceis |
| ⚙️ **Modularização** | Módulos `:app`, `:domain`, `:data`, `:ui` |
| ⚙️ **CI/CD** | GitHub Actions para builds e testes automáticos |
| ⚙️ **Backend opcional** | API REST para sincronização multi‑dispositivo |
| ⚙️ **ML** | Modelos de recomendação de periodização |

---  

## 💡 Por que este projeto importa?  

* **Visão de produto** – não é apenas um CRUD; inclui métricas reais de performance e lógica de periodização.  
* **Domínio de Android moderno** – demonstra habilidade com Compose, Flow, Coroutines e arquitetura limpa.  
* **Escalável e testável** – código preparado para crescimento, modularização e integração de IA.  
* **Valor agregado ao recrutador** – evidencia capacidade de transformar requisitos de negócio em soluções técnicas robustas.  

---  

## ▶️ Como executar  

> **Pré‑requisitos**  
> - Android Studio Flamingo (ou superior)  
> - Android SDK 33 (target)  
> - JDK 17  

```bash
# 1️⃣ Clone o repositório
git clone https://github.com/gustavohenrique/zenkai-android.git
cd zenkai-android

# 2️⃣ Abra o projeto no Android Studio
# (Android Studio irá sincronizar Gradle automaticamente)

# 3️⃣ Execute no emulador ou dispositivo físico
./gradlew installDebug
```

*O banco de dados é criado na primeira execução; não há configuração adicional.*  

---  

## 🤝 Contribuição  

Contribuições são bem‑vindas!  

1. Fork o repositório  
2. Crie uma branch `feature/nome-da-funcionalidade`  
3. Commit suas alterações  
4. Abra um Pull Request descrevendo a mudança  

---  

### ⭐️ Se gostou, deixe sua estrela!  

---  
