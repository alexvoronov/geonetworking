# Hardware and drivers

GeoNetworking is supposed to be used with ETSI ITS-G5. ITS-G5 MAC layer ([ETSI EN 302 663](http://webapp.etsi.org/workprogram/Report_WorkItem.asp?WKI_ID=34109)) is based on 802.11p. In turn, 802.11p (part of [802.11-2012](http://standards.ieee.org/about/get/802/802.11.html)) is based on 802.11a, see, for example, FreeBSD wiki for a [summary of how 11p is different from 11a](https://wiki.freebsd.org/802.11p), or stackoverflow for an [answer with a summary of needed driver changes](http://stackoverflow.com/a/10076012). Annex B of v1.2.1 of [ETSI EN 302 663](http://webapp.etsi.org/workprogram/Report_WorkItem.asp?WKI_ID=34109) also has a summary of 11p. 

For 802.11p Linux drivers, see, for example, [ath5k](https://wireless.wiki.kernel.org/en/users/drivers/ath5k) and [ath9k](https://wireless.wiki.kernel.org/en/users/drivers/ath9k) drivers modified by [CTU-IIG](https://github.com/CTU-IIG) ([kernel](https://github.com/CTU-IIG/802.11p-linux), [iw](https://github.com/CTU-IIG/802.11p-iw), [regdb](https://github.com/CTU-IIG/802.11p-wireless-regdb), [crda](https://github.com/CTU-IIG/802.11p-crda)) or by [Componentality](https://github.com/Componentality). Some of these modifications might already be in the upstream ([e5d71](https://git.kernel.org/cgit/linux/kernel/git/next/linux-next.git/commit/?id=239281f803e2efdb77d906ef296086b6917e5d71), [48b50](https://git.kernel.org/cgit/linux/kernel/git/next/linux-next.git/commit/?id=6e0bd6c35b021dc73a81ebd1ef79761233c48b50)). There are rumors that Linux kernel 4.4 will have a lot of it integrated. There is also a Gist with a step-by-step [802.11p instruction](https://gist.github.com/lisovy/80dde5a792e774a706a9). Any further updates on the status are much appreciated! 


For hardware for ath5k, see cards based on Atheros AR5414 chipset, like [Mikrotik R52H](http://routerboard.com/R52H) (for more hardware see wiki lists on [kernel.org](https://wireless.wiki.kernel.org/en/users/drivers/ath5k#supported_devices) or [debian.org](https://wiki.debian.org/ath5k#Supported_Devices)). 

For hardware for ath9k, see Atheros AR92xx-based cards, like [Compex WLE200NX](http://www.pcengines.ch/wle200nx.htm) (miniPCI express) or [Routerboard R5SHPn](http://routerboard.com/R5SHPn) ("old" miniPCI). Note that [Mikrotik wiki](http://wiki.mikrotik.com/wiki/Manual:Wireless_Advanced_Channels) says that in 2011 only AR92xx chips supported the needed 10 MHz channels, but maybe the support was extended since then? Any updates welcome! For example, whether or not [Mikrotik R11e-5HnD](http://routerboard.com/R11e-5HnD) based on AR9580 works (there are reports that [Unex DHXA-222](http://unex.com.tw/wi-fi/dhxa-222) based on AR9462 works). For more ath9k devices see wiki lists on [kernel.org](https://wireless.wiki.kernel.org/en/users/drivers/ath9k/products) or [debian.org](https://wiki.debian.org/ath9k#Supported_Devices). 

Examples of cheap small computers to install the wifi cards are [Alix board](http://www.pcengines.ch/alix.htm) and it's successor [apu platform](http://www.pcengines.ch/apu.htm); another small computer with Mini PCI Express is [Hummingboard](https://www.solid-run.com/products/hummingboard/). 

As a Linux distro, try [Voyage Linux](http://linux.voyage.hk/). 

Note that the Linux machine with the wifi card don't have to be the same as the one running this GeoNetworking stack (it can be two separate machines talking UDP to each other). 

Any other information about hardware and drivers is greatly appreciated!
