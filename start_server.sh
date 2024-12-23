echo "mvn packaging now"

mvn package

echo 'Running jar file'

# run server at port 8080
java -jar target/PasswdDB-1.0-SNAPSHOT.jar 8080
