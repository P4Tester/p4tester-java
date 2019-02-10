package org.p4tester;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;



class CmdOption {

    @Option(name="-data", usage="Specify the name of data set")
    String dataSet = "default";

    @Option(name="-max", usage="Specify the max number of rules")
    int maxRules = 20000;

    @Option(name="-update", usage="Enable rule update")
    boolean update = false;

    @Option(name="-fast", usage="Enable fast update")
    boolean fast = false;

    @Option(name="-inject", usage="Inject probes")
    boolean inject = false;

    @Option(name="-print", usage="Print with format")
    boolean print = false;

    @Option(name="-priority", usage="Enable priority")
    boolean priority = false;

    @Option(name="-routers", usage="The max number of routers")
    int maxRouters = 1000;
}


public class Main {

    public static void main(String[] args) {
        int UPDATE_FLAG = 0;
        int PRINT_FLAG = 0;
        boolean INJECT_FLAG = false;
        P4TesterBDD bdd = new P4TesterBDD(32);
        // P4TesterBDD ipv6bdd = new P4TesterBDD(128);

        CmdOption cmdOption = new CmdOption();
        CmdLineParser parser = new CmdLineParser(cmdOption);

        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            e.printStackTrace();
            System.exit(1);
        }

        if (cmdOption.update) {
            if (cmdOption.fast) {
                UPDATE_FLAG = 1;
            } else {
                UPDATE_FLAG = 2;
            }
        } else {
            UPDATE_FLAG = 0;
        }

        if (cmdOption.print) {
            PRINT_FLAG = 1;
        } else {
            PRINT_FLAG = 0;
        }

        INJECT_FLAG = cmdOption.inject;

        P4Tester p4tester = new P4Tester(bdd,
                cmdOption.maxRules,
                cmdOption.priority,
                cmdOption.maxRouters);
        if (cmdOption.dataSet.equals("internet2")) {
            p4tester.startInternet2(UPDATE_FLAG, INJECT_FLAG, PRINT_FLAG);
        } else if (cmdOption.dataSet.equals("stanford")) {
            p4tester.startStanford(UPDATE_FLAG, INJECT_FLAG, PRINT_FLAG);
        } else if (cmdOption.dataSet.equals("tofino")) {
            p4tester.setMaxRouters(1);
            p4tester.startTofino(UPDATE_FLAG, INJECT_FLAG, PRINT_FLAG);
        } else {
            p4tester.startStanford(UPDATE_FLAG, INJECT_FLAG, PRINT_FLAG);
        }
    }
}
