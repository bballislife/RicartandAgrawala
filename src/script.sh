#! /bin/bash
# start the rmiregistry server

# compile all the necessary java files
# javac *.java
if [ "$#" -ne 4 ];
then
  echo "Incorrect number of arguments"
  exit 1
fi
# rmiregistry -J-Djava.rmi.server.useCodebaseOnly=false &
numConnections=$2
tot=$numConnections
let "tot -= 1"
echo $tot
outfile=$4
for i in $(seq 0 $tot);
do
  java -cp /home/dj_hunter/Documents/sem_5/distributed_systems/assignments/Assignment2/JavaApplication6/src -Djava.rmi.server.codebase=file:/home/dj_hunter/Documents/sem_5/distributed_systems/assignments/Assignment2/JavaApplication6/src/ RicartAgrawala.RicartAgrawala -i $i -n $numConnections -o $outfile &
done
# killall rmiregistry
