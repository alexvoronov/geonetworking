[![DOI](https://zenodo.org/badge/21976/RNDITS/geonetworking.svg)](https://zenodo.org/badge/latestdoi/21976/RNDITS/geonetworking)
# Vehicle Adapter

This is the vehicle adapter that connects the GeoNetworking stack to the vehicle control system implemented in, for example, [Simulink](http://se.mathworks.com/products/simulink/), and it allows the vehicle control system to easily send vehicle-to-vehicle wireless messages. The vehicle control system communicates with the Vehicle Adapter via UDP, using the [Chalmers local message set](https://github.com/Zeverin/GCDC16-Chalmers-Communication/tree/master/Documentation). The vehicle adapter is an application built ontop of the Geonetworking library developed by Alexey Voronov.

The Vehicle Adapter was developed for the [GCDC 2016](http://gcdc.net/) competition by Albin Severinson of the Chalmers University team and was released as open source code. Several teams are using the system in preparation of the competition in favor of existing commercial alternatives.

The system is designed to make research and development in areas related to ITS-G5 open and easy, and as such is well suited for it. However, please note that the Vehicle Adapter, local message set and GeoNetworking library is still under development.

If you use this code in your research, make sure to cite the code using the DOI found above.

### ITS-G5 Stations
The system is built to be run on generic hardware and using open software to facilitate reasearch and development. However, the complete install process is quite involved and requires advanced knowledge of how Linux works. If you just want a working ITS-G5 station without the hassle and want to discuss purchasing options, send an email to me on [albin@severinson.org](mailto: albin@severinson.org).

### Running
The Vehicle Adapter can be run on any computer with Java. For development I recommend running it on the computer you're working on. There's no need to run it on a separate machine until you need to communicate wirelessly.

To run it you first need to compile it using the Maven build tool. When you have it installed you can compile the entire project simply by running 'mvn install' in the root directory. On OS X and Linux you can run the Vehicle Adapter using the run.sh script in the vehicle-adapter folder. Use the run.bat file instead on Windows.

In order to change the parameters of the Vehicle Adapter, simply edit the run.sh or run.bat files depending on if you're on OS X/Linux or Windows.

### Hardware and ITS-G5
I'm running the stack on a PC Engines APU1D4 board with an AR9280-based network interface card. Follow these [instructions](https://github.com/ssinyagin/pcengines-apu-debian-cd) to get Debian running on the board. Using the binary releases provided is the easiest way of getting the board up and running.

In order to setup the wireless you need to take care of the following:
- Linux kernel with support for ITS-G5
- A wireless network interface card with support for the 5.9GHz frequency band and 10MHz channels. I've tested cards based on the Atheros AR9280 chipset.
- Modified ath9k or ath5k driver depending on which network interface card you're using.
- Modified userspace utility iw that adds options to use the ITS-G5 functionality we've added to the kernel.
- Modifed wireless-regdb that includes regulatory requirements for the 5.9GHz frequency band.
- Modified CRDA that uses the modified wireless-regdb.

Instructions for all of these steps are provided [here](https://gist.github.com/lisovy/80dde5a792e774a706a9#file-802-11p-on-linux-L19).

### License
The Vehicle Adapter is licensed under the [Apache-2.0](https://www.apache.org/licenses/LICENSE-2.0) license.



