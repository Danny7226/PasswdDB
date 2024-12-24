# SecretDB
A simple server that stores secrets with server-side encryption using tenant-specified private key;

For security, either use it within your localhost, or always use **https** when running this server on a remote desktop

### Background
This project sets/spins up a servlet container service that stores encrypted secrets on disk.

For fun, it doesn't use Spring frameworks, but vanilla tomcat servlet container with self-registered servlets

### Key functional features in this service
* Get/List/POST apis
* Persistent logs to dedicated log files
* Scheduled workers that backs up datafile once every day

### Key technical features in this service
* Tomcat 10.x
* OpenJDK21
* Compile-time IoC with Google Dagger
* Log4j2 2.24.3

### Spin up server
If you don't have jdk21 installed

```
wget https://download.oracle.com/java/21/latest/jdk-21_linux-x64_bin.tar.gz

sudo tar jdk-21_linux-x64_bin.tar.gz -C /opt/
sudo tar -xvzf jdk-21_linux-x64_bin.tar.gz -C /opt/

export JAVA_HOME=/opt/jdk-21
export PATH=$JAVA_HOME/bin:$PATH
```

```
mvn package

java -jar target/SecretDB-1.0-SNAPSHOT.jar $PORT_NUMBER
```

or 

```
./start_server.sh
```

### APIs
```
# to list
curl "http://localhost:8080/api/list/{tenant_id}" 

# to list and grep
curl "http://localhost:8080/api/list/{tenant_id}" | grep -i "name" 
curl -s "http://localhost:8080/api/list/{tenant_id}" | grep -i "name" 

# to get
curl "http://localhost:8080/api/{tenant_id}?name={name}&key={key}" 

# to write
curl -i -X POST http://localhost:8080/api/duochai -H "Content-Type: application/json" -d '{"key":"key1", "name":"name", "value": "value"}'
```

### Debug
* `jar tf target/SecretDB-1.0-SNAPSHOT.jar` to check all classes packaged in jar

### Contribute
```
git remote add origin https://github.com/Danny7226/SecretDB.git
git branch -u origin/mainline master
git config --list

# (set local git config)
git config user.email "duochai9611@gmail.com"

# (push to remote [origin] branch [mainline])
git push origin HEAD:mainline
```