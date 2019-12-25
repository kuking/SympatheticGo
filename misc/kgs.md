
```
$ curl -s https://www.u-go.net/gamerecords/ | grep -oP '(https://.*bz2)' | xargs wget {}
$ # use ggrep in osx
$ for i in `ls *.bz2`; do tar -xvjf $i; done
```