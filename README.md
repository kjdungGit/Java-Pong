# Java-Pong

Java-Pong is a small desktop implementation of the classic Pong arcade game. The project is intended as a hands-on Java game-programming exercise, with a focus on real-time updates, simple 2D physics, Swing/AWT rendering, keyboard input, and an opponent that reacts to the ball instead of following a fixed script.

## Features

- Play against an adaptive computer-controlled paddle.
- Move the left paddle with the `W` and `S` keys.
- Bounce the ball off the top and bottom walls and both paddles.
- Change the ball's return angle based on where it hits a paddle.
- Increase the ball speed as a rally continues.
- Track the player and AI scores on screen.
- Pause the game automatically when the window loses focus.
- Reset the paddles and ball after each point, with the player who was scored on serving next.
- Render the game with a bundled custom scoreboard font and no third-party dependencies.

## How to Play

1. Start the game using one of the launch methods below.
2. Click the game window so it has keyboard focus.
3. Press `W` to move the left paddle up and `S` to move it down.
4. Return the ball past the AI paddle to score. Prevent the ball from leaving through your side.
5. Keep the rally going: every paddle hit makes the ball slightly faster.

The right paddle uses trajectory prediction and raycasting to estimate where the ball will arrive. It reacts after the ball crosses a randomized threshold and deliberately includes some prediction error. Its accuracy also changes with the score difference, giving the opponent a lightweight adaptive difficulty model.

## Requirements

- Java Development Kit (JDK) 8 or newer
- A desktop environment with Java AWT/Swing support
- VS Code with the Extension Pack for Java, or another Java IDE

There are currently no external libraries in `lib/`; the game uses only the Java standard library.

## Running in VS Code

1. Open the project folder in VS Code.
2. Allow the Java extension to finish loading the project.
3. Open `src/Main/Main.java` and select **Run** above the `main` method, or choose the `Main` launch configuration from Run and Debug.

The project settings use `src` as the source directory and `bin` as the compiled output directory. The bundled font is stored in `src/Main/Resources/` and is included in the repository so the scoreboard can load it at runtime.

## Running from PowerShell

From the project root, compile the source files and copy the font resource into the output directory:

```powershell
javac -d bin (Get-ChildItem -Recurse src -Filter *.java | ForEach-Object FullName)
Copy-Item -Recurse -Force src\Main\Resources bin\Main\Resources
java -cp bin Main.Main
```

Close the game window to stop the render and update loops cleanly.

## Project Structure

```text
src/Main/
	Main.java             Game entry point, input, update loop, and scoring pauses
	Config.java           Paddles, ball physics, scoreboard, rendering, and key bindings
	AI.java               Right-paddle prediction and adaptive difficulty
	GraphicalRender.java  Frame construction and drawing
	RenderObject.java     Shape and color data passed to the renderer
	Screen.java           Swing window and render loop
	Resources/            Bundled scoreboard font

bin/                    Compiled classes and runtime resources
lib/                    Reserved for optional project libraries
.vscode/                Java project and launch configuration
```

## Game Loop

The game separates simulation from drawing:

- Game logic updates run at approximately 60 updates per second.
- Rendering runs at approximately 120 frames per second.
- Each update processes player input, ball movement and collisions, AI decisions, paddle movement, and the current frame.
- When a point is scored, the game briefly displays the score, centers the game objects, and resumes play.

## Project Objective

This project explores how a simple arcade game can be built from small cooperating Java classes. The main goals are to practice event-driven input, scheduled game loops, geometric collision detection, state resets, resource loading, and an AI opponent whose behavior is understandable and adjustable in code.
