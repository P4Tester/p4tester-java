#!/usr/bin/env bash

echo "Start Stanford"
>stanford.txt

for ((i=1; i<=1000; i ++))
do
echo "Stanford $i"
java -jar build/p4tester.jar -data stanford -update -print -priority  >>stanford.txt
done

>stanford_fast.txt

for ((i=1; i<=1000; i ++))
do
echo "Stanford Fast $i"
java -jar build/p4tester.jar -data stanford -update -fast -print -priority >>stanford_fast.txt
done

cat stanford.txt|grep Step1 >stanford_update_step1_p.txt
cat stanford.txt|grep Step2 >stanford_update_step2_p.txt
cat stanford.txt|grep Step3 >stanford_update_step3_p.txt
cat stanford.txt|grep Add >stanford_update_add_p.txt
cat stanford.txt|grep Remove >stanford_update_remove_p.txt
cat stanford_fast.txt|grep Fast >stanford_update_add_fast_p.txt


echo "Start Internet2"
>internet2.txt

for ((i=1; i<=1000; i ++))
do
echo "Internet2 $i"
java -jar build/p4tester.jar -data internet2 -update -print -priority >>internet2.txt
done

>internet2_fast.txt

for ((i=1; i<=1000; i ++))
do
echo "Internet2 Fast $i"
java -jar build/p4tester.jar -data internet2 -update -fast -print -priority  >>internet2_fast.txt
done

cat internet2.txt|grep Step1 >internet2_update_step1_p.txt
cat internet2.txt|grep Step2 >internet2_update_step2_p.txt
cat internet2.txt|grep Step3 >internet2_update_step3_p.txt
cat internet2.txt|grep Add >internet2_update_add_p.txt
cat internet2.txt|grep Remove >internet2_update_remove_p.txt
cat internet2_fast.txt|grep Fast >internet2_update_add_fast_p.txt