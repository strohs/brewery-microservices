MySQL with DOCKER
=============================
* mysql hub page is [here](https://hub.docker.com/_/mysql)

- most basic start
> docker run --name mysql-dev -p 3306:3306 -e MYSQL_ROOT_PASSWORD=password -d mysql:latest

* starting a new mysql instance with a root password, mapping port (3306:3306), with a user and password, and with a default database created named "blogen", and with the container named mysqldb
> docker run --name mysqldb -p 3306:3306 -e MYSQL_ROOT_PASSWORD=password -e MYSQL_USER=user -e MYSQL_PASSWORD=password -e MYSQL_DATABASE=blogen -d mysql:tag
	* where tag is some id like 'latest'
	* to create another user and password (in addition to root user):
	> -e MYSQL_USER=user -e MYSQL_PASSWORD=password -e MYSQL_DATABASE=blogen

* if you need to persist database data on your localhost:
	1. create a data directory `~/mysqldata`
	2. start docker with the volume flag:
		`docker run --name some-mysql -v ~/mysqldata/mysql -e MYSQL_ROOT_PASSWORD=my-secret-pw -d mysql:tag`

* to get a bash shell inside the container:
> docker exec -it some-mysql bash

* MySQL server logs are at
> docker logs some-mysql

* to see complete list of configuration options for MySQL run:
> docker run -it --rm mysql:tag --verbose --help

* to remove a container and its volumes
> docker rm -v <CONTAINER-NAME>


## MySQL command line commands:
1. get bash shell of container running mysql
2. log in as root
        mysql -u root -p
3. you will now be in mysql command prompt `mysql>`

* creating a database
> CREATE DATABASE javabase DEFAULT CHARACTER SET utf8 COLLATE utf8_unicode_ci;

* grant all permissions to a database, for a user at a specific host:
> GRANT ALL ON database-name-here.* TO 'your_mysql_name'@'your_client_host';

> show grants for 'user';


## Example commands
### MySQL Employee database (using a volume)
1. docker run --name mysql-dev -p 3306:3306 -v ~/mysql-projects/nodejs_shop_db:/mnt/nodejs_shop_db -e MYSQL_ROOT_PASSWORD=password -e MYSQL_USER=user -e MYSQL_PASSWORD=password -d mysql:latest
2. first time setup requires you to log into mysql command line and set up the employee database manually

OR
* use a prebuilt employees database container:
docker run -d --name mysql-employees -p 3306:3306 -e MYSQL_ROOT_PASSWORD=password -v ~/databases:/var/lib/mysql genschsa/mysql-employees


## Initializing a fresh instance
Docker mysql container will execute files with extensions `.sh, .sql, .sql.gz` that are found
in `/docker-entrypoint-initdb.d`. You will have to volume mount your scripts on the host to that directory in the
container.
> -v /yourscript:/docker-entrypoint-initdb.d