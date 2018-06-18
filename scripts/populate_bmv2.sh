#!/bin/bash

SWITCH_DIR=/home/netarchlab/behavioral-model/targets/simple_switch

cp ../resource/Stanford_backbone/*_commands.txt $SWITCH_DIR

cd $SWITCH_DIR

./runtime_CLI --thrift-port 9090 <bbra_commands.txt
./runtime_CLI --thrift-port 9091 <boza_commands.txt
./runtime_CLI --thrift-port 9092 <coza_commands.txt
./runtime_CLI --thrift-port 9093 <goza_commands.txt
./runtime_CLI --thrift-port 9094 <poza_commands.txt
./runtime_CLI --thrift-port 9095 <roza_commands.txt
./runtime_CLI --thrift-port 9096 <soza_commands.txt
./runtime_CLI --thrift-port 9097 <yoza_commands.txt
