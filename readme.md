# Setup docker on virtual machine
1. Install the VirtualBox with selected *Skip unattended installation*
2. Download the Linux version you need from https://ubuntu.com/download/desktop
3. Create virtual machine with downloaded iso image
    - 6GB RAM
    - 32GB disk storage
    - 64MB video memory
    - set network as bridge
4. Run machine and install system
5. ***$ sudo apt-get update***
6. ***$ sudo apt install docker.io***
7. ***$ sudo snap install docker***
8. ***$sudo usermod -aG docker $USER***
9. Log out and log back in## so that your group membership is re-evaluated
10. Verify that you can run docker commands without sudo ***$ docker run hello-world***
11. Configure Docker to be accessed remotely
    - Create config file */etc/systemd/system/docker.service.d/override.conf*
    - Add content to file - docker will be listened on 2376 port
    ```
    # /etc/systemd/system/docker.service.d/override.conf
    
    [Service]
    
    ExecStart=
    
    ExecStart=/usr/bin/dockerd -H fd:// -H tcp://0.0.0.0:2376
    ```
    - ***systemctl daemon-reload***
    - ***systemctl restart docker.service***
    - Check your Docker daemon - ***systemctl status docker.service*** - You should see something like: /usr/bin/dockerd -H fd:// -H tcp://0.0.0.0:2376
12. Configure docker host for testcontainers
    - Get ip of your virtual machine eg. ifconfig
    - Create file c:/users/userName/.testcontainers.properties with content
    ```
    docker.host=tcp\://IP OF YOUR VM\:2376
    ```
13. You are ready to go