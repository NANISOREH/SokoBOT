SokoBOT is a Java implementation of a solver for the puzzle game Sokoban, a classic toy problem of AI, providing
a simplified model for real-life problems in the fields of robotics and logistics. It ships with a simple JavaFX GUI and a small selection of sample levels.
As soon as I can I'll make it possible to simply import your own json-encoded levels and manage them. 

You can try it out by building it and running it with Maven with `mvn clean javafx:run`. Or, better yet, by downloading one of the runtime images
in the Release section. They should run on any Linux or Windows machine, since they are prepackaged with everything that's needed to run the program.

To get the optimal box-goal matching required by A* heuristic function I used code from this repo: https://github.com/KevinStern/software-and-algorithms
distributed under MIT license. The entirety of the copyright notice is available in the file SokobanToolkit.java, in which the code was used.

