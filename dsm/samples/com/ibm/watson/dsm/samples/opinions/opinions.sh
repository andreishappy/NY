#!/bin/bash
# This script starts 3 engines all with the same rules file and then
# simulates the engines making observations of their neighbors.
# The observations are processed through the rules files and
# aggregated with observations from neighboring nodes.  All
# 3 engines are neighbors of each other.
# Usage: ./opinions.sh
############################################################################
rules=$DSM_HOME/samples/com/ibm/watson/dsm/samples/opinions/opinions.dsmr
for i in a b c; do
   dsmengine -Dcom.ibm.watson.dsm.engine.level=FINE \
	-namespace opinions -instance $i \
	$rules > engine-$i.out 2>&1 &
done
echo -n Waiting for engines to come up...
sleep 10	# Wait for engines to come up and discover each other
echo done.
# Set a's observation of b as .3
echo
echo Insert observations for instance a
tuple insert opinions a my_observations node=b opinion=.3
# Set b's observation of a as .6
echo
echo Insert observations for instance b
tuple insert opinions b my_observations node=a opinion=.6
# Set c's observation of a as .8
echo
echo Insert observations for instance c
tuple insert opinions c my_observations node=a opinion=.8
echo 
echo Waiting for engines to exchange the last bits of info
sleep 3
# Kill the engines
pids=`ps -elf | grep DSMEngine | grep -v grep | awk '{print $4}'`
kill -9 $pids
