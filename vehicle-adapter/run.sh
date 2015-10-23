portRcvFromSimulink=5001
simulinkAddress=127.0.0.1:5000
localPortForUdpLinkLayer=4000
remoteAddressForUdpLinkLayer=127.0.0.1:4001
stationId=1337
macAddress=00:21:cc:67:34:0e
java -jar target/vehicle-adapter-0.0.1-SNAPSHOT-jar-with-dependencies.jar --portRcvFromSimulink $portRcvFromSimulink --simulinkAddress $simulinkAddress --localPortForUdpLinkLayer $localPortForUdpLinkLayer --remoteAddressForUdpLinkLayer $remoteAddressForUdpLinkLayer --stationID $stationId --macAddress $macAddress
