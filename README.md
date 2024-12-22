# PasswdDB
A simple server that stores password with server-side encryption and tenant specific private key

### Setup
```
git remote add origin https://github.com/Danny7226/PasswdDB.git
git branch -u origin/mainline master
git config --list

# (set local git config)
git config user.email "duochai9611@gmail.com"

# (push to remote [origin] branch [mainline])
git push origin HEAD:mainline
```


### Run
```
mvn package

java -jar target/PasswdDB-1.0-SNAPSHOT.jar 
```

or 

```
./start_server.sh
```
