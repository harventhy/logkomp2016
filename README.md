# Sudoku Solver using MiniSAT

Simple n-Sudoku solver using [MiniSAT](http://minisat.se/). This project was made with the purpose of completing Computational Logic Assignment: SAT Solvers, Faculty of Computer Science, University of Indonesia, 2016.

## Getting Started

First of all, you need these:
1. Linux is preferred. You'll need Linux-alike environment to run this solver in Windows
2. Most recent package-manager
3. At least Java 6 environment is installed

## Installation

There are two ways to install MiniSAT

### Using Package Manager

To install MiniSAT using your Linux package manager, execute following command:

```
apt-get update
apt-get install minisat
```

Note: the package manager `apt-get` might be different depends on your Linux distribution.

### Using GCC Compiler

To install MiniSAT using GCC compiler, first you need to download the archive above or here: [minisat-2.2.0.tar.gz](http://minisat.se/MiniSat.html), then go to the downloaded directory and open the terminal there to execute these commands:

1. `tar xzf minisat-2.2.0.tar`
2. `cd minisat`
3. `export MROOT=$(pwd)`
4. `cd core`
5. `sudo apt-get install libghc-zlib-dev`
6. `make`

Note: the package manager `apt-get` might be different depends on your Linux distribution.

## How to Use

1. Go back to the main directory where you have the `minisat` directory extracted from the archive.
2. Put the `SudokuSolver.java` there
3. Compile the `SudokuSolver.java` using command `$ javac SudokuSolver.java`
4. Run the program using command `$ java SudokuSolver`
5. Enjoy

## Authors

* Syukri Mullia Adil Perkasa
* Dhanang Hadhi Sasmita
* Martin Novela