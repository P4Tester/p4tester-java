package org.p4tester;


public class Main {

    public static void main(String[] args) {
        P4TesterBDD bdd = new P4TesterBDD(32);
        // P4TesterBDD ipv6bdd = new P4TesterBDD(128);

        P4Tester p4tester = new P4Tester(bdd);
        if (args.length == 0) {
            p4tester.startInternet2(false, false);
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
