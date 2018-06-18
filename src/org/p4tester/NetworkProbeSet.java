package org.p4tester;

import org.p4tester.packet.Ethernet;
import org.p4tester.packet.IPv4;
import org.p4tester.packet.UDP;
import org.projectfloodlight.openflow.types.EthType;
import org.projectfloodlight.openflow.types.IpProtocol;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;

public class NetworkProbeSet extends ProbeSet {
    private ArrayList<Router> routers;
    private ArrayList<SwitchProbeSet> switchProbeSets;
    private ArrayList<Integer> paths;
    private HashMap<String, Integer> recordRouterMap;
    private static final int MAX = 20;
    private int count;
    private P4TesterBDD bdd;
    private int update;
    private P4Tester tester;
    private ArrayList<byte[]> probes;

    NetworkProbeSet(P4TesterBDD bdd, P4Tester p4Tester) {
        super(-1);
        this.bdd = bdd;
        this.tester = p4Tester;
        this.switchProbeSets = new ArrayList<>();
        this.paths = new ArrayList<>();
        this.routers = new ArrayList<>();
        this.count = 0;
        this.update = 0;
        this.recordRouterMap = new HashMap<>();
        this.probes = new ArrayList<>();
    }

    @Override
    public int getMatch() {
        return match;
    }


    public void addSwitchProbeSet(SwitchProbeSet switchProbeSet) {
        this.switchProbeSets.add(switchProbeSet);
        this.addRouter(switchProbeSet.getRouter());

        if (this.match == -1) {
            this.match = switchProbeSet.getMatch();
        } else {
            int var = this.match;
            this.match = this.bdd.and(this.match, switchProbeSet.getMatch());
            this.bdd.deref(var);
        }
    }

    public ArrayList<SwitchProbeSet> getSwitchProbeSets() {
        return switchProbeSets;
    }

    public void removeSwitchProbeSet(SwitchProbeSet switchProbeSet) {
        this.switchProbeSets.remove(switchProbeSet);
    }

    @Override
    public void setMatch(int match) {
        this.match = match;
        this.update = 0;
    }

    public void updatePaths() {
        this.recordRouterMap.clear();
        this.paths.clear();
        this.count = 0;
        for (int i = 0; i < this.tester.getPath().size(); i ++) {
            SwitchPortPair pair = this.tester.getPath().get(i);
            if (routers.contains(pair.getRouter())) {
                this.traverse(pair.getRouter(), i);
            }
        }
    }

    public void traverse(Router router, int i) {
        if (!this.recordRouterMap.keySet().contains(router.getName())) {
            this.recordRouterMap.put(router.getName(), i);
            if (this.count == 0) {
                this.paths.add(i);
            }
            this.count++;
            if (this.count == MAX) {
                this.paths.add(i);
                this.count = 0;
            }
            if (this.recordRouterMap.size() == this.routers.size()) {
                this.paths.add(i);
            }
        }
    }

    public void addRouter(Router router) {
        if (!routers.contains(router)) {
            router.addNetworkProbeSets(this);
            this.routers.add(router);
        }
    }

    public ArrayList<Router> getRouters() {
        return routers;
    }

    public void removeRouter(Router router) {
        this.update = 0;
        this.routers.remove(router);
        // int i = this.recordRouterMap.get(router.getName());
        // this.recordRouterMap.remove(router.getName());
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    public ArrayList<byte[]> generateProbes() {
        ArrayList<SwitchPortPair> path = this.tester.getPath();
        if (this.update == 0) {
            this.probes.clear();

            int[] ipBytes = this.bdd.oneSATArray(this.match);

            int dstIp = 0;
            for (int b:ipBytes) {
                dstIp <<= 1;
                dstIp += b;
            }

            Ethernet ethernet = new Ethernet();

            byte[] bytes = {0x11, 0x11, 0x11, 0x11, 0x11, 0x11};
            ethernet.setDestinationMACAddress(bytes)
                    .setSourceMACAddress(bytes)
                    .setEtherType(EthType.IPv4);



            IPv4 ip = new IPv4();
            ip.setProtocol(IpProtocol.UDP)
                    .setDestinationAddress(dstIp)
                    .setSourceAddress(dstIp);



            UDP udp = new UDP();
            udp.setDestinationPort((short) 11).setSourcePort((short) 11);

            ip.setPayload(udp);
            ethernet.setPayload(ip);


            this.updatePaths();


            for (int i = 0; i < paths.size() - 1; i += 2) {
                int start = paths.get(i);
                int end = paths.get(i + 1);
                SwitchPortPair startPair = path.get(start);
                SwitchPortPair endPair = path.get(end);

                ArrayList<Short> portList = tester.getForwardPortList(startPair.getRouter().getName());

                ArrayList<Short> srs =  new ArrayList<>();


                //if (startPair.getRouter().getName().equals("bbra")) {

                //}

                for (short p:portList) {
                    srs.add((short)(p<<8));

                }

                for (int j = start; j < end; j++) {
                    short p = path.get(j).getPort();
                    if (recordRouterMap.keySet().contains(path.get(j).getRouter().getName())) {
                        int k = recordRouterMap.get(path.get(j).getRouter().getName());
                        if (k == j) {
                            srs.add((short)((p<<8)|1));
                        } else {
                            srs.add((short)(p<<8));
                        }
                    } else {
                        srs.add((short)(p<<8));
                    }
                }


                if (srs.size() == portList.size()) {
                    continue;
                }

                portList = tester.getBackwordPortList(endPair.getRouter().getName());
                for (short p:portList) {
                    srs.add((short)(p<<8));
                }
                int x = srs.size() - 1;
                srs.set(x, (short)(srs.get(x) | 2));


                byte[] header = ethernet.serialize();



                byte[] probe = new byte[header.length + srs.size()*2 + 20]; // For test results
                ByteBuffer byteBuffer = ByteBuffer.wrap(probe);

                byteBuffer.put(header);

                for (Short sr: srs) {
                    byteBuffer.putShort(sr);
                }

                this.probes.add(probe);
            }

            this.update = 1;

        }


        return probes;
    }

    public ArrayList<Integer> getPaths() {
        return paths;
    }
}
