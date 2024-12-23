# PasswdDB
A simple server that stores password with server-side encryption using tenant-specified private key;

For security, either use it within your localhost, or always use **https** when running this server on a remote env

### Background
This project is about a servlet container service that stores encrypted password on disk.
For fun, it doesn't use Spring frameworks, but vanilla tomcat servlet container with self-registered servlets

### Spin up server
```
mvn package

java -jar target/PasswdDB-1.0-SNAPSHOT.jar 
```

or 

```
./start_server.sh
```

### APIs
* List
```
# to list
curl "http://localhost:8080/api/list/{tenant_id}" 

# to get
curl "http://localhost:8080/api/{tenant_id}?name={name}&key={key}" 

# to write
curl -i -X POST http://localhost:8080/api/duochai -H "Content-Type: application/json" -d '{"key":"key1", "name":"name", "value": "value"}'
```

### Debug
* `jar tf target/PasswdDB-1.0-SNAPSHOT.jar` to check all classes packaged in jar

### Contribute
```
git remote add origin https://github.com/Danny7226/PasswdDB.git
git branch -u origin/mainline master
git config --list

# (set local git config)
git config user.email "duochai9611@gmail.com"

# (push to remote [origin] branch [mainline])
git push origin HEAD:mainline
```