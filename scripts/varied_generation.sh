#!/usr/bin/env bash

echo "Start Stanford Rules"
>stanford.txt
for ((i=100; i<=3000; i += 100))
do
echo "Stanford Rule $i"
java -jar build/p4tester.jar -data stanford -max $i -print  >>stanford.txt
done

cat stanford.txt|grep Step1 >stanford_rules_step1.txt
cat stanford.txt|grep Step2 >stanford_rules_step2.txt
cat stanford.txt|grep Step3 >stanford_rules_step3.txt
cat stanford.txt|grep Probes >stanford_rules_probes.txt



echo "Start Stanford Routers"
>stanford.txt

for ((i=1; i<=15; i ++))
do
echo "Stanford Router $i"
java -jar build/p4tester.jar -data stanford -routers $i -print  >>stanford.txt
done
cat stanford.txt|grep Step1 >stanford_routers_step1.txt
cat stanford.txt|grep Step2 >stanford_routers_step2.txt
cat stanford.txt|grep Step3 >stanford_routers_step3.txt
cat stanford.txt|grep Probes >stanford_routers_probes.txt





echo "Start Internet2 Rules"
>internet2.txt
for ((i=1000; i<=14000; i += 1000))
do
echo "Internet2 Rule $i"
java -jar build/p4tester.jar -data internet2 -max $i -print >>internet2.txt
done

cat internet2.txt|grep Step1 >internet2_rules_step1.txt
cat internet2.txt|grep Step2 >internet2_rules_step2.txt
cat internet2.txt|grep Step3 >internet2_rules_step3.txt
cat internet2.txt|grep Probes >internet2_rules_probes.txt



echo "Start Internet2 Routers"
>internet2.txt

for ((i=1; i<=9; i ++))
do
echo "Internet2 Router $i"
java -jar build/p4tester.jar -data internet2 -routers $i -print >>internet2.txt
done
cat internet2.txt|grep Step1 >internet2_routers_step1.txt
cat internet2.txt|grep Step2 >internet2_routers_step2.txt
cat internet2.txt|grep Step3 >internet2_routers_step3.txt
cat internet2.txt|grep Probes >internet2_routers_probes.txt