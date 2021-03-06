#!/bin/bash

source=$1
if [ -n "$2" ]; then
    binary=$2
else
    binary=$source
fi

rm $binary.bin
g++ $source -o $binary.bin -O2 -std=c++0x -Wno-unused-result
