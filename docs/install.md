# Installation manual

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
sudo git checkout tags/0.3.2
sudo mvn clean
sudo mvn install
docker build -t rethink/greg:0.3.2 .
```
- Run GReg:
```
docker run -d –p 5001:5001/tcp -p 5001:5001/udp –p 5002:5002/tcp rethink/greg:0.3.2
```
