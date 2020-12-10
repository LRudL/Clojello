# Clojello

Clojello is a terminal (i.e. no GUI) implementation of the game [Othello (a variant of Reversi)](https://en.wikipedia.org/wiki/Reversi) in [Clojure](https://clojure.org/), a functional-oriented Lisp.

Project goals:

1. Elegant functional code (apart from IO, Clojello is 100% functional).
2. Designed in a very general and easily extensible way (switch a few parameters in the code and you can use the same code to play 3-player 3D Othello without writing a single new function, for example, or define a new function that implements an AI and you can trivially swap it in).

## Installation & running

Clojello uses [Leiningen](https://leiningen.org/) to automate build stuff. If you have a cloned version of this repository, run `lein uberjar` in the directory to create executable JAR files (Clojure compiles to Java) in the target/uberjar folder. After that, run with:

```
java -jar target/uberjar/clojello-0.1.0-SNAPSHOT-standalone.jar
```

Make sure that Leiningen and Java are installed.

## Usage

Player pieces are denoted by `X` and `O`. When printing the board, the next player to play is also printed, and in addition to player pieces you'll see lowercase letters marking possible moves. Type one of these letters and press enter to submit your move.

If a player is to play but has no moves, their turn is skipped. If the next player also has no moves, the game ends.

Example gameplay:

```
-------------------
               
               
        a      
      X O b    
    c O X      
      d        
               
               
-------------------
Player X to play
b
-------------------
               
               
      c   b    
      X X X    
      O X a    
               
               
               
-------------------
Player O to play
a
-------------------
               
               
               
      X X X    
      O O O    
    a e d c b  
               
               
-------------------
Player X to play
a
-------------------
               
               
    a f b c e  
      X X X    
    d X O O    
    X          
               
               
-------------------
Player O to play
...
```

