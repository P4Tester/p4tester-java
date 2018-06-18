package org.p4tester;


import org.jnetpcap.Pcap;
import org.jnetpcap.PcapIf;

import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {
        P4TesterBDD bdd = new P4TesterBDD(32);
        // P4TesterBDD ipv6bdd = new P4TesterBDD(128);

        P4Tester p4tester = new P4Tester(bdd);

        ArrayList<PcapIf> devs = new ArrayList<>();
        StringBuilder errbuf = new StringBuilder();
        int r = Pcap.findAllDevs(devs, errbuf);
        if (r == Pcap.NOT_OK || devs.isEmpty()) {
            System.out.println("Error");
        }


        if (args.length == 0) {
//            p4tester.startInternet2(false, false);
            //p4tester.startStanford(false, true);
            // P4TesterProbeProcessor probeProcessor = new P4TesterProbeProcessor(null);
            // probeProcessor.sendProbes();
        } if (args.length == 1) {
            if (args[0].equals("internet2")) {
                p4tester.startInternet2(false, false);
            } else {
                p4tester.startStanford(false, false);
            }
        } if (args.length == 2) {
            if (args[0].equals("internet2")) {
                p4tester.startInternet2(true, false);
            } else {
                p4tester.startStanford(true, false);
            }
        }
    }
}
