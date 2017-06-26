# Installation manual

The Global Registry (GReg) is a decentralized directory service, linking globally unique user identifiers (GUIDs) to a list of user accounts owned by a user. By resolving a GUID, services can retrieve information about the existence and location of all registered user accounts for a specific user and connect to the respective account's Domain Registry. The Global Registry is built in Java using Spring Boot and relies on decentralized control by utilizing DHT technology.

On a Debian-based system, execute the following commands to build GReg

- Install maven and git:
```
sudo apt-get update && sudo apt-get dist-upgrade
sudo apt-get -y install maven git
```
- Install Docker (https://docs.docker.com/engine/installation/linux/debian/):
```
sudo apt-get install -y apt-transport-https ca-certificates curl software-properties-common
curl -fsSL https://apt.dockerproject.org/gpg | sudo apt-key add -
sudo add-apt-repository "deb https://apt.dockerproject.org/repo/ debian-$(lsb_release -cs) main"
sudo apt-get -y install docker-engine
```
- Compile GReg:
```
cd /var
sudo mkdir greg
cd greg
sudo git clone https://github.com/reTHINK-project/dev-registry-global.git
sudo git checkout tags/0.3.4
sudo mvn clean
sudo mvn install
docker build -t rethink/greg:0.3.4 .
```
- Run GReg:
```
docker run -d -p 5001:5001/tcp -p 5001:5001/udp -p 5002:5002/tcp rethink/greg:0.3.4
```
