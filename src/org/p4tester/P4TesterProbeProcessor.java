package org.p4tester;


import org.jnetpcap.Pcap;
import org.jnetpcap.PcapIf;
import org.p4tester.packet.Ethernet;

import java.util.ArrayList;

import static java.lang.System.exit;

public class P4TesterProbeProcessor  {


    private ArrayList<NetworkProbeSet> networkProbeSets;
    static final int PACKET_COUNT = 1000;
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
    }

    public void loop() {

    }

    public void setNetworkProbeSets(ArrayList<NetworkProbeSet> networkProbeSets) {
        this.networkProbeSets = networkProbeSets;
    }

    public void injectProbes() {
        if (networkProbeSets != null) {
            for (NetworkProbeSet networkProbeSet:networkProbeSets) {
                //System.out.println("NetworkProbeSet :" + networkProbeSet.getRouters().size() + "  " + networkProbeSet.getPaths().size());
                for (byte[] probe:networkProbeSet.generateProbes()) {
                    System.out.println("Probe Size:" + probe.length);
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



}