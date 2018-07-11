#!/usr/bin/env bash

echo "Start Stanford*"
java -jar build/p4tester.jar -data stanford -print

echo "Start Stanford"
java -jar build/p4tester.jar -data stanford -print -priority

echo "Start Internet2*"
java -jar build/p4tester.jar -data internet2 -print

echo "Start Internet2"
java -jar build/p4tester.jar -data internet2 -print -priority