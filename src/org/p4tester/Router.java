package org.p4tester;


import java.util.ArrayList;
import java.util.HashMap;


class RouterRule {
    private String matchIp;
    private int matchBdd;
    private int prefix;
    private String port;
    private String nextHop;
    private ArrayList<SwitchProbeSet> switchProbeSets;

    RouterRule(String matchIp, int matchBdd, int prefix, String port, String nextHop) {
        this.matchIp = matchIp;
        this.matchBdd = matchBdd;
        this.prefix = prefix;
        this.port = port;
        this.nextHop = nextHop;
        this.switchProbeSets = new ArrayList<>();
    }


    /*
    public void setMatchBdd(int matchBdd) {
        this.matchBdd = matchBdd;
    }
    */
    /*
    public void setNextHop(String nextHop) {
        this.nextHop = nextHop;
    }
    */

    /**
     * Set destination port
     * @param port destination port
     */
    public void setPort(String port) {
        this.port = port;
    }

    ArrayList<SwitchProbeSet> getSwitchProbeSets() {
        return switchProbeSets;
    }

    int getMatchBdd() {
        return matchBdd;
    }

    String getMatchIp() {
        return matchIp;
    }

    public int getPrefix() {
        return prefix;
    }

    public String getPort() {
        return port;
    }

    String getNextHop() {
        return nextHop;
    }

    void addSwitchProbeSet(SwitchProbeSet probeSet) {
        this.switchProbeSets.add(probeSet);
    }


}

public class Router {
    HashMap<String, RouterRule> ruleHashMap;
    ArrayList<RouterRule> rules ;
    P4TesterBDD bdd;
    private String name;
    private ArrayList<String> localIps;
    private ArrayList<SwitchProbeSet> switchProbeSets;
    private ArrayList<NetworkProbeSet> networkProbeSets;
    int MAX_RULES = 20000;
    private boolean enablePriorityChecking;
    private BDDTree tree;
    private int headerSpaceLength;


    Router(P4TesterBDD bdd, String name, int maxRule, boolean enablePriorityChecking) {
        this.bdd = bdd;
        this.name = name;
        this.rules = new ArrayList<>();
        this.localIps = new ArrayList<>();
        this.switchProbeSets = new ArrayList<>();
        this.networkProbeSets = new ArrayList<>();
        this.enablePriorityChecking = enablePriorityChecking;
        this.ruleHashMap = new HashMap<>();
        this.neighbors = new HashMap<>();
        this.headerSpaceLength = 32;
        this.MAX_RULES = maxRule;
    }



    public BDDTree getTree() {
        return tree;
    }


    void buildTree() {
        this.tree = BDDTree.buildBinary(bdd, this.switchProbeSets);
    }

    ArrayList<RouterRule> getRules() {
        return rules;
    }

    public String getName() {
        return name;
    }

    void addNetworkProbeSet(NetworkProbeSet networkProbeSet) {
        this.networkProbeSets.add(networkProbeSet);
    }

    ArrayList<SwitchProbeSet> getSwitchProbeSets() {
        return switchProbeSets;
    }

    public void addIPv4withPrefix( String str, String port, String nextHop) {
        String[] ipv4 =  str.split("/");

        if (ipv4.length != 2) {
            return;
        }

        if (this.rules.size() > MAX_RULES) {
            return;
        }

        String[] ip = ipv4[0].split("\\.");
        int prefix = Integer.valueOf(ipv4[1]);

        if (prefix == 0) {
            return;
        }

        ArrayList<Integer> bits = new ArrayList<>();
       // bits.add(0);
        // System.out.println(str);
        for (String i:ip) {
            int value = Integer.valueOf(i);
            for (int j = 0; j < 8; j++) {
                // int bit = value / (1 << (7 - j));
                bits.add( value / (1 << (7 - j)));
                value = value % (1 << (7 - j));
                //System.out.print(bit + "");
            }
            //System.out.print( "|");
        }
       //System.out.println();

        int rule = this.bdd.getTrue();
        for (int i = 0; i < 32; i++) {
            if (bits.get(i) == 1) {
                //int var = rule;
                rule = this.bdd.and(rule, this.bdd.getVar(i));
                //this.bdd.deref(var);
            } else if (bits.get(i) == 0) {
                //int var = rule;
                rule = this.bdd.and(rule, this.bdd.getNotVar(i));
                //this.bdd.deref(var);
            }
        }

        int mask = this.bdd.getTrue();
        for (int i = 0; i < prefix; i++) {
            if (bits.get(i) == 1) {
                //int var = rule;
                mask = this.bdd.and(mask, this.bdd.getVar(i));
                // this.bdd.deref(var);
            } else if (bits.get(i) == 0) {
                //int var = rule;
                mask = this.bdd.and(mask, this.bdd.getNotVar(i));
                // this.bdd.deref(var);
            }
        }

        for (int i = prefix; i <32 ; i++) {
            mask = this.bdd.and(mask, this.bdd.getNotVar(i));
        }
        rule = this.bdd.or(rule, mask);

        /*
        {
            int[] ipBytes = this.bdd.oneSATArray(rule);

            long dstIp = 0;
            for (int b:ipBytes) {
                dstIp <<= 1;
                dstIp += b;
            }
            if (dstIp < 0) {
                dstIp = -dstIp;
            }
            System.out.println("" + (dstIp>> 24) + "." +((dstIp>> 16) &0xFF) + "." + ((dstIp>> 8) &0xFF) + "." + ((dstIp) &0xFF) );
        }
        */

        RouterRule routerRule = new RouterRule(str, rule, prefix, port, nextHop);
        this.rules.add(routerRule);
        this.ruleHashMap.put(str, routerRule);
    }

    // public BDDTree getComplementTree() {
    //    return complementTree;
    //}


    void addLocalIp(String ip) {
        if (!this.localIps.contains(ip)) {
            this.localIps.add(ip);
        }
    }

    void generateProbeSets() {
        int[] prefixMap = new int[this.headerSpaceLength + 2];
        int[] ruleIds = new int[this.rules.size()];

        for (RouterRule r:this.rules) {
            prefixMap[r.getPrefix()] ++;
        }

        for (int i = this.headerSpaceLength; i >= 0 ; i --) {
            prefixMap[i] += prefixMap[i + 1];
        }

        for (int i = 0; i < this.rules.size(); i ++) {
            try {
                int var = -- prefixMap[rules.get(i).getPrefix()];
                ruleIds[var] = i;
            } catch (Exception e) {
                System.out.println(ruleIds.length);
                System.out.println(prefixMap[i]);
                System.exit(1);
            }
        }
        int[] tmp_rules = new int[this.rules.size()];
        int Ha = bdd.getTrue();
        for (int i = 0; i < ruleIds.length; i++) {
            int rid_i = ruleIds[i];
            int r = this.rules.get(rid_i).getMatchBdd();
            // System.out.println(prefixes.get(rid_i));
            tmp_rules[i] = bdd.and(r, Ha);
            Ha = bdd.subtract(Ha, tmp_rules[i]);
        }

        // int count = 0;
        for (int i = 0; i < ruleIds.length; i++) {
            RouterRule rule_i = rules.get(ruleIds[i]);
            int r = rule_i.getMatchBdd();
            // System.out.println(prefixes.get(rid_i));
            int rh = tmp_rules[i];
            // this.bdd.print(this.bdd.oneSAT(rh));
//            Ha = bdd.subtract(Ha, rh);
            int Hb = r;
            if (this.bdd.oneSAT(rh) != 0) {
                if (this.enablePriorityChecking) {
                    for (int j = prefixMap[rule_i.getPrefix() - 1]; j < ruleIds.length; j++) {
                        RouterRule rule_j = rules.get(ruleIds[j]);
                        int override = this.bdd.and(Hb, tmp_rules[j]);

                        if (this.bdd.oneSAT(override) != 0) {
                            if (!rule_i.getPort().equals(rule_j.getPort())) {
                                SwitchProbeSet switchProbeSet = new SwitchProbeSet(rule_i,
                                        override,
                                        this,
                                        rule_j.getPrefix());
                                rule_i.addSwitchProbeSet(switchProbeSet);
                                switchProbeSets.add(switchProbeSet);
                                // System.out.println("1");
                            }
                            // else {
                            //
                            // }
                            Hb = this.bdd.subtract(Hb, override);
                        }
                    }
                }
                if (this.bdd.oneSAT(Hb) != 0) {
                    SwitchProbeSet switchProbeSet = new SwitchProbeSet(rule_i,
                            rule_i.getMatchBdd(),
                            this,
                            0);
                    rule_i.addSwitchProbeSet(switchProbeSet);
                    switchProbeSets.add(switchProbeSet);
                }
            }
            //else {
                // count ++;
                // System.out.println(i);
            // }
        }
        // System.out.println(count);
    }

    public void regenerateProbeSets() {
        for (SwitchProbeSet switchProbeSet:this.switchProbeSets) {
            if (switchProbeSet.getNetworkProbeSet() != null) {
                switchProbeSet.getNetworkProbeSet().removeSwitchProbeSet(switchProbeSet);
            }
        }

        this.switchProbeSets.clear();
        this.networkProbeSets.clear();

        this.generateProbeSets();

        /*
        int[] prefixMap = new int[this.headerSpaceLength + 2];
        int[] ruleIds = new int[this.rules.size()];
        for (RouterRule r:this.rules) {
            prefixMap[r.getPrefix()] ++;
        }

        for (int i = this.headerSpaceLength; i >= 0  ; i--) {
            prefixMap[i] += prefixMap[i + 1];
        }

        for (int i = 0; i < this.rules.size(); i++) {
            try {
                int var = -- prefixMap[rules.get(i).getPrefix()];
                ruleIds[var] = i;
            } catch (Exception e) {
                System.out.println(ruleIds.length);
                System.out.println(prefixMap[i]);
            }
        }
        int[] tmp_rules = new int[this.rules.size()];
        int Ha = bdd.getTrue();
        for (int i = 0; i < ruleIds.length; i++) {
            int rid_i = ruleIds[i];
            int r = this.rules.get(rid_i).getMatchBdd();
            // System.out.println(prefixes.get(rid_i));
            tmp_rules[i] = bdd.and(r, Ha);
            Ha = bdd.subtract(Ha, tmp_rules[i]);
        }

        // int count = 0;
        for (int i = 0; i < ruleIds.length; i++) {
            RouterRule rule_i = rules.get(ruleIds[i]);
            int r = rule_i.getMatchBdd();
            // System.out.println(prefixes.get(rid_i));
            int rh = tmp_rules[i];
            // this.bdd.print(this.bdd.oneSAT(rh));
//            Ha = bdd.subtract(Ha, rh);
            if (this.bdd.oneSAT(rh) != 0) {
                int Hb = rh;
                if (this.enablePriorityChecking) {

                    for (int j = prefixMap[rule_i.getPrefix() - 1]; j < ruleIds.length; j++) {
                        RouterRule rule_j = rules.get(ruleIds[j]);
                        int override = this.bdd.and(Hb, tmp_rules[j]);

                        if (this.bdd.oneSAT(override) != 0) {
                            if (!rule_i.getPort().equals(rule_j.getPort())) {
                                SwitchProbeSet switchProbeSet = new SwitchProbeSet(rule_i, override, this, rule_j.getPrefix());
                                rule_i.addSwitchProbeSet(switchProbeSet);
                                switchProbeSets.add(switchProbeSet);
                            } else {

                            }
                            Hb = this.bdd.subtract(Hb, override);
                        }
                    }
                }
                if (this.bdd.oneSAT(Hb) != 0) {
                    SwitchProbeSet switchProbeSet = new SwitchProbeSet(rule_i, Hb, this, 0);
                    rule_i.addSwitchProbeSet(switchProbeSet);
                    switchProbeSets.add(switchProbeSet);
                }
            }
            //else {
            // count ++;
            // System.out.println(i);
            // }
        }
        // System.out.println(count);
        */
    }

    private int getMatchBdd(String match) {
        String[] ipv4 =  match.split("/");
        if (ipv4.length != 2) {
            return 0;
        }

        String[] ip = ipv4[0].split("\\.");
        int prefix = Integer.valueOf(ipv4[1]);

        ArrayList<Integer> bits = new ArrayList<>();

        for (String i:ip) {
            int value = Integer.valueOf(i);
            for (int j = 0; j < 8; j++) {
                bits.add(value / (1 << (7 - j)));
                value = value % (1 << (7 - j));
            }
        }

        int rule;
        if (prefix > 0) {
            rule = this.bdd.getVar(0);

            if (bits.get(0) == 0) {
                rule = this.bdd.getNotVar(0);
            }

            for (int i = 1; i < 32; i++) {
                if (i < prefix) {
                    int var = rule;
                    if (bits.get(i) == 1) {
                        rule = this.bdd.and(rule, this.bdd.getVar(i));
                    } else {
                        rule = this.bdd.and(rule, this.bdd.getNotVar(i));
                    }
                    this.bdd.deref(var);
                }
            }
        } else {
            rule = this.bdd.getTrue();
        }

        return rule;
    }

    private int getPrefix( String match) {
        String[] ipv4 =  match.split("/");
        if (ipv4.length != 2) {
            return 0;
        }

        return Integer.valueOf(ipv4[1]);
    }

    RouterRule addRule(String match,
                        String port,
                        String nextHop) {
        if (getPrefix(match) == 0) {
            return null;
        }
        int matchBdd = this.getMatchBdd(match);
        RouterRule routerRule = new RouterRule(match, matchBdd,
                getPrefix(match), port, nextHop);
        this.rules.add(routerRule);
        this.ruleHashMap.put(match, routerRule);
        return routerRule;
    }

    @Deprecated
    public ArrayList<SwitchProbeSet> addRuleWithPriority(String match,
                                                             String port,
                                                             String nextHop) {
        int matchBdd = this.getMatchBdd(match);
        RouterRule routerRule = new RouterRule(match, matchBdd,
                getPrefix(match), port, nextHop);
        this.rules.add(routerRule);
        this.ruleHashMap.put(match, routerRule);


        // Rules that need to add new network probe sets
        ArrayList<SwitchProbeSet> bddRules = new ArrayList<>();

        int var = this.bdd.subtract(matchBdd, this.tree.getRoot().getMatch());

        if (this.bdd.oneSAT(var) != 0) {
            bddRules.add(new SwitchProbeSet(routerRule, var, this, 0));
        }

        ArrayList<BDDTreeNode> nodes = this.tree.queryMatchNodes(matchBdd);

        for (BDDTreeNode node:nodes) {
            SwitchProbeSet switchProbeSet = node.getSwitchProbeSet();
            NetworkProbeSet networkProbeSet = switchProbeSet.getNetworkProbeSet();
            RouterRule ruleTmp = switchProbeSet.getRouterRule();
            if (ruleTmp.getPrefix() > routerRule.getPrefix()) {
                int tmp = this.bdd.subtract(switchProbeSet.getMatch(), matchBdd);
                if (this.bdd.oneSAT(tmp) != 0) {
                    bddRules.add(new SwitchProbeSet(routerRule, tmp,
                            this, 0));

                    var = this.bdd.and(switchProbeSet.getMatch(), matchBdd);
                    switchProbeSet.setMatch(var);
                    BDDTreeNode tmpNode = node.getParent();
                    while (tmpNode != null) {
                        var = this.bdd.subtract(tmpNode.getMatch(), tmp);
                        tmpNode.setMatch(var);
                        tmpNode = tmpNode.getParent();
                    }

                    tmp = this.bdd.subtract(networkProbeSet.getMatch(), matchBdd);
                    if (bdd.oneSAT(tmp) != 0) {
                        // tmp = this.bdd.and(networkProbeSet.getMatch(), matchBdd);
                        networkProbeSet.setMatch(tmp);
                    }
                }

            } else if (ruleTmp.getPrefix() < routerRule.getPrefix()) {
                if (bdd.isOverlap(networkProbeSet.getMatch(), matchBdd)) {
                    int tmp = this.bdd.subtract(switchProbeSet.getMatch(), matchBdd);
                    if (this.bdd.oneSAT(tmp) != 0) {
                        var = this.bdd.subtract(networkProbeSet.getMatch(), matchBdd);

                        if (bdd.oneSAT(var) == 0) {
                            // DO NOTHING
                            switchProbeSet.setMatch(tmp);

                            BDDTreeNode tmpNode = node.getParent();
                            while (tmpNode != null) {
                                var = this.bdd.subtract(tmpNode.getMatch(), matchBdd);
                                tmpNode.setMatch(var);
                                tmpNode = tmpNode.getParent();
                            }

                            bddRules.add(switchProbeSet);
                        } else {
                            networkProbeSet.setMatch(this.bdd.and(tmp, var));
                            switchProbeSet.setMatch(tmp);

                            BDDTreeNode tmpNode = node.getParent();
                            while (tmpNode != null) {
                                var = this.bdd.subtract(tmpNode.getMatch(), matchBdd);
                                tmpNode.setMatch(var);
                                tmpNode = tmpNode.getParent();
                            }
                        }



                        // TODO
                    } else {
                        // TODO Update network probe set
                        // networkProbeSet.removeRouter(this);
                    }
                }
            }

        }


        // BDDTreeNode node = this.complementTree.query(switchProbeSet.getMatch());
        // TODO
    /*
    if (networkProbeSet == null) {
        switchProbeSet.addRouter(this);
        probeSets.add(probeSet);
    } else {
        networkProbeSet.addRouter(this);
        probeSets.add(networkProbeSet);
    }
    */
        return bddRules;
    }


    public ArrayList<NetworkProbeSet> removeRule(String match) {
        RouterRule rule = this.ruleHashMap.get(match);

        ArrayList<NetworkProbeSet> networkProbeSets = new ArrayList<>();
        if (rule != null) {
            for (SwitchProbeSet switchProbeSet:rule.getSwitchProbeSets()) {
                networkProbeSets.add(switchProbeSet.getNetworkProbeSet());
                BDDTreeNode node = switchProbeSet.getNode();
                int var = node.getMatch();

                node.setMatch(this.bdd.getFalse());
                node.setSwitchProbeSet(null);
                // node.setNetworkProbeSet(null);
                node = node.getParent();

                while (node != null) {

                    int m = node.getMatch();

                    m = this.bdd.subtract(m, var);

                    node.setMatch(m);

                    node = node.getParent();
                }
                this.switchProbeSets.remove(switchProbeSet);
                this.networkProbeSets.remove(switchProbeSet.getNetworkProbeSet());
            }
            this.rules.remove(rule);
            this.ruleHashMap.remove(match);
        }
        return networkProbeSets;
    }

    ArrayList<NetworkProbeSet> getNetworkProbeSets() {
        return networkProbeSets;
    }

    public void internalTest() {
        this.addIPv4withPrefix("169.154.229.160/27", "1", "2");
        this.addIPv4withPrefix("1.0.0.0/8", "3", "2");

        // int n = this.bdd.subtract(this.rules.get(1), this.rules.get(0));

        // this.bdd.print(this.rules.get(0));
    }

    private HashMap<String, Short> neighbors;

    void addNeighbor(String name, short port) {
        neighbors.put(name, port);
    }

    void addNeighbor(String name) {
        short port = (short)(neighbors.size() + 1);
        neighbors.put(name, port);
    }

    short getPort(String name) {
        return neighbors.get(name);
    }
}
