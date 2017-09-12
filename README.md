# Content Server 

[![Build Status](https://travis-ci.org/MarcoLotz/ContentServer.svg?branch=master)](https://travis-ci.org/cytosm/cytosm.svg?branch=master)

Have you ever desired to easily visualize the photos and videos stored in your laptop from any device with a browser?

Well, that's why content server was created! You can run this application in your laptop and watch all the videos that you previously recorded from an iPad or a mobile.

Content server is an open-source server-application that allows you to securely visualize, download and ultimately stream their content on a browser.

## Installation

There are two easy ways to run the Content Server: using SBT or by deploying a docker-container. The current version of the server accepts some configuration, that will described later in this document.

### How to configure

There are two main ways to configure the application. The first one is through a JSON file and the second one is by using command-lin parameters.

#### JSON configuration

The JSON configuration file must be located in the /conf folder. Any command line argument will bypass the configuration here contained.

```json
{
  "mountPath": "/path/to/be/mounted",
  "tempDirectory": "/path/to/temp/dir",
  "enableUserAuthentication": false,
  "port": 8080,
  "username": "marco",
  "password": "abc",
  "showHiddenFiles": false,
  "filteredoutExtensions": [
    "md"
  ]
}
```

***mounthPath*** specifies the path where the content server is going to start serving. It serves all files contained in that directory, recursively.

***tempDirectory*** is the path where all compressed temporary data is going to be stored. When a directory is downloaded, first it is compressed, then stored on this path and then served to the used. The contents on this directory are cleaned then the application exits.

***enableUserAuthentication*** enables the login section with a username and password. The username and password are the ones provided in the ***username*** and ***password*** keys.

***port*** is the port exposes by the content server for browser login.

***showHiddenFiles*** filters files that start with "."

***filteredoutExtesions*** prevents files with the extension types from the list from being served by the Content Server.

#### Command line parameters

All the options available in the JSON configuration are also available through command line. Any command line argument has precedence over the JSON content.
To see the command line usage, run the following command:

```shell
sbt "run --help"
```

### Deploy
#### SBT

Simply clone this repo from gitHub and execute is content by running the following command inside the directory:

```shell
sbt container:start
```

Please bear in mind that this approach requires you to have sbt and scala installed in the machine, aside from java.

##### SBT configuration

SBT accepts configurations on either the JSON file (available in the conf folder), or through command line parameters.
In order to configure through command line parameters, it should be executed as 

```shell
sbt "run [parameters]"
```

#### Docker container

The docker container approach is more straight-forward, since it only requires docker to be installed. The container clones this repository and run the sbt application.
In order to deploy a container running this application, simply run:

```shell
docker run -v [Absolute path of directory to mount]:/root/mount -p [port number to be exposed on host]:8080 marcolotz/contentserver:latest sbt "run -p 8080 -m /root/mount -t /root/tmp"
```

Bear in mind that, as mentioned [here](https://forums.docker.com/t/can-i-change-the-default-ip-from-0-0-0-0-when-binding/30358), by default you will be able to reach the content server on ***[host machine ip]*** : ***[mapped port]*** , instead of default 127.0.0.1 loopback ip.
To debug the output of the application, use the docker flags -it.
##### Docker configuration

To configure the docker deployment, it is expected for the user to provide command line arguments.

## Under the hood
Content server is mostly implemented in Scala, with the Scalatra and Scalate libraries. The command line parsing is done through Scopt.