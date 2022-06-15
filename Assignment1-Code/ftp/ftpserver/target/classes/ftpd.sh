#!/bin/bash
export JARFILE=/home/ec2-user/tmp/cs549/ftp-test/ftpd.jar
export POLICY=/home/ec2-user/tmp/cs549/ftp-test/server.policy
# export CODEBASE=file:/home/ec2-user/tmp/cs549/ftp-test/ftpd.jar
# export SERVERHOST=${server.machine}

if [ ! -e $JARFILE ] ; then
	echo "Missing jar file: $JARFILE"
	echo "Please assemble the ftpserver jar file."
	exit
fi

if [ ! -e $POLICY ] ; then
	pushd /home/ec2-user/tmp/cs549/ftp-test
	jar xf "$JARFILE" server.policy
	popd
fi

# echo "java -Djava.security.policy=$POLICY -Djava.rmi.server.codebase=$CODEBASE -Djava.rmi.server.hostname=$SERVERHOST -jar $JARFILE $*"
# java -Djava.security.policy=$POLICY -Djava.rmi.server.codebase=$CODEBASE -Djava.rmi.server.hostname=$SERVERHOST -jar $JARFILE $*

echo "java -Djava.security.policy=$POLICY -jar $JARFILE $*"
java -Djava.security.policy=$POLICY -jar $JARFILE $*
