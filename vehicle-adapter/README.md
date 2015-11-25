# Vehicle Adapter

This is the Chalmers team vehicle adapter that connects the GeoNetworking stack to the vehicle control system implemented in [Simulink](http://se.mathworks.com/products/simulink/). The vehicle control system communicates with the GeoNetworking stack via UDP, using the [Chalmers local message set](https://github.com/Zeverin/GCDC16-Chalmers-Communication/tree/master/Documentation). The vehicle adapter is an application built ontop of the Geonetworking library developed by Alex Voronov.

Both the vehicle adapter and the local message set is still in development and everything is subject to change.

### Running
The VA can be run on any computer with Java, and that is powerful enough. For development I recommend running it on the computer you're workin on. There's no need to run it on a separate machine until you need to communicate wirelessly.

To run it simply install everything by using mvn install in the root directory and run the vehicle adapter using the run.sh script in the vehicle-adapter folder.


### Hardware and ITS-G5
I'm running the stack on a PC Engines APU4D board with an AR9280-based network interface card. Follow these [instructions](https://github.com/ssinyagin/pcengines-apu-debian-cd) to get Debian running on the board.

In order to setup the wireless you need to take care of the following:
- Linux kernel with support for ITS-G5
- A wireless network interface card with support for the 5.9GHz frequency band and 10MHz channels. I'm using a card based on the Atheros AR9280 chipset.
- Modified ath9k or ath5k driver depending on which network interface card you're using.
- Modified userspace utility iw that adds options to use the ITS-G5 functionality we've added to the kernel.
- Modifed wireless-regdb that includes regulatory requirements for the 5.9GHz frequency band.
- Modified CRDA that uses our modified wireless-regdb.

Instructions for all of these steps are provided [here](https://gist.github.com/lisovy/80dde5a792e774a706a9#file-802-11p-on-linux-L19).


