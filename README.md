# SympatheticGo engine

Monte Carlo Mechanically Symphatetic Go Engine.
This aims to be hardware symphatetic, allocation & GC free, and run in L1.

Some ideas from http://mechanical-sympathy.blogspot.com and some libraries from https://github.com/OpenHFT.

```
$ sysctl -n machdep.cpu.brand_string
Intel(R) Core(TM) i7-8559U CPU @ 2.70GHz

$ ./gradlew run
===================================================================
Running 1 concurrent threads with 9x9 games ...
   thrd.  0:  10942ms,  222222 plys,  20309.08 ply/s, 49.239μs/ply
------------- -------- ------------ ----------------- -------------
Agr. & avg.:  10942ms,  222222 plys,  20309.08 ply/s, 49.239μs/ply
Effectively:  11282ms,  222222 plys,  19697.04 ply/s, 50.769μs/ply
===================================================================
Running 2 concurrent threads with 9x9 games ...
   thrd.  1:  11384ms,  222222 plys,  19520.56 ply/s, 51.228μs/ply
   thrd.  0:  11393ms,  222222 plys,  19505.13 ply/s, 51.269μs/ply
------------- -------- ------------ ----------------- -------------
Agr. & avg.:  22777ms,  222222 plys,  19512.84 ply/s, 51.248μs/ply
Effectively:  11395ms,  444444 plys,  39003.42 ply/s, 25.639μs/ply
===================================================================
[...]
===================================================================
Running 8 concurrent threads with 9x9 games ...
   thrd.  2:  21409ms,  222222 plys,  10379.84 ply/s, 96.341μs/ply
   thrd.  3:  21452ms,  222222 plys,  10359.03 ply/s, 96.534μs/ply
   thrd.  1:  21483ms,  222222 plys,  10344.09 ply/s, 96.674μs/ply
   thrd.  5:  21484ms,  222222 plys,  10343.60 ply/s, 96.678μs/ply
   thrd.  4:  21485ms,  222222 plys,  10343.12 ply/s, 96.683μs/ply
   thrd.  6:  21482ms,  222222 plys,  10344.57 ply/s, 96.669μs/ply
   thrd.  7:  21502ms,  222222 plys,  10334.95 ply/s, 96.759μs/ply
   thrd.  0:  21592ms,  222222 plys,  10291.87 ply/s, 97.164μs/ply
------------- -------- ------------ ----------------- -------------
Agr. & avg.: 171889ms,  222222 plys,  10342.58 ply/s, 96.688μs/ply
Effectively:  21592ms, 1777776 plys,  82334.94 ply/s, 12.146μs/ply
```

```
$ lscpu | grep "Model name"
Model name:          AMD Ryzen 7 3800X 8-Core Processor

$ ./gradlew run
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

## TODO
- Better MCTS implementation

