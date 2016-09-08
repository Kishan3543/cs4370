### CSCI 4370 : Database Management  
### Fall 2016 : University of Georgia
### Project 1
=======

#### Background
=======

This project is built with `sbt` - can be installed with [Homebrew](https://brewformulas.org), a package manager on OS X. Information about installing on Windows can be found [here](http://www.scala-sbt.org/0.13/docs/Installing-sbt-on-Windows.html).
```
$ brew install sbt
```  

`cd` to your local directory that will hold the project.  
```
$ cd /path/to/dir
```
Create the directory structure for the `sbt` project.
```
$ mkdir -p src/main/java
$ mkdir -p src/main/scala
$ mkdir -p src/main/resources
$ mkdir -p test/main/java
$ mkdir -p test/main/scala
$ mkdir -p test/main/resources
```
Make your `build.sbt` file. The simplest version is below:
```
name := "project2"
version := "0.0.0"
```

Enter the `sbt` shell.
```
$ sbt compile
$ sbt run
```

Assess any errors at the command line(`sbt` happens to be pretty verbose).