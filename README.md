# Codekata-Civ Sample Client

A sample client in kotlin for the [codekata-civ](https://github.com/fairviewrobotics/codekata-civ) competition.

Your code should go in `src/main/kotlin/app/AI.kt`.

Utility classes for representing the state of the game are in `src/main/kotlin/app/game.kt`. You're welcome to extend or change them. Note: if you change their constructors you'll have to adjust the api interface code.

The api interface code is in `src/main/kotlin/app/api.kt`. You can change it, but read the api documentation and make sure you know what you are doing.

Same for `src/main/kotlin/app/Runner.kt`.


# Running
Run your client with `./gradlew run --args="url keys..."`.

For example, to start a client for the serve on localhost:8080 with default keys, run:
`./gradlew run --args="http://localhost:8080 secret0 secret1 secret2 secret3`

# Questions, Issues?
Github issues and pull requests are welcome. Please report any bugs you find.

Contact Edward with questions.