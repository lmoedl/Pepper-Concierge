# Concierge behaviour with humanoid robot Pepper at Hochschule Furtwangen University (HFU)
Pepper is an humanoid robot 🤖 from [Aldebaran](https://www.ald.softbankrobotics.com/en). Pepper can be programmed using it's SDK. This repository contains all project files for the concierge behaviour guiding through the Smart Home Lab of Hochschule Furtwangen University.

A first impression can be found [here](https://youtu.be/ARXpej7blrY)📹

## Usage
This Software is written in Java. It includes all the SDKs for Mac, Linux and Windows to interact with Pepper. The movements and other stuff like the MQTT-Broker etc. only fits the setup at HFU. You need to adjust these parameters to fit your needs.

Clone or download this project. Inside the folder [Software/Pepper](https://github.com/lmoedl/MOS-Projekt/tree/master/Software/Pepper) the real concierge software can be found. Compile and run or you use the precompiled jar at [store folder](https://github.com/lmoedl/MOS-Projekt/tree/master/Software/Pepper/store).

The folder [Webserver-Files](https://github.com/lmoedl/MOS-Projekt/tree/master/Software/Webserver-Files) contains a ready to use web project for voting at anything. You can see the website in the video above on Peppers tablet. It can be deployed on any server as well on an Raspberry Pi.

The folder [Pepper-Raspi](https://github.com/lmoedl/MOS-Projekt/tree/master/Software/Pepper-Raspi) contains a ready to use [Swift Package Manager](https://swift.org/package-manager/) project with [Kitura Webserver](https://github.com/IBM-Swift/Kitura) which can be deployed on Mac or Linux (of course you need to install [Swift](https://swift.org/) first).

## Thanks
Special Thanks to [Valentin Michalak](https://github.com/vmichalak) for his [Sonos Controller API](https://github.com/vmichalak/sonos-controller) 

## License
This project is licensed under the MIT License - see the [LICENSE](https://github.com/lmoedl/MOS-Projekt/blob/master/LICENSE) file for details.
