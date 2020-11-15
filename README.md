# Stentor

A backend for a minimal blogging platform.

*Copyright (c) 2020 Axonibyte Innovations, LLC. All rights reserved.*

## Build

You need Java 11. This project can be tested and compiled with the following command.

`gradlew clean shadowJar`

## Execution

To run it, just do `java -jar build\libs\stentor.jar`.

You can optionally specify some command-line arguments.

|Short Param|Long Param     |Description                                         |Default                             |
|:----------|:--------------|:---------------------------------------------------|:-----------------------------------|
|-c         |--config-file  |Specifies the location of the configuration file.   |`NULL`                              |
|-d         |--database     |Specifies the target database server.               |127.0.0.1:27017                     |
|-k         |--preshared-key|Specifies the preshared key for authentication.     |484dd6d1-9262-4975-a707-4238e08ed266|
|-p         |--port         |Specifies the server's listening port.              |2586                                |

You should definitely change the security-related options.

In addition, there are two options that are used as one-time maintenance routines.
If either is specified, the backend stops after execution and does not spin up a server.

|Short Param|Long Param      |Description                                     |
|:----------|:---------------|:-----------------------------------------------|
|-a         |--add-admin     |Launches a prompt that adds an administrator.   |
|-r         |--reset-password|Launches a prompt that resets a user's password.|

## License

**This repository is subject to Version 2.0 of the [Apache License](https://apache.org/licenses/LICENSE-2.0).**
