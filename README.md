# SympatheticGo engine

Monte Carlo Mechanically Symphatetic Go Engine.
This aims to be hardware symphatetic, allocation & GC free, and run in L1.

Some ideas from http://mechanical-sympathy.blogspot.com and some libraries from https://github.com/OpenHFT.

Stats so far:

| Size| Processor| # Threads  | Ply/s | 1 Ply (avg)   |
|-----|----------|------------|-------|---------------|
| 9x9 | i7-8559U | 1          | 20k   | 50.769μs      |
| 9x9 | i7-8559U | 4 (=cores) | 64k   | 15.442μs      |
| 9x9 | i7-8559U | 8          | 82k   | 12.146μs      |


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
Running 3 concurrent threads with 9x9 games ...
   thrd.  2:  12308ms,  222222 plys,  18055.09 ply/s, 55.386μs/ply
   thrd.  0:  12311ms,  222222 plys,  18050.69 ply/s, 55.400μs/ply
   thrd.  1:  12323ms,  222222 plys,  18033.11 ply/s, 55.454μs/ply
------------- -------- ------------ ----------------- -------------
Agr. & avg.:  36942ms,  222222 plys,  18046.29 ply/s, 55.413μs/ply
Effectively:  12323ms,  666666 plys,  54099.33 ply/s, 18.485μs/ply
===================================================================
Running 4 concurrent threads with 9x9 games ...
   thrd.  3:  13701ms,  222222 plys,  16219.40 ply/s, 61.655μs/ply
   thrd.  0:  13703ms,  222222 plys,  16217.03 ply/s, 61.664μs/ply
   thrd.  2:  13707ms,  222222 plys,  16212.30 ply/s, 61.682μs/ply
   thrd.  1:  13724ms,  222222 plys,  16192.22 ply/s, 61.758μs/ply
------------- -------- ------------ ----------------- -------------
Agr. & avg.:  54835ms,  222222 plys,  16210.23 ply/s, 61.689μs/ply
Effectively:  13726ms,  888888 plys,  64759.43 ply/s, 15.442μs/ply
===================================================================
Running 5 concurrent threads with 9x9 games ...
   thrd.  2:  15837ms,  222222 plys,  14031.82 ply/s, 71.267μs/ply
   thrd.  0:  15895ms,  222222 plys,  13980.62 ply/s, 71.528μs/ply
   thrd.  1:  15903ms,  222222 plys,  13973.59 ply/s, 71.564μs/ply
   thrd.  4:  15949ms,  222222 plys,  13933.29 ply/s, 71.771μs/ply
   thrd.  3:  16056ms,  222222 plys,  13840.43 ply/s, 72.252μs/ply
------------- -------- ------------ ----------------- -------------
Agr. & avg.:  79640ms,  222222 plys,  13951.66 ply/s, 71.676μs/ply
Effectively:  16057ms, 1111110 plys,  69197.86 ply/s, 14.451μs/ply
===================================================================
Running 6 concurrent threads with 9x9 games ...
   thrd.  1:  17704ms,  222222 plys,  12552.08 ply/s, 79.668μs/ply
   thrd.  0:  17813ms,  222222 plys,  12475.27 ply/s, 80.159μs/ply
   thrd.  5:  17829ms,  222222 plys,  12464.08 ply/s, 80.231μs/ply
   thrd.  3:  17846ms,  222222 plys,  12452.20 ply/s, 80.307μs/ply
   thrd.  4:  18046ms,  222222 plys,  12314.20 ply/s, 81.207μs/ply
   thrd.  2:  18617ms,  222222 plys,  11936.51 ply/s, 83.777μs/ply
------------- -------- ------------ ----------------- -------------
Agr. & avg.: 107855ms,  222222 plys,  12362.26 ply/s, 80.891μs/ply
Effectively:  18618ms, 1333332 plys,  71615.21 ply/s, 13.964μs/ply
===================================================================
Running 7 concurrent threads with 9x9 games ...
   thrd.  6:  19811ms,  222222 plys,  11217.10 ply/s, 89.150μs/ply
   thrd.  0:  19828ms,  222222 plys,  11207.48 ply/s, 89.226μs/ply
   thrd.  1:  19876ms,  222222 plys,  11180.42 ply/s, 89.442μs/ply
   thrd.  3:  19879ms,  222222 plys,  11178.73 ply/s, 89.456μs/ply
   thrd.  2:  19901ms,  222222 plys,  11166.37 ply/s, 89.555μs/ply
   thrd.  4:  20029ms,  222222 plys,  11095.01 ply/s, 90.131μs/ply
   thrd.  5:  20089ms,  222222 plys,  11061.87 ply/s, 90.401μs/ply
------------- -------- ------------ ----------------- -------------
Agr. & avg.: 139413ms,  222222 plys,  11157.88 ply/s, 89.623μs/ply
Effectively:  20089ms, 1555554 plys,  77433.12 ply/s, 12.914μs/ply
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
