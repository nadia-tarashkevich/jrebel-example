# JRevel Example Project

This is a minimal example "JRevel" project. It starts a tiny HTTP server and serves an index page that contains an input field.

## Requirements satisfied
- Create JRevel project (minimal example)
- It's a simple example project
- The index page contains an input field

## How to run

Using Gradle (requires JDK 17+):

```bash
./gradlew run
```

Then open your browser at:

```
http://localhost:8080/
```

You should see a page titled "JRevel Example" with a text input and a button. Submitting the form takes you to `/echo` which greets you by name.

You can also change the port by setting the `PORT` environment variable:

```bash
PORT=9090 ./gradlew run
```

## Project structure
- `src/main/java/com/example/jrevel/App.java` — main application starting the HTTP server
- `build.gradle` — Gradle build configuration
- `settings.gradle` — Gradle settings
