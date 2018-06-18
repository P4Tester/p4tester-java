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

    public void sendProbes() {
                //System.out.println("NetworkProbeSet :" + networkProbeSet.getRouters().size() + "  " + networkProbeSet.getPaths().size());
        byte[] data = new byte [128];
        for (int i = 0 ;  i < data.length; i++ ) {
            data[i] = 1;
        }
        for (int i =0 ; i< 100; i++) {
            this.pcap.sendPacket(data);
            try {
                Thread.currentThread().sleep(1000);
            } catch (Exception e) {
                exit(1);
            }
        }
        //for (byte[] probe:networkProbeSet.generateProbes()) {
                    // try {
                    //     this.handle.sendPacket(ethernet.serialize());
                    //} catch (NotOpenException e) {
                    //    e.printStackTrace();
                    //} catch (PcapNativeException e) {
                    //    e.printStackTrace();
                    //}
        //}
    }


}