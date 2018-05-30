#!/bin/sh

if [ "$#" -ge "2" ]; then
    echo "Error: please insert only one parameter."
    echo "Usage:\n - ./dsquack.sh [quiet] : redirects log in a new file in log directory.\n - ./dsquack.sh noisy   : prints log in std output."
    exit
fi

OPT=quiet
if [ "$#" -eq "1" ]; then
    OPT="$1"
fi
if [ "x$OPT" != "xnoisy" ] && [ "x$OPT" != "xquiet" ]; then
    echo "Error: invalid parameter."
    echo "Usage:\n - ./dsquack.sh [quiet] : redirects log in a new file in log directory.\n - ./dsquack.sh noisy   : prints log in std output."
    exit
fi

if [ "x$OPT" = "xnoisy" ]; then
    java -jar project/classes/artifacts/deployDS_jar/app.jar
else
    NAME="log-"$(date)
    java -jar project/classes/artifacts/deployDS_jar/app.jar > log/$NAME
fi
