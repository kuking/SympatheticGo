# SympatheticGo engine

Monte Carlo Mechanically Symphatetic Go Engine.
This aims to be hardware symphatetic, allocation & GC free, and run in L1.

Some ideas from http://mechanical-sympathy.blogspot.com and some libraries from https://github.com/OpenHFT.

Stats so far:

| Size| Processor     | # Threads  | Ply/s | 1 Ply (avg)   |
|-----|---------------|------------|-------|---------------|
| 9x9 | i7-8559U      | 1          | 20k   | 50.769μs      |
| 9x9 | i7-8559U      | 4 (=cores) | 64k   | 15.442μs      |
| 9x9 | i7-8559U      | 8          | 82k   | 12.146μs      |
| 9x9 | Ryzen 7 3800X | 1          | 21k   | 47.160μs      |  
| 9x9 | Ryzen 7 3800X | 8 (=cores) | 101k  | 9.816μs       |               
| 9x9 | Ryzen 7 3800X | 16         | 140k  | 7.133μs       |

The fact we get more processing from using hyper-threads needs further investigation.

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
   thrd.  0:  10879ms,  222222 plys,  20426.69 ply/s, 48.956μs/ply
------------- -------- ------------ ----------------- -------------
Agr. & avg.:  10879ms,  222222 plys,  20426.69 ply/s, 48.956μs/ply
Effectively:  11089ms,  222222 plys,  20039.86 ply/s, 49.901μs/ply
===================================================================
Running 2 concurrent threads with 9x9 games ...
   thrd.  0:  11029ms,  222222 plys,  20148.88 ply/s, 49.631μs/ply
   thrd.  1:  11086ms,  222222 plys,  20045.28 ply/s, 49.887μs/ply
------------- -------- ------------ ----------------- -------------
Agr. & avg.:  22115ms,  222222 plys,  20096.95 ply/s, 49.759μs/ply
Effectively:  11097ms,  444444 plys,  40050.82 ply/s, 24.968μs/ply
===================================================================
[...]
===================================================================
Running 16 concurrent threads with 9x9 games ...
   thrd. 10:  17700ms,  222222 plys,  12554.92 ply/s, 79.650μs/ply
   thrd.  5:  17705ms,  222222 plys,  12551.37 ply/s, 79.673μs/ply
   thrd. 15:  17705ms,  222222 plys,  12551.37 ply/s, 79.673μs/ply
   thrd.  8:  17704ms,  222222 plys,  12552.08 ply/s, 79.668μs/ply
   thrd. 11:  17698ms,  222222 plys,  12556.33 ply/s, 79.641μs/ply
   thrd.  3:  17730ms,  222222 plys,  12533.67 ply/s, 79.785μs/ply
   thrd. 12:  17770ms,  222222 plys,  12505.46 ply/s, 79.965μs/ply
   thrd.  1:  17746ms,  222222 plys,  12522.37 ply/s, 79.857μs/ply
   thrd.  7:  17780ms,  222222 plys,  12498.43 ply/s, 80.010μs/ply
   thrd. 14:  17766ms,  222222 plys,  12508.27 ply/s, 79.947μs/ply
   thrd.  6:  17774ms,  222222 plys,  12502.64 ply/s, 79.983μs/ply
   thrd.  2:  17772ms,  222222 plys,  12504.05 ply/s, 79.974μs/ply
   thrd.  4:  17803ms,  222222 plys,  12482.28 ply/s, 80.114μs/ply
   thrd.  0:  17792ms,  222222 plys,  12490.00 ply/s, 80.064μs/ply
   thrd.  9:  17785ms,  222222 plys,  12494.91 ply/s, 80.033μs/ply
   thrd. 13:  17912ms,  222222 plys,  12406.32 ply/s, 80.604μs/ply
------------- -------- ------------ ----------------- -------------
Agr. & avg.: 284142ms,  222222 plys,  12513.29 ply/s, 79.915μs/ply
Effectively:  17963ms, 3555552 plys, 197937.54 ply/s, 5.052μs/ply

```

## TODO
- Try to make adjacent crosses calculation to be a bitset instead of a list of coords.
- MCTS implementation
- SGF protocol

