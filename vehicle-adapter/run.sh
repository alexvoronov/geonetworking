portRcvFromSimulink=5003
portSendCam=5000;
portSendDenm=5001;
portSendIclcm=5002;
simulinkAddress=127.0.0.1:5000
localPortForUdpLinkLayer=4000
remoteAddressForUdpLinkLayer=127.0.0.1:4001
macAddress=00:21:cc:67:34:0e
java -jar target/vehicle-adapter-0.0.1-SNAPSHOT-jar-with-dependencies.jar --portRcvFromSimulink $portRcvFromSimulink --portSendCam $portSendCam --portSendDenm $portSendDenm --portSendIclcm $portSendIclcm --simulinkAddress $simulinkAddress --localPortForUdpLinkLayer $localPortForUdpLinkLayer --remoteAddressForUdpLinkLayer $remoteAddressForUdpLinkLayer  --macAddress $macAddress
