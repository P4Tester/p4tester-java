package org.p4tester;

import java.util.ArrayList;

public class IPv6Router extends Router {
    IPv6Router(P4TesterBDD bdd, String name, int maxRules, boolean enablePriority) {
        super(bdd, name, maxRules, enablePriority);
    }


    @Override
    public void addIPv4withPrefix(String str, String port, String nextHop) {

    }

    public void addIPv6withPrefix(String str, String port, String nextHop) {
        if (str == null) {
            return;
        }
        String[] ipv6 =  str.split("/");

        if (ipv6.length != 2) {
            return;
        }

        if (this.rules.size() > MAX_RULES) {
            return;
        }

        String[] ip = ipv6[0].split(":");
        int prefix = Integer.valueOf(ipv6[1]);
        ArrayList<Integer> bits = new ArrayList<>();

        int x = 8 - ip.length + 1;

        for (String i:ip) {
            if (i.equals("")) {
                for (int j = 0; j < x * 16; j ++) {
                    bits.add(0);
                }
            } else {
                int value = Integer.valueOf(i, 16);
                for (int j = 0; j < 16; j++) {
                    bits.add(value / (1 << (15 - j)));
                    value = value % (1 << (15 - j));
                }
            }
        }

        int y = ipv6[0].split("::").length;
        if (y == 1) {
            for (int j = 0; j < x*16; j ++) {
                bits.add(0);
            }
        }

        int rule;
        if (prefix > 0) {
            rule = this.bdd.getVar(0);

            if (bits.get(0) == 0) {
                rule = this.bdd.getNotVar(0);
            }

            for (int i = 1; i < 128; i++) {
                if (i < prefix) {
                    if (bits.get(i) == 1) {
                        int var = rule;
                        rule = this.bdd.and(rule, this.bdd.getVar(i));
                        this.bdd.deref(var);
                    } else {
                        int var = rule;
                        rule = this.bdd.and(rule, this.bdd.getNotVar(i));
                        this.bdd.deref(var);
                    }
                } else {
                    int var = this.bdd.or(this.bdd.getVar(i), this.bdd.getNotVar(i));
                    rule = this.bdd.and(rule, var);
                    this.bdd.deref(var);
                }
            }
        } else {
            rule = this.bdd.getTrue();
        }
        //       System.out.println(str);
        //     this.bdd.print(rule);
        RouterRule routerRule = new RouterRule(str, rule, prefix, port, nextHop);
        this.rules.add(routerRule);
        this.ruleHashMap.put(str, routerRule);
    }
}
