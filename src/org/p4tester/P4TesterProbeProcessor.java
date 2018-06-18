package org.p4tester;


import org.jnetpcap.Pcap;
import org.jnetpcap.packet.PcapPacket;
import org.jnetpcap.packet.PcapPacketHandler;
import org.p4tester.packet.Ethernet;
import org.p4tester.packet.IPv4;
import org.p4tester.packet.UDP;
import org.projectfloodlight.openflow.types.EthType;
import org.projectfloodlight.openflow.types.IpProtocol;

import java.util.ArrayList;
import java.util.HashMap;

import static java.lang.System.exit;

public class P4TesterProbeProcessor  implements PcapPacketHandler {


    private ArrayList<NetworkProbeSet> networkProbeSets;
    private HashMap<Integer, NetworkProbeSet> probeSetHashMap;
    private Pcap pcap;


    P4TesterProbeProcessor(ArrayList<NetworkProbeSet> networkProbeSets) {

        int snaplen = 64 * 1024;           // Capture all packets, no trucation
        int flags = Pcap.MODE_PROMISCUOUS; // capture all packets
        int timeout = 10 * 1000;           // 10 seconds in millis
        StringBuilder errbuf = new StringBuilder();
        this.pcap =
                Pcap.openLive("heth", snaplen, flags, timeout, errbuf);
        if (pcap == null) {
            System.out.println("Cannot open heth!");
            exit(1);
        }

        this.networkProbeSets = networkProbeSets;
        this.probeSetHashMap =new HashMap<>();
    }

    public void loop() {
        this.pcap.loop(1000000000, this, "P4Tester");
    }

    public void setNetworkProbeSets(ArrayList<NetworkProbeSet> networkProbeSets) {
        this.networkProbeSets = networkProbeSets;
    }

    public void injectProbes() {
        if (networkProbeSets != null) {
            for (NetworkProbeSet networkProbeSet:networkProbeSets) {
                //System.out.println("NetworkProbeSet :" + networkProbeSet.getRouters().size() + "  " + networkProbeSet.getPaths().size());

                if (this.probeSetHashMap.containsKey(networkProbeSet.getMatch())) {
                    probeSetHashMap.put(networkProbeSet.getMatch(), networkProbeSet);
                }

                for (byte[] probe:networkProbeSet.generateProbes()) {
                    // System.out.println("Probe Size:" + probe.length);
                    this.pcap.sendPacket(probe);
                    // try {
                    //     this.handle.sendPacket(ethernet.serialize());
                    //} catch (NotOpenException e) {
                    //    e.printStackTrace();
                    //} catch (PcapNativeException e) {
                    //    e.printStackTrace();
                    //}
                }
            }
        }
    }

    @Override
    public void nextPacket(PcapPacket pcapPacket, Object o) {
        byte[] packet = pcapPacket.getByteArray(0, 1500);
        Ethernet eth = new Ethernet();
        eth.deserialize(packet, 0, packet.length);
        System.out.println("Received Packet!");
        if (eth.getEtherType() == EthType.IPv4) {
            IPv4 ipv4 = (IPv4) eth.getPayload();
            int dstIp = ipv4.getDestinationAddress();
            if (ipv4.getProtocol() == IpProtocol.UDP) {
                UDP udp = (UDP) ipv4.getPayload();
                if (udp.getSourcePort() == 11) {
                    NetworkProbeSet networkProbeSet = this.probeSetHashMap.get(dstIp);
                    if (networkProbeSet != null) {
                        ArrayList<String> routers = networkProbeSet.check(udp.getPayload().serialize());
                        for (String string:routers) {
                            System.out.println(string + " " + dstIp);
                        }
                    } else {
                        System.out.println("Cannot find a network probe set for the dst : " + dstIp);
                    }
                }
            }
        }

    }
}