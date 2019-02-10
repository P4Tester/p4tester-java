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
import java.util.HashSet;

import static java.lang.System.exit;

public class P4TesterProbeProcessor {

    private ArrayList<NetworkProbeSet> networkProbeSets;
    private HashMap<Integer, NetworkProbeSet> probeSetHashMap;
    private Pcap pcap;
    private ArrayList<Integer> matchList;
    private long startTime;
    private int init;
    private HashSet<Integer> faultRules;
    private int maxFaults;

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
        this.matchList = new ArrayList<>();
        this.init = 0;
        this.faultRules = new HashSet<>();
        this.maxFaults = 1;
    }

    public void loop() {

        PcapPacketHandler handler = new PcapPacketHandler() {
            @Override
            public void nextPacket(PcapPacket pcapPacket, Object o) {
                checkPacket(pcapPacket);
            }
        };


        this.pcap.loop(-1, handler, "P4Tester");

    }

    public void setNetworkProbeSets(ArrayList<NetworkProbeSet> networkProbeSets) {
        this.networkProbeSets = networkProbeSets;
    }

    public void injectProbes() {
        if (networkProbeSets != null) {
            for (int i = 0; i < networkProbeSets.size(); i++) {
                //System.out.println("NetworkProbeSet :" + networkProbeSet.getRouters().size() + "  " + networkProbeSet.getPaths().size());
                NetworkProbeSet networkProbeSet = networkProbeSets.get(i);



                if (this.probeSetHashMap.containsKey(networkProbeSet.getMatch())) {
                    probeSetHashMap.put(networkProbeSet.getMatch(), networkProbeSet);
                }

                for (byte[] probe:networkProbeSet.generateProbes(i)) {
                    // System.out.println("Probe Size:" + probe.length);
                    this.pcap.sendPacket(probe);
                    try {
                        Thread.sleep(0, 100);
                    }catch (Exception e) {
                        e.printStackTrace();
                    }
                    // try {
                    //     this.handle.sendPacket(ethernet.serialize());
                    //} catch (NotOpenException e) {
                    //    e.printStackTrace();
                    //} catch (PcapNativeException e) {
                    //    e.printStackTrace();
                    //}
                }
                if (matchList.size() <= i) {
                    matchList.add(networkProbeSet.getDstIp());
                }
            }
        }
        if (init == 0) {
            this.startTime = System.nanoTime();
            init = 1;
        }
    }


    public void injectTofinoProbes() {
        if (networkProbeSets != null) {
            for (int i = 0; i < networkProbeSets.size(); i++) {
                //System.out.println("NetworkProbeSet :" + networkProbeSet.getRouters().size() + "  " + networkProbeSet.getPaths().size());
                NetworkProbeSet networkProbeSet = networkProbeSets.get(i);

                if (this.probeSetHashMap.containsKey(networkProbeSet.getMatch())) {
                    probeSetHashMap.put(networkProbeSet.getMatch(), networkProbeSet);
                }

                for (byte[] probe:networkProbeSet.generateTofinoProbes(i)) {
                    this.pcap.sendPacket(probe);
                    try {
                        Thread.sleep(0, 100);
                    }catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (matchList.size() <= i) {
                    matchList.add(networkProbeSet.getDstIp());
                }
            }
        }
        if (init == 0) {
            this.startTime = System.nanoTime();
            init = 1;
        }
    }

    private int count = 0;
    public void checkPacket(PcapPacket pcapPacket) {
        byte[] packet = pcapPacket.getByteArray(0, 42);
        Ethernet eth = new Ethernet();
        eth.deserialize(packet, 0, packet.length);
        try {
            if (eth.getEtherType() == EthType.IPv4) {
                IPv4 ipv4 = new IPv4();
                ipv4.deserialize(packet, 14, 42 - 14);
                int id = ipv4.getIdentification();
                int dstIp = ipv4.getDestinationAddress();
                if (ipv4.getProtocol() == IpProtocol.UDP) {
                    UDP udp = new UDP();
                    udp.deserialize(packet, 34, 42 - 34);
                    if (udp.getSourcePort() == 11111) {
                        int length = udp.getLength() - 8;
                        byte[] data = pcapPacket.getByteArray(42, length);
                        NetworkProbeSet networkProbeSet = this.networkProbeSets.get(id);
                        count ++;
                        if (count % 2000 ==0) {
                            System.out.println("Check packet!");
                            count = 0;
                        }
                        if (networkProbeSet != null) {
                            ArrayList<String> routers = networkProbeSet.check(data,0, length);
                            for (String string : routers) {
                                // System.out.println(string + " " + Integer.toHexString(dstIp) + " " + matchList.get(id));
//                                System.out.println(string + " " + id);
                                if (!faultRules.contains(dstIp)) {
                                    faultRules.add(dstIp);
                                    //if (faultRules.size() > this.maxFaults) {
                                    System.out.println("" + faultRules.size() + "\t" + (System.nanoTime() - startTime));
                                    //}
                                }
                            }
                        } else {
                            //System.out.println("Cannot find a network probe set for the dst : " +
                            //        (dstIp>>24) + "."+ ((dstIp>>16)&0xFF) + "." + ((dstIp>>8) &0xFF)+ "." + (dstIp&0xFF));
                            System.out.println("Cannot find a network probe set for the dst : " + id );

                        }
                    }
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }

    }
}