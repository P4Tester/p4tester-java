#!/usr/bin/env bash
>internet2.txt

for ((i=1; i<=1000; i ++))
do
java -jar build/p4tester.jar internet2 >>internet2.txt
done

>internet2_fast.txt

for ((i=1; i<=1000; i ++))
do
java -jar build/p4tester.jar -data internet2 -update -fast >>internet2_fast.txt
done

cat internet2.txt|grep Step1 >internet2_step1.txt
cat internet2.txt|grep Step2 >internet2_step2.txt
cat internet2.txt|grep Step3 >internet2_step3.txt
cat internet2.txt|grep Add >internet2_add.txt
cat internet2.txt|grep Remove >internet2_remove.txt
cat internet2_fast.txt|grep Add >internet2_add_fast.txt
