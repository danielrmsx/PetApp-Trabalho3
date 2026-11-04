# PetApp — Trabalho 4 (Arquitetura MVVM)

App Android em Kotlin + Jetpack Compose: um marketplace de petshops onde o usuário cadastra seus pets e agenda serviços (banho, tosa, vacinas...). Os dados ficam salvos no aparelho com Room.

No Trabalho 4 o app **não ganhou telas nem funções novas**: o mesmo código do Trabalho 3 foi reorganizado em **MVVM** (Model, View, ViewModel), com Repository, StateFlow/UiState e ViewModelFactory.

---

## Como rodar o projeto

1. Instale o **Android Studio** (versão recente, com Android SDK 37).
2. Clone o repositório: `git clone https://github.com/danielrmsx/PetApp-Trabalho3.git`
3. No Android Studio: **File > Open** e escolha a pasta do projeto.
4. Espere o **Gradle Sync** terminar (ele baixa Room, Navigation, Lifecycle e Coil).
5. Escolha um emulador ou celular e clique em **Run ▶**.
6. Na primeira vez, toque em **Cadastre-se**, crie uma conta e faça login.

---

## 1. Como estava no Trabalho 3 e o que mudou

**Antes:** todos os arquivos ficavam soltos na mesma pasta. Cada tela (`@Composable`) chamava o DAO direto, criava a própria `CoroutineScope` com `Dispatchers.IO` e guardava o estado em várias variáveis `remember` separadas. Depois de inserir ou remover, a tela precisava chamar `carregarDados()` de novo.

**Agora:**

| Camada | O que faz | Onde está |
|---|---|---|
| **Model** | Entidades, DAOs, banco e regras (porte, preço) | `data/` |
| **Repository** | Única porta de entrada para os dados | `data/repository/` |
| **ViewModel** | Estado da tela (`UiState`) e ações | `viewmodel/` |
| **View** | Telas Compose: só desenham e avisam o ViewModel | `ui/` |

Fluxo: **Tela → ViewModel → Repository → DAO → Room**, e os dados voltam pelo caminho inverso via `Flow`/`StateFlow`.

> 📸 *[Inserir aqui print da árvore de pastas no Android Studio, antes e depois]*

---

## 2. Como dividimos os pacotes

```
com.example.petapp
├── MainActivity.kt
├── data/                 → Model
│   ├── Entidades.kt          (@Entity: Usuario, Pet, Petshop, Agendamento)
│   ├── Daos.kt               (@Dao com Flow)
│   ├── AppDatabase.kt        (@Database, Singleton)
│   ├── RegrasPet.kt          (porte, preço por porte, idade humana)
│   └── repository/       → Repository
│       ├── UsuarioRepository.kt
│       ├── PetRepository.kt
│       ├── PetshopRepository.kt
│       ├── AgendamentoRepository.kt
│       └── Repositorios.kt   (cria os 4 Repositories uma vez só)
├── viewmodel/            → ViewModel + UiState + Factory
│   ├── LoginViewModel.kt, CadastroViewModel.kt, InicioViewModel.kt
│   ├── PetsViewModel.kt, DetalhePetViewModel.kt
│   ├── AgendaViewModel.kt, DetalheAgendamentoViewModel.kt
│   ├── ContaViewModel.kt, EditarPerfilViewModel.kt
│   └── PetAppViewModelFactory.kt
└── ui/                   → View
    ├── AppNavigation.kt, Rotas.kt
    ├── Componentes.kt, Mascaras.kt
    ├── *Screen.kt (9 telas)
    └── theme/
```

**Decisões:**
- **Regras de negócio** (preço por porte, idade humana) foram para `data/RegrasPet.kt`, porque são regras do domínio e não desenho.
- **Componentes repetidos** (`FotoPerfil`, `CartaoSecao`) e **máscaras** (CPF, telefone, CEP, data, hora) foram para `ui/Componentes.kt` e `ui/Mascaras.kt`. Antes havia cópias iguais em três telas.
- A classe `DadosApp` deixou de existir. O que ela fazia foi para o lugar certo: banco e sessão no Repository, pré-seleção de serviço/pet na rota da Agenda (`agenda?servico=...&petId=...`).

---

## 3. Por que o Repository existe

Antes, se o jeito de buscar dados mudasse, seria preciso mexer em todas as telas. Agora **só o Repository fala com o DAO** e só o ViewModel fala com o Repository. Se um dia os pets vierem de uma API, só o `PetRepository` muda: ViewModel e tela continuam iguais.

Criamos **um Repository por Entity** (4 no total). O `UsuarioRepository` também cuida da **sessão** (quem está logado), que antes era lida do `SharedPreferences` direto pelas telas.

---

## 4. DAO reativo com Flow

`buscarTodos()` agora devolve `Flow<List<...>>` em vez de `suspend fun ... : List<...>`. Com isso o Room avisa sozinho quando a tabela muda: ao adicionar um pet, a lista da tela atualiza sem precisar buscar de novo. Também criamos `observarPorId(id)` (Flow) para as telas de Detalhe e para o usuário logado, e `contar()` no `PetshopDao`.

Queries além das 4 básicas: `autenticar`, `buscarPorEmailOuCpf`, `buscarPorId`, `observarPorId`, `buscarPorPetId`, `observarPorPetId` e `contar`.

---

## 5. O que tem no UiState de cada tela

| ViewModel | Campos principais do UiState | Por quê |
|---|---|---|
| `LoginViewModel` | emailOuCpf, senha, lembrarMim, erro, diálogo de recuperar senha, mensagem, loginRealizado | Tudo que o formulário mostra; `loginRealizado` avisa a tela para navegar |
| `CadastroViewModel` | nome, cpf, email, celular, senha, confirmarSenha, aceiteTermos, erro | Campos do formulário e o erro de validação |
| `InicioViewModel` | usuario, pets, agendamentos, petAtualId, folhas abertas | Card do pet atual, contador de pendentes e notificações |
| `PetsViewModel` | pets, agendamentos, campos do formulário, petParaRemover | Lista + formulário + diálogo de remover |
| `DetalhePetViewModel` | pet, agendamentosDoPet, pesoTexto, erroPeso | Detalhe, tabela de preços e edição do peso |
| `AgendaViewModel` | pets, petshops, agendamentos, formulário, aba e petshop abertos | As 3 abas e o formulário de agendamento |
| `DetalheAgendamentoViewModel` | agendamento, pet, excluido | Detalhe e cálculo do valor total |
| `ContaViewModel` | usuario, totalPets, pendentes, concluidos | Estatísticas da conta |
| `EditarPerfilViewModel` | campos do perfil, mensagem, salvo | Formulário de edição |

Cada ViewModel segue o padrão `private val _uiState = MutableStateFlow(...)` + `val uiState: StateFlow<...> = _uiState.asStateFlow()`. A tela lê com `collectAsStateWithLifecycle()` e só chama métodos do ViewModel. Nenhuma tela tem mais `dao`, `CoroutineScope` ou `Dispatchers.IO`.

---

## 6. ViewModelFactory

Usamos **uma Factory reaproveitada** (`PetAppViewModelFactory`), que recebe os 4 Repositories no construtor e cria o ViewModel certo. Em cada tela: `vm: PetsViewModel = viewModel(factory = fabrica)`.

---

## 7. O que quebrou durante a reorganização e como resolvemos

- **Arquivos duplicados:** ao mover as telas e o banco para os pacotes `ui`, `data` e `viewmodel`,
  as versões antigas continuaram na pasta raiz. Isso causaria erro de compilação por declaração
  duplicada (duas `LoginScreen`, duas classes `Pet`). Resolvemos apagando os arquivos antigos e
  deixando só o `MainActivity.kt` na raiz.
- **Bug do "Lembrar de mim":** no Trabalho 3, quem fazia login sem marcar "Lembrar de mim" via
  "Usuário" no Início e o Editar Perfil vazio, porque o id do usuário não era guardado em lugar
  nenhum. Na reorganização, a sessão passou a ser controlada pelo `UsuarioRepository`, que guarda
  o id na memória enquanto o app está aberto.

  

---

# Histórico — Trabalho 3 (Room & Persistência Local)

Repositório contendo a evolução do **PetApp** para o Trabalho 3. O projeto consiste em um aplicativo mobile desenvolvido em Kotlin e Jetpack Compose focado em um Marketplace de Petshops, estruturado com persistência de dados local utilizando **SQLite / Room Database**.

---

## 1. Evolução: Trabalho 2 vs. Trabalho 3
* **No Trabalho 2:** O aplicativo gerenciava dados utilizando coleções em memória (`mutableStateListOf`). Embora permitisse inserções e remoções em tempo de execução, o encerramento da aplicação resultava na perda integral de todos os registros.
* **No Trabalho 3:** A camada de dados foi completamente migrada para o **Room Database**. Agora, todas as operações de cadastro de usuários, pets, petshops e agendamentos são gravadas de forma permanente no dispositivo.

---

## 2. Arquitetura e Decisões Técnicas

### 2.1. Entidades (`@Entity`)
O banco de dados conta com quatro entidades principais, estruturadas com chaves primárias autogeradas (`@PrimaryKey(autoGenerate = true)`):
* **`Usuario`**: Armazena os dados do cliente (nome, CPF, e-mail, celular, senha, foto e CEP), permitindo referenciar a localização para o marketplace de serviços.
* **`Pet`**: Gerencia o perfil dos animais (nome, espécie, sexo, raça, idade e peso), utilizados também no cálculo dinâmico de preços dos serviços de acordo com o porte.
* **`Petshop`**: Modela as unidades parceiras disponíveis na região (nome, profissionais, serviços prestados, distância simulada e avaliação).
* **`Agendamento`**: Controla os compromissos vinculados ao pet e ao petshop, suportando múltiplos serviços simultâneos, data, horário, observações e status de conclusão.

### 2.2. DAOs e Consultas (`@Dao`)
Foram criadas interfaces DAO específicas para cada entidade (`UsuarioDao`, `PetDao`, `PetshopDao`, `AgendamentoDao`):
* `UsuarioDao`, `PetDao` e `AgendamentoDao` implementam as operações fundamentais (`@Insert`, `@Query` para busca geral, `@Update` e `@Delete`), obrigatoriamente declaradas como **`suspend fun`**.
* Incluem consultas filtradas por identificador (ex: `buscarPorId(id: Int)`), permitindo que os ecrãs de detalhes busquem o registo correto diretamente do banco de dados a partir do ID recebido na navegação.

### 2.3. Configuração do Banco (`AppDatabase`)
* A classe abstrata herda de `RoomDatabase` e integra todas as entidades.
* Implementa o **padrão Singleton** utilizando `companion object`, `@Volatile` e `synchronized` para otimizar o acesso e prevenir múltiplas instâncias concorrentes.

---

## 3. Desafios de Migração e Soluções

* **Concorrência e Assincronia (Corrotinas):** Para impedir o travamento da thread principal (UI), todas as chamadas aos DAOs foram encapsuladas estritamente dentro de blocos assíncronos utilizando `CoroutineScope(Dispatchers.IO).launch { ... }`.
* **Sincronização de Estado com o Room:** Com a remoção das listas voláteis, a interface passou a carregar os dados diretamente do banco na inicialização dos ecrãs por meio de `LaunchedEffect(Unit)` chamando `dao.buscarTodos()`.
* **Tratamento de Entradas e Máscaras:** Ajuste de formatações visuais (CEP, CPF, Telefone, Datas) com limites seguros baseados em `.take()` para garantir estabilidade e prevenir exceções de índice (*IndexOutOfBounds*) ao preencher formulários rapidamente.

---

## 4. Teste de Aceitação (Persistência Real)

O comportamento esperado para a validação do trabalho segue o procedimento abaixo:
1. **Adicionar:** Cadastrar um novo pet ou registrar um agendamento na aba de agenda/marketplace.
2. **Fechar:** Encerrar o aplicativo por completo, removendo-o do histórico de aplicações recentes do dispositivo.
3. **Reabrir:** Iniciar a aplicação novamente e verificar que os dados inseridos continuam preservados na interface, comprovando o funcionamento bem-sucedido do SQLite/Room.