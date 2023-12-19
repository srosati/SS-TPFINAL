#!/bin/sh

mvn exec:java -Dexec.mainClass="ar.edu.itba.ss.Main" -Dexec.args="$*"