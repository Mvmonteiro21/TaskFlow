# TaskFlow — MVP Android (Kotlin + Jetpack Compose + Room)

Gerenciador de tarefas com persistência local.

## Como executar
1. Android Studio (Koala+ / JDK 17) → **File > Open** → pasta `TaskFlow`.
2. Aguarde o *Gradle Sync* (o Android Studio gera o `gradlew`/wrapper automaticamente).
3. Escolha um emulador (API 24+) ou dispositivo físico e clique em **Run ▶**.
4. Testes unitários: `./gradlew test`.

## Arquitetura (app/src/main/kotlin/com/example/taskflow)
- `ui/` — Composables + ViewModels (apenas apresentação e eventos)
- `domain/` — modelos, interfaces de repositório e casos de uso (regras de negócio)
- `data/` — Room (SQLite) e SharedPreferences, implementações dos repositórios
- `navigation/` — NavHost e rotas
- `di/` — AppContainer (injeção manual)

Fluxo de dependência: `ui → domain ← data`.
