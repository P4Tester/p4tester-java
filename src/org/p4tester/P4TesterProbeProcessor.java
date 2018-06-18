package org.p4tester;


import org.jnetpcap.Pcap;
import org.jnetpcap.PcapIf;
import org.p4tester.packet.Ethernet;

import java.util.ArrayList;

import static java.lang.System.exit;

public class P4TesterProbeProcessor  {

    private String devName;

    private ArrayList<NetworkProbeSet> networkProbeSets;
    static final int PACKET_COUNT = 1000;


    P4TesterProbeProcessor(ArrayList<NetworkProbeSet> networkProbeSets) {

        ArrayList<PcapIf> devs = new ArrayList<>();
        StringBuilder errbuf = new StringBuilder();
        int r = Pcap.findAllDevs(devs, errbuf);
        //System.out.println("Open the device: " + this.nif.getName());
        if (r == Pcap.NOT_OK || devs.isEmpty()) {
            System.out.println("Error");
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