# Stentor

A backend for a minimal blogging platform.

*Copyright (c) 2020-2021 Axonibyte Innovations, LLC. All rights reserved.*

## Build

You need Java 11. This project can be tested and compiled with the following command.

`gradlew clean shadowJar`

## Configuration

The configuration file is represented as a JSON object.

|Path               |Datatype |Description                                                                                                                           |
|:------------------|:--------|:-------------------------------------------------------------------------------------------------------------------------------------|
|.database          |object   |Optional. Denotes database metadata.                                                                                                  |
|.database.protocol |string   |Optional. Denotes connection protocol, without the `://` suffix. Must be `mongodb` (default) or `mongodb+srv`.                        |
|.database.host     |string   |Optional. Denotes mongodb host. Defaults to `127.0.0.1`.                                                                              |
|.database.port     |integer  |Optional. Denotes `27017`. Ineffective if protocol is `mongodb+srv`.                                                                  |
|.database.username |string   |Required if `.database.password` is specified. Denotes the username for the database.                                                 |
|.database.password |string   |Optional, but generally used in conjunction with `.database.username`. Denotes the database password.                                 |
|.database.database |string   |Optional. Denotes the name of the database. Defaults to `stentor`.                                                                    |
|.database.secure   |boolean  |Optional. Should be `true` if TLS is to be used. Defaults to `false`.                                                                 |
|.auth              |object   |Optional. Denotes authentication metadata.                                                                                            |
|.auth.salt         |string   |Optional. Denotes the user password salt. Defaults to `0a486beb-d953-4620-95c7-c99689fb228b`.                                         |
|.net               |object   |Optional. Denotes network metadata.                                                                                                   |
|.net.psk           |string   |Optional. Denotes the preshared key for network session keys. Defaults to `484dd6d1-9262-4975-a707-4238e08ed266`.                     |
|.net.port          |integer  |Optional. Denotes the port that the API will bind to. Defaults to `2586`.                                                             |
|.net.truststore    |string   |Required if `.net.trustpass` is specified. Denotes the location of the TrustStore. Probably required if `.database.secure` is `true`. |
|.net.trustpass     |string   |Optional, but generally used in conjunction with `.net.truststore`. Denotes the TrustStore password.                                  |

## Execution

To run it, just do `java -jar build\libs\stentor.jar`.

You can optionally specify some command-line arguments.

|Short Param|Long Param     |Description                                         |Default                             |
|:----------|:--------------|:---------------------------------------------------|:-----------------------------------|
|-c         |--config-file  |Specifies the location of the configuration file.   |`NULL`                              |
|-d         |--debug        |Enables debug logging.                              |`false`                             |

You should definitely change the security-related options.

In addition, there are two options that are used as one-time maintenance routines.
If either is specified, the backend stops after execution and does not spin up a server.

|Short Switch|Long Switch      |Description                                      |
|:-----------|:----------------|:------------------------------------------------|
|-a          |--add-admin      |Launches a prompt that adds an administrator.    |
|-r          |--reset-password |Launches a prompt that resets a user's password. |

## License

**This repository is subject to Version 2.0 of the [Apache License](https://apache.org/licenses/LICENSE-2.0).**
