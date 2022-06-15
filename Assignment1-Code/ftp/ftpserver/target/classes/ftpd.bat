SET JARFILE=/home/ec2-user/tmp/cs549/ftp-test/ftpd.jar
REM To convert forward slash to back-slash for windows paths
SET "JARFILE=%JARFILE:/=\%"
SET POLICY=/home/ec2-user/tmp/cs549/ftp-test/server.policy
SET "POLICY=%POLICY:/=\%"
SET CODEBASE=file:/home/ec2-user/tmp/cs549/ftp-test/ftpd.jar
SET "CODEBASE=%CODEBASE:/=\%"
SET SERVERHOST=${server.machine}
SET "SERVERHOST=%SERVERHOST:/=\%"
SET TESTDIR=/home/ec2-user/tmp/cs549/ftp-test
SET "TESTDIR=%TESTDIR:/=\%"

if NOT EXIST %JARFILE% (
	echo "Missing jar file: %JARFILE%"
	echo "Please assemble the ftpserver jar file."
    EXIT 1
)

if NOT EXIST %POLICY% (
	pushd %TESTDIR%
	jar xf "%JARFILE%" server.policy
	popd
)

echo "Running server with CODEBASE=%CODEBASE% and SERVERHOST=%SERVERHOST%"
echo "java -Djava.security.policy=%POLICY% -Djava.rmi.server.codebase=%CODEBASE% -Djava.rmi.server.hostname=%SERVERHOST% -jar %JARFILE%"
java -Djava.security.policy=%POLICY% -Djava.rmi.server.codebase=%CODEBASE% -Djava.rmi.server.hostname=%SERVERHOST% -jar %JARFILE%
