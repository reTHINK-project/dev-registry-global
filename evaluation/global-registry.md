## Description of Component ##

The Global Registry is a decentralized directory service, linking globally unique user identifiers (GUIDs) to a list of user accounts owned by a user.
By resolving a GUID, services can retrieve information about the existence and location of all registeres user accounts for a specific user and connect to the respective account's Domain Registry.
The Global Registry is built in Java using Spring Boot and relys on decentralized control by utinizing DHT technology.

## Methodology and setup ##

For the evaluation of the Global Registry, three separate nodes were set up and connected to each other.
Each node consisted of a virtual server managed by VMware with 1 CPU, 1 GB of RAM, and 10 GB of disk space.
The used operating system was Debian Jessie with a 3.16.0 kernel, while the Global Registry daemon was dockerized using Docker 17.03.1-ce.

## Conformance metrics ##

## Performance metrics ##

The conducted tests evaluate the performance of the Global Registry network unter high load.

## Conclusions and recommendations ##
