#!/bin/bash

SWITCH_DIR=/home/netarchlab/behavioral-model/targets/simple_switch

LOG='-L off'

cp ../tests/router.json $SWITCH_DIR

cd $SWITCH_DIR

for i in `seq 1`
do
{
    sudo ./simple_switch router.json $LOG  --device-id 0 -i 0@s0_0 -i 1@s0_1 -i 2@s0_2 -i 3@s0_3 -i 4@s0_4 -i 5@s0_5 -i 6@s0_6 -i 7@s0_7 --thrift-port 9090 >1.txt
} &
done

for i in `seq 1`
do
{
    sudo ./simple_switch router.json --device-id 1 -i 0@s1_0 $LOG --thrift-port 9091 >2.txt
} &
done

for i in `seq 1`
do
{
    sudo ./simple_switch router.json --device-id 2 -i 0@s2_0 $LOG --thrift-port 9092 >3.txt
} &
done

for i in `seq 1`
do
{
    sudo ./simple_switch router.json --device-id 3 -i 0@s3_0 $LOG --thrift-port 9093 >4.txt
} &
done

for i in `seq 1`
do
{
    sudo ./simple_switch router.json --device-id 4 -i 0@s4_0 $LOG --thrift-port 9094 >5.txt
} &
done

for i in `seq 1`
do
{
    sudo ./simple_switch router.json --device-id 5  -i 0@s5_0 $LOG --thrift-port 9095 >6.txt
} &
done


for i in `seq 1`
do
{
    sudo ./simple_switch router.json --device-id 6 -i 0@s6_0 $LOG --thrift-port 9096 >7.txt
} &
done

sudo ./simple_switch router.json --device-id 7 -i 0@s7_0 $LOG --thrift-port 9097
