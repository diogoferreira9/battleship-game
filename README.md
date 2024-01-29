# Battleship Game

## Overview
Batalha Naval, or Battleship, is a strategy-type guessing game traditionally played on grid paper. This implementation of the game is a digital version developed in Kotlin, aimed at providing an interactive and engaging experience for users. The game supports single-player mode against the computer, offering features like ship placement, gameplay, saving, and loading game states.

## Features
- **Flexible Board Configuration**: Users can define the board's size within the constraints of the game rules.
- **Interactive Ship Placement**: Players can position their ships on the board, specifying coordinates and orientation.
- **Intelligent Computer Opponent**: The game features an automated opponent that makes random, yet logical, moves.
- **Game State Management**: Players can save their game progress to a file and load it later to resume the game.
- **Robust Coordinate Validation**: The game rigorously validates user inputs for coordinates and orientations, ensuring a smooth gaming experience.

## How to Play
1. **Set Up Board**: Define the board size as per the game rules.
2. **Place Ships**: Position your ships on the board by specifying coordinates and orientations.
3. **Start Game**: Once both players (you and the computer) have placed all ships, the game starts.
4. **Take Turns**: Players take turns guessing the coordinates of the opponent's ships.
5. **Check Status**: During the game, you can check the status of your shots and the remaining ships.
6. **Winning the Game**: The first player to sink all of the opponent's ships wins the game.

## Game Rules
The game supports board sizes of 4x4, 5x5, 7x7, 8x8, and 10x10. Each board size has a predefined set of ships:
- **Submarines** (1 cell)
- **Destroyers** (2 cells)
- **Cruisers** (3 cells)
- **Battleships** (4 cells)

Ships cannot overlap and must be placed within the confines of the board.

## Saving and Loading Games
You can save your game progress at any point and resume it later. The game state, including the positions of all ships and the history of moves, is saved to a file. You can load this file later to resume the game exactly where you left off.

## Technical Details
This game is implemented in Kotlin, leveraging its object-oriented features and robust type system. It runs in a console environment, accepting user inputs for various commands and displaying the game state in a text-based format.

## Conclusion
This Batalha Naval project represents an initial foray into game development with Kotlin. It encapsulates fundamental programming concepts and showcases the ability to create interactive and fun applications. Whether you're a beginner looking to understand programming basics or an enthusiast interested in game development, this project provides a solid foundation to build upon.

---

Enjoy the game, and may the best strategist win!