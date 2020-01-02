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

$ ./gradlew run --args 'perf'
===================================================================
Running 1 concurrent threads with 9x9 games ...
   thrd.  0:   8140ms,  222222 plys,  27300.00 ply/s, 36.630μs/ply
------------- -------- ------------ ----------------- -------------
Agr. & avg.:   8140ms,  222222 plys,  27300.00 ply/s, 36.630μs/ply
Effectively:   8322ms,  222222 plys,  26702.96 ply/s, 37.449μs/ply
===================================================================
Running 2 concurrent threads with 9x9 games ...
   thrd.  1:   8428ms,  222222 plys,  26367.11 ply/s, 37.926μs/ply
   thrd.  0:   8440ms,  222222 plys,  26329.62 ply/s, 37.980μs/ply
------------- -------- ------------ ----------------- -------------
Agr. & avg.:  16868ms,  222222 plys,  26348.35 ply/s, 37.953μs/ply
Effectively:   8449ms,  444444 plys,  52603.15 ply/s, 19.010μs/ply
===================================================================
[...]
===================================================================
Running 16 concurrent threads with 9x9 games ...
   thrd.  3:  14058ms,  222222 plys,  15807.51 ply/s, 63.261μs/ply
   thrd.  5:  14060ms,  222222 plys,  15805.26 ply/s, 63.270μs/ply
   thrd.  2:  14147ms,  222222 plys,  15708.07 ply/s, 63.662μs/ply
   thrd.  9:  14137ms,  222222 plys,  15719.18 ply/s, 63.617μs/ply
   thrd.  0:  14189ms,  222222 plys,  15661.57 ply/s, 63.851μs/ply
   thrd. 11:  14225ms,  222222 plys,  15621.93 ply/s, 64.013μs/ply
   thrd. 15:  14246ms,  222222 plys,  15598.90 ply/s, 64.107μs/ply
   thrd. 14:  14221ms,  222222 plys,  15626.33 ply/s, 63.995μs/ply
   thrd.  6:  14297ms,  222222 plys,  15543.26 ply/s, 64.337μs/ply
   thrd. 13:  14267ms,  222222 plys,  15575.94 ply/s, 64.202μs/ply
   thrd.  7:  14340ms,  222222 plys,  15496.65 ply/s, 64.530μs/ply
   thrd.  8:  14341ms,  222222 plys,  15495.57 ply/s, 64.535μs/ply
   thrd.  1:  14402ms,  222222 plys,  15429.94 ply/s, 64.809μs/ply
   thrd. 12:  14438ms,  222222 plys,  15391.47 ply/s, 64.971μs/ply
   thrd. 10:  14483ms,  222222 plys,  15343.64 ply/s, 65.174μs/ply
   thrd.  4:  14640ms,  222222 plys,  15179.10 ply/s, 65.880μs/ply
------------- -------- ------------ ----------------- -------------
Agr. & avg.: 228491ms,  222222 plys,  15561.02 ply/s, 64.263μs/ply
Effectively:  14659ms, 3555552 plys, 242550.79 ply/s, 4.123μs/ply
```

## Usage

```
$ ./gradlew build 
$ java -jar build/libs/sgo.jar help
$ java -jar build/libs/sgo.jar perf
$ java -jar build/libs/sgo.jar gtp
$ misc/sgo-gtp
```


## Mini-Roadmap 
- Better MCTS implementation
  - implement Go Text Protocol, so it can play against itself, GNUGO and in KGS. Helps to define the Engine API.
  - Basic Complete engine
  - Better implementation with MTCS (UCT)
  - Side-channel stats, i.e. current top-10 exploration trees, its odds, maybe visuals.
  - Time Management
  - Basic statical positional board for MCCS. i.e. D4 has a 1/8 chance of being played on 1st move, using games.
  - Introduce a NN to prune exploration tree (i.e. to pre-populate UCT tree) 
  - Simple Remote Service, so it can be distributed

- Aim: fun, practice Sympathetic coding in Java, implement > 1D at 19x19 

- Check about SuperKO rules here, we might need to revisit current impl: http://www.weddslist.com/kgs/past/superko.html
- Check "Mastering the game of Go without Human Knowledge" https://www.nature.com/articles/nature24270.epdf
- https://github.com/leela-zero/leela-zero
- http://www.lysator.liu.se/~gunnar/gtp/gtp2-spec-draft2/gtp2-spec.html

- GTP
  - Some of the verbs missing (see @Disabled tests)
  - Streaming wrappers
