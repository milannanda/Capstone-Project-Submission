
# Auralyse Machine Learning Application

This is the source code of the machine learning app. It is a Kivy Python application that requires Buildozer to be compiled to an Android Application.

Buildozer can be installed on MacOS and Ubuntu. Windows 10+ users are able to use the Windows Subsystem for Android to run Buildozer.

Install Buildozer as below or read the [Official Documentation](https://buildozer.readthedocs.io/en/latest/installation.html)

```
pip3 install --user --upgrade buildozer

sudo apt update
sudo apt install -y git zip unzip openjdk-17-jdk python3-pip autoconf libtool pkg-config zlib1g-dev libncurses5-dev libncursesw5-dev libtinfo5 cmake libffi-dev libssl-dev
pip3 install --user --upgrade Cython==0.29.33 virtualenv

export PATH=$PATH:~/.local/bin/
```
To compile, run: ```buildozer -v android debug```

To change architecture or dependencies, change the appropriate settings in the ```buildozer.spec``` file.

~~I have included a .buildozer zip file. This includes some dependencies converted from x86_64 to AArch64 when extracted.~~ The zip was too large to upload to GitHub

