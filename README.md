# SympatheticGo engine

Monte Carlo Mechanically Symphatetic Go Engine.
This aims to be hardware symphatetic, allocation & GC free, and run in L1.

Some ideas from http://mechanical-sympathy.blogspot.com and some libraries from https://github.com/OpenHFT.

```
$ sysctl -n machdep.cpu.brand_string
Intel(R) Core(TM) i7-8559U CPU @ 2.70GHz

$ ./gradlew run --args 'perf'
===================================================================
Running 1 concurrent threads with 9x9 games ...
   thrd.  0:   7820ms,  222222 plys,  28417.14 ply/s, 35.190μs/ply
------------- -------- ------------ ----------------- -------------
Agr. & avg.:   7820ms,  222222 plys,  28417.14 ply/s, 35.190μs/ply
Effectively:   7882ms,  222222 plys,  28193.61 ply/s, 35.469μs/ply
===================================================================
Running 2 concurrent threads with 9x9 games ...
   thrd.  1:   8198ms,  222222 plys,  27106.86 ply/s, 36.891μs/ply
   thrd.  0:   8207ms,  222222 plys,  27077.13 ply/s, 36.932μs/ply
------------- -------- ------------ ----------------- -------------
Agr. & avg.:  16405ms,  222222 plys,  27091.98 ply/s, 36.911μs/ply
Effectively:   8209ms,  444444 plys,  54141.06 ply/s, 18.470μs/ply
===================================================================
[...]
===================================================================
Running 8 concurrent threads with 9x9 games ...
   thrd.  2:  15191ms,  222222 plys,  14628.53 ply/s, 68.360μs/ply
   thrd.  3:  15256ms,  222222 plys,  14566.20 ply/s, 68.652μs/ply
   thrd.  6:  15262ms,  222222 plys,  14560.48 ply/s, 68.679μs/ply
   thrd.  4:  15272ms,  222222 plys,  14550.94 ply/s, 68.724μs/ply
   thrd.  0:  15285ms,  222222 plys,  14538.57 ply/s, 68.783μs/ply
   thrd.  7:  15285ms,  222222 plys,  14538.57 ply/s, 68.783μs/ply
   thrd.  1:  15286ms,  222222 plys,  14537.62 ply/s, 68.787μs/ply
   thrd.  5:  15317ms,  222222 plys,  14508.19 ply/s, 68.927μs/ply
------------- -------- ------------ ----------------- -------------
Agr. & avg.: 122154ms,  222222 plys,  14553.56 ply/s, 68.712μs/ply
Effectively:  15319ms, 1777776 plys, 116050.39 ply/s, 8.617μs/ply
```

```
$ lscpu | grep "Model name"
Model name:          AMD Ryzen 7 3800X 8-Core Processor

$ java -server -jar build/libs/sgo.jar perf
===================================================================
Running 1 concurrent threads with 9x9 games ...
   thrd.  0:   6758ms,  222222 plys,  32882.81 ply/s, 30.411μs/ply
------------- -------- ------------ ----------------- -------------
Agr. & avg.:   6758ms,  222222 plys,  32882.81 ply/s, 30.411μs/ply
Effectively:   6919ms,  222222 plys,  32117.65 ply/s, 31.136μs/ply
===================================================================
Running 2 concurrent threads with 9x9 games ...
   thrd.  0:   6986ms,  222222 plys,  31809.62 ply/s, 31.437μs/ply
   thrd.  1:   7007ms,  222222 plys,  31714.29 ply/s, 31.532μs/ply
------------- -------- ------------ ----------------- -------------
Agr. & avg.:  13993ms,  222222 plys,  31761.88 ply/s, 31.484μs/ply
Effectively:   7019ms,  444444 plys,  63320.13 ply/s, 15.793μs/ply
===================================================================
[...]
===================================================================
Running 15 concurrent threads with 9x9 games ...
   thrd. 13:  11671ms,  222222 plys,  19040.53 ply/s, 52.520μs/ply
   thrd. 10:  11697ms,  222222 plys,  18998.20 ply/s, 52.637μs/ply
   thrd.  9:  11725ms,  222222 plys,  18952.84 ply/s, 52.763μs/ply
   thrd.  8:  11736ms,  222222 plys,  18935.07 ply/s, 52.812μs/ply
   thrd.  1:  11718ms,  222222 plys,  18964.16 ply/s, 52.731μs/ply
   thrd.  6:  11715ms,  222222 plys,  18969.01 ply/s, 52.718μs/ply
   thrd. 14:  11744ms,  222222 plys,  18922.17 ply/s, 52.848μs/ply
   thrd.  0:  11744ms,  222222 plys,  18922.17 ply/s, 52.848μs/ply
   thrd. 12:  11780ms,  222222 plys,  18864.35 ply/s, 53.010μs/ply
   thrd.  2:  11787ms,  222222 plys,  18853.14 ply/s, 53.042μs/ply
   thrd.  4:  11764ms,  222222 plys,  18890.00 ply/s, 52.938μs/ply
   thrd. 11:  11778ms,  222222 plys,  18867.55 ply/s, 53.001μs/ply
   thrd.  7:  11823ms,  222222 plys,  18795.74 ply/s, 53.204μs/ply
   thrd.  5:  11879ms,  222222 plys,  18707.13 ply/s, 53.456μs/ply
   thrd.  3:  12007ms,  222222 plys,  18507.70 ply/s, 54.032μs/ply
------------- -------- ------------ ----------------- -------------
Agr. & avg.: 176568ms,  222222 plys,  18878.45 ply/s, 52.970μs/ply
Effectively:  12035ms, 3333330 plys, 276969.67 ply/s, 3.611μs/ply
```

16 threads is considerably slower than 15 threads in this processor.

## Usage

```
$ ./gradlew build 
$ java -jar build/libs/sgo.jar help
$ java -jar build/libs/sgo.jar perf
$ java -jar build/libs/sgo.jar gtp:random
$ java -jar build/libs/sgo.jar gtp:mc1
$ misc/sgo-gtp
```


## Mini-Roadmap 

- Better MCTS implementation
  - MC Game finisher has to understand eyes and basic livability, to avoid committing suicide.
  - Engine should generate moves for any of the players (i.e. a flag to not enforce next player in Game class)
  - Basic statical positional board for MCCS. i.e. D4 has a 1/8 chance of being played on 1st move, using games.
  - Simple Remote Service, so it can be distributed

- Aim: fun, practice Sympathetic coding in Java, implement > 1D at 19x19 

- Check about SuperKO rules here, we might need to revisit current impl: http://www.weddslist.com/kgs/past/superko.html
- Check "Mastering the game of Go without Human Knowledge" https://www.nature.com/articles/nature24270.epdf
- https://github.com/mattheww/gomill
- Integrate https://github.com/jythontools/jython for meta-programming?
- https://en.wikipedia.org/wiki/Computer_Go
- https://en.wikipedia.org/wiki/Thompson_sampling#Upper-Confidence-Bound_(UCB)_Algorithms
- https://en.wikipedia.org/wiki/Monte_Carlo_tree_search

- Log into stderr the expanded mtcs tree
- Fix TimeManager ticks

- tune a factor for uct + coordByMoveDist9x9[moveNo][i / 2] * FACTOR;
- tune expansionThreshold = 3

- Heatmaps for UCT, topsFor, so easier to analyse

- fix misc/sgfs/20200327-lost-because-played-in-atari-multiple-times.sgf
- playing in atari should not be an option (in most of the cases) (random and no random...)
