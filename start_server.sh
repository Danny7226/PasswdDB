echo "mvn packaging now"

mvn package

echo 'Running jar file'

java -jar target/PasswdDB-1.0-SNAPSHOT.jar 
