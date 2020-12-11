# Clojello

Clojello is a terminal (i.e. no GUI) implementation of the game [Othello (a variant of Reversi)](https://en.wikipedia.org/wiki/Reversi) in [Clojure](https://clojure.org/), a functional-oriented Lisp.

Project goals:

1. Elegant functional code (apart from IO, Clojello is 100% functional).
2. Designed in a very general and easily extensible way (switch a few parameters in the code and you can use the same code to play 3-player 3D Othello without writing a single new function, for example, or define a new function that implements an AI and you can trivially swap it in).

## Installation & running

Clojello uses [Leiningen](https://leiningen.org/) to automate build stuff. If you have a cloned version of this repository, run `lein uberjar` in the directory to create executable JAR files (Clojure compiles to Java) in the target/uberjar folder. After that, run with:

```
java -jar target/uberjar/clojello-0.2.0-standalone.jar
```

Make sure that Leiningen and Java are installed.

## Usage

Player pieces are denoted by `X` and `O`. When printing the board, the next player to play is also printed, and in addition to player pieces you'll see lowercase letters marking possible moves. Type one of these letters and press enter to submit your move.

If a player is to play but has no moves, their turn is skipped. If the next player also has no moves, the game ends.

Example gameplay:

```
Press enter for default game, enter anything else to customise. Type undo/quit to undo a move / quit the game.
.
Enter board height
6
Enter board width
12
Enter number of players
3
- - - - - - - - - - - -
                       
        b c            
          O U X        
        a U X O        
          X O U        
                       
- - - - - - - - - - - -
Player X to play
a
- - - - - - - - - - - -
                       
                       
        b O U X        
        X U X O        
      a d X O U        
          c            
- - - - - - - - - - - -
Player O to play
c
- - - - - - - - - - - -
                       
                       
        a O U X        
        X O X O        
        b O O U        
          O            
- - - - - - - - - - - -
Player U to play
b
- - - - - - - - - - - -
                       
        a              
          O U X        
        X O X O        
        U O O U        
        b O            
- - - - - - - - - - - -
Player X to play
...
```

