#!/bin/bash

username="rethink"
installpath="/usr/local/gReg"

# require the executing user to have root privileges
if [ "$(id -u)" != "0" ]
then
   echo "This script must be run as root" 1>&2
   exit 1
fi

echo "installing dependency jsvc..."
apt-get -y update && apt-get install -y jsvc

echo "copying resources..."
cp -R gReg $installpath

echo "setting up user $username..."
myuser="$(getent passwd $username)"
echo $myuer
if [[ -z $myuser ]]
then
        adduser $username --gecos "" --no-create-home --disabled-login
else
        echo "user $username already exists"
fi

echo "setting access permissions..."
chown -R $username $installpath

echo "starting service..."
service greg stop && service greg start
