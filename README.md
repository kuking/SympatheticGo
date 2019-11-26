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
   thrd.  0:  10480ms,  222222 plys,  21204.39 ply/s, 47.160μs/ply
------------- -------- ------------ ----------------- -------------
Agr. & avg.:  10480ms,  222222 plys,  21204.39 ply/s, 47.160μs/ply
Effectively:  10694ms,  222222 plys,  20780.06 ply/s, 48.123μs/ply
===================================================================
Running 2 concurrent threads with 9x9 games ...
   thrd.  0:  10832ms,  222222 plys,  20515.32 ply/s, 48.744μs/ply
   thrd.  1:  11221ms,  222222 plys,  19804.12 ply/s, 50.495μs/ply
------------- -------- ------------ ----------------- -------------
Agr. & avg.:  22053ms,  222222 plys,  20153.45 ply/s, 49.619μs/ply
Effectively:  11230ms,  444444 plys,  39576.49 ply/s, 25.268μs/ply
===================================================================
[...]
==================================================================
Running 16 concurrent threads with 9x9 games ...
   thrd.  7:  25041ms,  222222 plys,   8874.33 ply/s, 112.685μs/ply
   thrd.  9:  25060ms,  222222 plys,   8867.60 ply/s, 112.770μs/ply
   thrd. 12:  25057ms,  222222 plys,   8868.66 ply/s, 112.757μs/ply
   thrd. 14:  25051ms,  222222 plys,   8870.78 ply/s, 112.730μs/ply
   thrd.  2:  25043ms,  222222 plys,   8873.62 ply/s, 112.694μs/ply
   thrd.  8:  25060ms,  222222 plys,   8867.60 ply/s, 112.770μs/ply
   thrd.  5:  25050ms,  222222 plys,   8871.14 ply/s, 112.725μs/ply
   thrd.  1:  25075ms,  222222 plys,   8862.29 ply/s, 112.838μs/ply
   thrd. 10:  25087ms,  222222 plys,   8858.05 ply/s, 112.892μs/ply
   thrd.  0:  25061ms,  222222 plys,   8867.24 ply/s, 112.775μs/ply
   thrd. 11:  25096ms,  222222 plys,   8854.88 ply/s, 112.932μs/ply
   thrd.  3:  25074ms,  222222 plys,   8862.65 ply/s, 112.833μs/ply
   thrd. 13:  25113ms,  222222 plys,   8848.88 ply/s, 113.009μs/ply
   thrd.  6:  25205ms,  222222 plys,   8816.58 ply/s, 113.423μs/ply
   thrd.  4:  25187ms,  222222 plys,   8822.88 ply/s, 113.342μs/ply
   thrd. 15:  25305ms,  222222 plys,   8781.74 ply/s, 113.873μs/ply
------------- -------- ------------ ----------------- -------------
Agr. & avg.: 401565ms,  222222 plys,   8854.24 ply/s, 112.940μs/ply
Effectively:  25360ms, 3555552 plys, 140203.15 ply/s, 7.133μs/ply
```
