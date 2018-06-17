#!/usr/bin/env bash
>stanford.txt

for ((i=1; i<=1000; i ++))
do
java -jar build/p4tester.jar stanford >>stanford.txt
done

>stanford_fast.txt

for ((i=1; i<=1000; i ++))
do
java -jar build/p4tester.jar stanford fast >>stanford_fast.txt
done

cat stanford.txt|grep Step1 >stanford_step1.txt
cat stanford.txt|grep Step2 >stanford_step2.txt
cat stanford.txt|grep Step3 >stanford_step3.txt
cat stanford.txt|grep Add >stanford_add.txt
cat stanford.txt|grep Remove >stanford_remove.txt
cat stanford_fast.txt|grep Add >stanford_add_fast.txt