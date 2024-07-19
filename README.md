<h1 align="center">Diep.Java</h3>

<p align="center">
  <a href="#about">About</a> •
  <a href="#features">Features</a> •
  <a href="#build">Build</a> •
  <a href="#running">Running</a>
</p>

<p align="center">
  <img src="./Sample.webp" alt="Sample Webp" />
</p>

---

## About

Remake of Diep.io in Java. All tanks and stats are balanced from the original game thanks to reverse-engineering and research from https://github.com/ABCxFF/diepindepth. A few tank definitions have been tweaked and the newest tank Glider has been added (too new to be found in `diepindepth` definitions). JSON file with all tank stats and geometry (barrel size, angle, etc) are parsed with the library https://github.com/stleary/JSON-java and drawn at runtime. As a result, there is no need to hardcode the art of tanks and their stats. Additionally, it is very easy to add new custom tank definitions or tweak existing ones.

## Features

- Original Diep.io tanks, stats, physics, level/XP system, controls, etc.
- Bots that pre-aim perfectly and dodge bullets (algorithm based on moving perpendicular to incoming bullet velocities and repulsion from obstacles)
- Polygon spawning (square, triangles, pentagons, alpha pentagons, and crashers) system
- Live updating leaderboard, minimap, and leader direction pointer
- Death and menu screen 
- Different game modes
- Custom camera zoom for debugging
- ALL tanks except for necromancer and factory are implemented and tested (including more complex custom tanks such as overlord, mothership, spike, auto-5, rocketeer, etc.) 
- And more!

## Build

The external .jar libraries are already included in the `libs` folder. This includes a Raylib (OpenGL-based game library) port for Java and the JSON parsing library. Add these external libraries to your project (depends on your IDE). For example, in VSCode, add them to `Referenced Libraries` and in IntelliJ IDEA, add them to `Modules` in the `Project Structure` settings.

Next, add the folders `src`, `assets`, and `config` to the classpath.

Finally, run the `Main.java` file in the root folder.

## Running
A pre-configured folder with an exe file can be found in releases. For now, any windows machine should be able to directly play the game without any installation.
The folder comes with all required files and a JRE to run the game.












