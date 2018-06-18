package org.p4tester;


import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.*;


class SwitchPortPair {
    private Router router;
    private short port;

    SwitchPortPair(Router router, short port) {
        this.router = router;
        this.port = port;
    }

    public void setPort(short port) {
        this.port = port;
    }

    public void setRouter(Router router) {
        this.router = router;
    }

    public short getPort() {
        return port;
    }

    public Router getRouter() {
        return router;
    }
}

public class P4Tester {
    private ArrayList<Router> routers;
    private ArrayList<NetworkProbeSet> probeSets;
    private P4TesterBDD bdd;
    private HashMap<String, Router> routerMap;
    private HashMap<String, ArrayList<Router>> topoMap;
    private Router root;
    private ArrayList<SwitchPortPair> path;
    private static final String[] INTERNET2_ROUTERS = {
            "chic",
            "atla",
            "hous",
            "kans",
            "losa",
            "newy32aoa" ,
            "salt",
            "wash" ,
            "seat"
    };

    private static final String[] STANFORD_ROUTERS = {
            "bbra",
            //"bbrb",
            "boza",
            //"bozb",
            "coza",
            //"cozb",
            "goza",
            //"gozb",
            "poza",
            //"pozb",
            "roza",
            //"rozb",
            "soza",
            //"sozb",
            "yoza",
            //"yozb"
    };

    private static final String[] UNVv6_ROUTERS = {
            "1",
            "2",
            "3",
            "4",
            "5",
            "6",
            "7",
            "8",
            "9",
            "10",
            "11",
            "12",
            "13",
            "14",
            "15",
            "16",
            "17",
            "18",
            "19",
            "20",
            "21",
            "22",
            "23"
    };

    P4Tester(P4TesterBDD bdd) {
        this.bdd = bdd;
        this.routerMap = new HashMap<>();
        this.probeSets = new ArrayList<>();
        this.routers = new ArrayList<>();
        this.topoMap = new HashMap<>();
        this.path = new ArrayList<>();

    }

    public void parseInternet2(String routerName, String fileName) {
        try {
            InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(fileName));
            BufferedReader reader = new BufferedReader(inputStreamReader);

            String line;
            Router router = new Router(bdd, routerName);

            for (int i =0 ;i < 7; i++) {
                reader.readLine();
            }
            String match;
            String port;
            String nextHop;
            while((line = reader.readLine()) != null) {
                match = null;
                port = null;
                nextHop = null;

                if (line.contains(":")) {
                    continue;
                }

                try {
                    if (line.contains("indr")) {
                        String[] info = line.split(" ");

                        for (String i : info) {
                            if (i.contains("/")) {
                                match = i;
                                break;
                            }
                        }
                        line = reader.readLine();
                        info = line.split(" ");

                        for (String i : info) {
                            if (nextHop == null) {
                                if (i.contains(".")) {
                                    nextHop = i;
                                }
                            }
                            port = i;
                        }
                        router.addIPv4withPrefix(match, port, nextHop);
                    } else if (line.contains("ucst")) {
                        String[] info = line.split(" ");

                        for (String i : info) {
                            if (match == null) {
                                if (i.contains("/")) {
                                    match = i;
                                }
                            } else if (nextHop == null) {
                                if (i.contains(".")) {
                                    nextHop = i;
                                }
                            }
                            port = i;
                        }

                        if (match != null) {
                            // router.addIPv4withPrefix(match, port, nextHop);
                        }
                    } else if (line.contains("locl")) {
                        String[] info = line.split(" ");

                        for (String i : info) {
                            if (match == null) {
                                if (i.contains("/")) {
                                    match = i;
                                }
                            } else if (i.contains(".")) {
                                nextHop = i;
                                break;
                            }
                        }
                        router.addLocalIp(nextHop);
                    }
                } catch (Exception e) {
                    // e.printStackTrace();
                }
            }
            router.generateProbeSets();
            this.routers.add(router);
            routerMap.put(routerName, router);
            inputStreamReader.close();
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void parseCompressedStanford(String routerName, String fileName) {
        try {
            InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(fileName));
            BufferedReader reader = new BufferedReader(inputStreamReader);

            String line;
            Router router = new Router(bdd, routerName);

            reader.readLine();
            String match;
            String port;
            String nextHop;
            while((line = reader.readLine()) != null) {
                match = null;
                port = null;
                nextHop = null;

                try {
                    String[] info = line.split(" ");
                    match = info[0];
                    port = info[1];
                    nextHop = info[1];

                    if (match != null && nextHop != null) {
                        router.addIPv4withPrefix(match, port, nextHop);
                    } else {
                        // System.out.println(line);
                    }


                } catch (Exception e) {

                }
            }
            router.generateProbeSets();
            this.routers.add(router);
            routerMap.put(routerName, router);
            inputStreamReader.close();
            reader.close();
            // System.out.println(routerName + " " + router.getRules().size());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void parseUNVv6(String routerName, String fileName){
        try{
            InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(fileName));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line;
            IPv6Router router = new IPv6Router(bdd, routerName);

            for (int i =0 ;i < 13; i++) {
                reader.readLine();
            }
            String match;
            String port;
            String nextHop;
            while((line = reader.readLine()) != null){
                match = null;
                port = null;
                nextHop = null;
                if (line.contains("via")) {
                    continue;
                }
                try{
                    String[] info = line.split(" |,");
                    for (String i : info) {
                        if (match == null && i.contains(":")) {
                            match = i;
                            //System.out.println(match);
                        }else if (nextHop == null) {
                            if (i.contains(":") && !i.contains("/")) {
                                nextHop = i;
                                //System.out.println(nextHop);
                            }
                        }
                    }
                    if (nextHop == null){
                        line = reader.readLine();
                        if(line== null) {
                            break;
                        }
                        info = line.split(" |,");

                        for (String i : info) {
                            if (nextHop == null) {
                                if (i.contains(":") && !i.contains("/")) {
                                    nextHop = i;
                                    //System.out.println(nextHop);
                                }
                            }
                            //port = i;
                        }
                    }
                    router.addIPv6withPrefix(match, port, nextHop);
                    router.generateProbeSets();
                    this.routers.add(router);
                    routerMap.put(routerName, router);
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
            inputStreamReader.close();
            reader.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    @Deprecated
    public void encodeStanford(String routerName, String fileName) {
        try {
            InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(fileName));
            BufferedReader reader = new BufferedReader(inputStreamReader);

            String line;
            Router router = new Router(bdd, routerName);

            for (int i =0 ;i < 2; i++) {
                reader.readLine();
            }
            String match;
            String port;
            String nextHop;
            while((line = reader.readLine()) != null) {
                match = null;
                port = null;
                nextHop = null;

                if (line.contains(":")) {
                    continue;
                }

                try {
                    String[] info = line.split(" ");

                    for (String i : info) {
                        if (match == null) {
                            if (i.contains("/")) {
                                match = i;
                            }
                        } else if (nextHop == null) {
                            if (i.contains(".")) {
                                nextHop = i;
                            }
                        }
                        port = i;
                    }

                    if (match != null && nextHop != null) {
                        router.addIPv4withPrefix(match, port, nextHop);
                    } else {
                        // System.out.println(line);
                    }


                } catch (Exception e) {

                }
            }
            router.generateProbeSets();
            this.routers.add(router);
            routerMap.put(routerName, router);
            inputStreamReader.close();
            reader.close();
            System.out.println(routerName + " " + router.getRules().size());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public void startInternet2(boolean fast, boolean inject) {
        long startAll = System.nanoTime();
        long start = System.nanoTime();
        internet2ProbeConstruct();
 //       System.out.println("Step1 :" + (System.nanoTime() - start));
        System.out.println((System.nanoTime() - start));

        start = System.nanoTime();
        buildBDDTreeFast();
       // System.out.println("Step2 :" + (System.nanoTime() - start));
        System.out.println((System.nanoTime() - start));

        start = System.nanoTime();
        this.buildInternet2ST();
        generateProbes();
        //System.out.println("Step3 :" + (System.nanoTime() - start));
        //System.out.println("Total :" + (System.nanoTime() - startAll));
        System.out.println((System.nanoTime() - start));

        int count = 0;
        for (Router router:this.routers) {
            count += router.getRules().size();
        }

        System.out.println(count);
        System.out.println(this.probeSets.size());

        int routerId = (int) (this.routers.size()*Math.random());
        Router router = routers.get(routerId);
        int ruleId = (int) (router.getRules().size()*Math.random());
        RouterRule rule = router.getRules().get(ruleId);
        start = System.nanoTime();
        removeRule(router.getName(), rule.getMatchIp());
        System.out.println("Remove Rule :" + (System.nanoTime() - start));

        start = System.nanoTime();
        if (fast) {
            addRuleFast(router.getName(), rule.getMatchIp(), rule.getPort(), rule.getNextHop());
        } else {
            addRule(router.getName(), rule.getMatchIp(), rule.getPort(), rule.getNextHop());
        }
        System.out.println("Add Rule :" + (System.nanoTime() - start));


        if (inject) {
            P4TesterProbeProcessor probeProcessor = new P4TesterProbeProcessor(this.probeSets);
            ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);

            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    // System.out.println(probeSets.size());
                    probeProcessor.injectProbes();
                }
            };
            executor.scheduleAtFixedRate(runnable, 1, 3, TimeUnit.SECONDS);
        }
    }

    public void startStanford(boolean fast, boolean inject) {
        long start = System.nanoTime();
        stanfordProbeConstruct();
        System.out.println("Step1 :" + (System.nanoTime() - start));

        start = System.nanoTime();
        buildBDDTreeFast();
        System.out.println("Step2 :" + (System.nanoTime() - start));


        start = System.nanoTime();
        this.buildStanfordST();
        generateProbes();
        System.out.println("Step3 :" + (System.nanoTime() - start));

        System.out.println("Probes: " + this.probeSets.size());

        int routerId = (int) (this.routers.size()*Math.random());
        Router router = routers.get(routerId);
        int ruleId = (int) (router.getRules().size()*Math.random());
        RouterRule rule = router.getRules().get(ruleId);
        start = System.nanoTime();
        removeRule(router.getName(), rule.getMatchIp());
        System.out.println("Remove Rule :" + (System.nanoTime() - start));

        start = System.nanoTime();
        if (fast) {
            addRuleFast(router.getName(), rule.getMatchIp(), rule.getPort(), rule.getNextHop());
        } else {
            addRule(router.getName(), rule.getMatchIp(), rule.getPort(), rule.getNextHop());
        }
        System.out.println("Add Rule :" + (System.nanoTime() - start));

        if (inject) {
            P4TesterProbeProcessor probeProcessor = new P4TesterProbeProcessor(this.probeSets);
            ExecutorService executor = Executors.newFixedThreadPool(2);

            Runnable injectTask = new Runnable() {
                @Override
                public void run() {
                    // System.out.println(probeSets.size());
                    try {
                        while (true) {
                            Thread.sleep(2000);
                            probeProcessor.injectProbes();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    // for (NetworkProbeSet networkProbeSet:probeSets) {
                    //    networkProbeSet.generateProbes();
                    //}
                   /*
                    ArrayList<byte[]> x = probeSets.get(300).generateProbes();
                    //for (int i = 42; i < x.get(0).length ; i+=2) {
                        System.out.print(" " + x.get(0)[i]);
                    }
                    System.out.println();
                    for (Router router1:probeSets.get(2).getRouters()) {
                        System.out.println(router1.getName());
                    }*/

                }
            };

            Runnable collectTask = new Runnable() {
                @Override
                public void run() {
                    probeProcessor.loop();
                }
            };

            executor.execute(injectTask);
            executor.execute(collectTask);
        }

        //

    }

    public void startUNVv6(boolean fast, boolean inject) {

        long start = System.nanoTime();
        unvv6ProbeConstruct();
        System.out.println("Step1 :" + (System.nanoTime() - start));

        /*

        start = System.nanoTime();
        buildBDDTreeFast();
        System.out.println("Step2 :" + (System.nanoTime() - start));


        start = System.nanoTime();
        this.buildInternet2ST();
        generateProbes();
        System.out.println("Step3 :" + (System.nanoTime() - start));


        System.out.println("Probes: " + this.probeSets.size());

        int routerId = (int) (this.routers.size()*Math.random());
        Router router = routers.get(routerId);
        int ruleId = (int) (router.getRules().size()*Math.random());
        RouterRule rule = router.getRules().get(ruleId);
        start = System.nanoTime();
        removeRule(router.getName(), rule.getMatchIp());
        System.out.println("Remove Rule :" + (System.nanoTime() - start));

        start = System.nanoTime();
        if (fast) {
            addRuleFast(router.getName(), rule.getMatchIp(), rule.getPort(), rule.getNextHop());
        } else {
            addRule(router.getName(), rule.getMatchIp(), rule.getPort(), rule.getNextHop());
        }
        System.out.println("Add Rule :" + (System.nanoTime() - start));


        if (inject) {
            P4TesterProbeProcessor probeProcessor = new P4TesterProbeProcessor(this.probeSets);
            ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);

            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    // System.out.println(probeSets.size());
                    probeProcessor.injectProbes();
                }
            };
            executor.scheduleAtFixedRate(runnable, 1, 1, TimeUnit.SECONDS);
        }
        */
    }

    public ArrayList<Short> getForwardPortList(String name) {
        ArrayList<Short> arrayList = new ArrayList<>();
        for (int i= 1; i < this.STANFORD_ROUTERS.length; i++) {
            if (this.STANFORD_ROUTERS[i].equals(name)) {
                arrayList.add((short) i);
                break;
            }
        }
        return arrayList;
    }

    public ArrayList<Short> getBackwordPortList(String name) {
        ArrayList<Short> arrayList = new ArrayList<>();
        arrayList.add((short) 0);
        if (!root.getName().equals(name)) {
            arrayList.add((short) 0);
        }
        return arrayList;
    }

    @Deprecated
    public void buildBDDTree() {
        // Router router = this.routers.get(0);
        /*
        for (Router router:this.routers) {
            for (ProbeSet probeSet : router.getProbeSets()) {
                tree.insert(probeSet);
            }
        }
        */
        //this.probeSets = tree.getLeafNodes();
    }

    public void buildBDDTreeFast() {
        // Router router = this.routers.get(0);

        HashSet<SwitchProbeSet> visitedProbeSets = new HashSet<>();
        for (int i = 0; i < routers.size(); i++) {
            ArrayList<SwitchProbeSet> probeSets = routers.get(i).getSwitchProbeSets();

            for (int j = 0; j < probeSets.size(); j++) {
                if (!visitedProbeSets.contains(probeSets.get(j))) {
                    visitedProbeSets.add(probeSets.get(j));
                    int target = probeSets.get(j).getMatch();

                    NetworkProbeSet networkProbeSet = new NetworkProbeSet(bdd, this);
                    networkProbeSet.addSwitchProbeSet(probeSets.get(j));
                    probeSets.get(j).setNetworkProbeSet(networkProbeSet);

                    for (int k = 0; k < routers.size(); k++) {
                        if (k != i) {
                            BDDTreeNode treeNode = routers.get(k).getTree().query(target);
                            if (treeNode != null) {
                                visitedProbeSets.add(treeNode.getSwitchProbeSet());
                                networkProbeSet.addSwitchProbeSet(treeNode.getSwitchProbeSet());
                                target = networkProbeSet.getMatch();
                                treeNode.setNetworkProbeSet(networkProbeSet);
                                treeNode.getSwitchProbeSet().setNetworkProbeSet(networkProbeSet);
                            } else {
                                // routers.get(k).getComplementTree().insertNetworkProbeSet(networkProbeSet);
                            }
                        }
                    }
                    this.probeSets.add(networkProbeSet);
                }
            }
        }

        // System.out.println(this.probeSets.size());
        // int count = 0;
        // for (Router router : this.routers) {
        // count += router.getProbeSets().size();
        // System.out.println(count);
        //}
        // this.probeSets = tree.getLeafNodes();
    }



    private void buildInternet2ST() {
        ArrayList<Router> children = new ArrayList<>();
        //children.add(this.routers.get(1));
        //children.add(this.routers.get(2));
        //children.add(this.routers.get(3));

        this.topoMap.put(INTERNET2_ROUTERS[0], children);

        children = new ArrayList<>();
        //children.add(this.routers.get(4));
        //children.add(this.routers.get(5));

        //this.topoMap.put(INTERNET2_ROUTERS[1], children);

        children = new ArrayList<>();
        //children.add(this.routers.get(6));
        //children.add(this.routers.get(7));

        //this.topoMap.put(INTERNET2_ROUTERS[2], children);

        children = new ArrayList<>();
        //children.add(this.routers.get(8));

        //this.topoMap.put(INTERNET2_ROUTERS[5], children);

        this.root = this.routers.get(0);
    }

    private void buildStanfordST() {
        ArrayList<Router> children = new ArrayList<>();
        children.add(this.routers.get(1));
        children.add(this.routers.get(2));
        children.add(this.routers.get(3));
        children.add(this.routers.get(4));
        children.add(this.routers.get(5));
        children.add(this.routers.get(6));
        children.add(this.routers.get(7));

        this.topoMap.put(STANFORD_ROUTERS[0], children);

        this.routers.get(0).addNeighbor(this.routers.get(1).getName(), (short) 1);
        this.routers.get(0).addNeighbor(this.routers.get(2).getName(), (short) 2);
        this.routers.get(0).addNeighbor(this.routers.get(3).getName(), (short) 3);
        this.routers.get(0).addNeighbor(this.routers.get(4).getName(), (short) 4);
        this.routers.get(0).addNeighbor(this.routers.get(5).getName(), (short) 5);
        this.routers.get(0).addNeighbor(this.routers.get(6).getName(), (short) 6);
        this.routers.get(0).addNeighbor(this.routers.get(7).getName(), (short) 7);

        this.routers.get(1).addNeighbor(this.routers.get(0).getName(), (short) 0);
        this.routers.get(2).addNeighbor(this.routers.get(0).getName(), (short) 0);
        this.routers.get(3).addNeighbor(this.routers.get(0).getName(), (short) 0);
        this.routers.get(4).addNeighbor(this.routers.get(0).getName(), (short) 0);
        this.routers.get(5).addNeighbor(this.routers.get(0).getName(), (short) 0);
        this.routers.get(6).addNeighbor(this.routers.get(0).getName(), (short) 0);
        this.routers.get(7).addNeighbor(this.routers.get(0).getName(), (short) 0);


        this.root = this.routers.get(0);
    }


    private void _buildStanfordST() {
        ArrayList<Router> children = new ArrayList<>();
        children.add(this.routers.get(1));
        children.add(this.routers.get(2));
        children.add(this.routers.get(3));

        this.topoMap.put(STANFORD_ROUTERS[0], children);

        children = new ArrayList<>();
        children.add(this.routers.get(4));
        children.add(this.routers.get(5));

        this.topoMap.put(STANFORD_ROUTERS[1], children);

        children = new ArrayList<>();
        children.add(this.routers.get(6));
        children.add(this.routers.get(7));

        this.topoMap.put(STANFORD_ROUTERS[2], children);

        children = new ArrayList<>();
        children.add(this.routers.get(8));
        children.add(this.routers.get(9));
        children.add(this.routers.get(10));

        this.topoMap.put(STANFORD_ROUTERS[3], children);

        children = new ArrayList<>();
        children.add(this.routers.get(11));
        children.add(this.routers.get(12));
        children.add(this.routers.get(13));

        this.topoMap.put(STANFORD_ROUTERS[5], children);


        children = new ArrayList<>();
        children.add(this.routers.get(14));
        children.add(this.routers.get(15));

        this.topoMap.put(STANFORD_ROUTERS[10], children);


        this.root = this.routers.get(0);
    }



    public ArrayList<SwitchPortPair> getPath() {
        return path;
    }

    public void generateProbes() {
        traverseST(root);
    }

    private void traverseST(Router router) {
        try {
            for (NetworkProbeSet probeSet : router.getNetworkProbeSets()) {
                probeSet.traverse(router, this.path.size());
            }
        } catch (Exception e) {
            System.out.println(router.getName());
        }

        if (this.topoMap.containsKey(router.getName())) {
            for (Router child: this.topoMap.get(router.getName())) {
                path.add(new SwitchPortPair(router, router.getPort(child.getName())));
                traverseST(child);
                path.add(new SwitchPortPair(child, child.getPort(router.getName())));
            }
        }
    }

    public void unvv6ProbeConstruct() {
        ArrayList<Thread> constructors = new ArrayList<>();
        for (String s: UNVv6_ROUTERS) {
            String fileName = "resource/Tsinghua_route/Tsinghua_route/" +  s + ".csv";
            parseUNVv6(s, fileName);
            // Thread t = new Thread(new P4TesterProbeSetConstructor(this, s, fileName));
            // t.run();
            // constructors.add(t);
        }
        for (Thread constructor: constructors) {
            constructor.start();
        }

        for (Thread constructor: constructors) {
            try {
                constructor.join();
            }
            catch (Exception e) {
                System.out.println("JOIN");
                e.printStackTrace();
            }
        }
        for (Router router:this.routers) {
            router.buildTree();
        }
    }

    public void internet2ProbeConstruct() {
        ArrayList<Thread> constructors = new ArrayList<>();
        for (String s: INTERNET2_ROUTERS) {
            String fileName = "resource/Internet2/" +  s + "-show_route_forwarding-table_table_default.xml";
            Thread t = new Thread(new P4TesterProbeSetConstructor(this, s, fileName));
            t.run();
            // constructors.add(t);
        }
        for (Thread constructor: constructors) {
            constructor.start();
        }

        for (Thread constructor: constructors) {
            try {
                constructor.join();
            }
            catch (Exception e) {
                System.out.println("JOIN");
                e.printStackTrace();
            }
        }
        for (Router router:this.routers) {
            router.buildTree();
        }
    }

    public void stanfordProbeConstruct() {
        ArrayList<Thread> constructors = new ArrayList<>();
        for (String s: STANFORD_ROUTERS) {
            String fileName = "resource/Stanford_backbone/" + s + ".txt";
            // Thread t = new Thread(new P4TesterProbeSetConstructor(this, s, fileName));
            //t.run();
            this.parseCompressedStanford(s, fileName);
            // constructors.add(t);
        }
        for (Thread constructor: constructors) {
            constructor.start();
        }

        for (Thread constructor: constructors) {
            try {
                constructor.join();
            }
            catch (Exception e) {
                System.out.println("JOIN");
                e.printStackTrace();
            }
        }
        for (Router router:this.routers) {
            router.buildTree();
        }
    }


    public void internalTest() {
        // this.parseInternet2("", "resource/Internet2/hous-show_route_forwarding-table_table_default.xml");
        this.encodeStanford("bbra", "resource/Stanford_backbone/bbra_rtr_route.txt");
        System.out.println(this.routers.get(0).getRules().size());
    }


    public void updateRule(Router router) {
        router.regenerateProbeSets();
        router.buildTree();
        for (NetworkProbeSet probeSet:this.probeSets) {
            BDDTreeNode node = router.getTree().query(probeSet.getMatch());
            if (node != null) {
                probeSet.addRouter(router);
                int var = this.bdd.and(probeSet.getMatch(), node.getMatch());
                probeSet.setMatch(var);
                probeSet.addSwitchProbeSet(node.getSwitchProbeSet());
                node.getSwitchProbeSet().setNetworkProbeSet(probeSet);
                router.addNetworkProbeSets(probeSet);
            } else {
                if (probeSet.getRouters().contains(router)) {
                    probeSet.getRouters().remove(router);
                    if (probeSet.getRouters().size() == 0) {
                        this.probeSets.remove(probeSet);
                    }
                }
            }
        }
        for (SwitchProbeSet switchProbeSet:router.getSwitchProbeSets()) {
            if (switchProbeSet.getNetworkProbeSet() == null) {
                NetworkProbeSet networkProbeSet = new NetworkProbeSet(this.bdd, this);
                networkProbeSet.setMatch(switchProbeSet.getMatch());
                networkProbeSet.addRouter(router);
                switchProbeSet.setNetworkProbeSet(networkProbeSet);
                this.probeSets.add(networkProbeSet);
            }
        }
    }

    public void removeRule(String routerName, String match) {
        Router router = this.routerMap.get(routerName);
        if (router != null) {
            router.removeRule("35.0.0.0/8");
            updateRule(router);
        }
    }


    public void addRule(String routerName, String match, String port, String nextHop) {
        Router router = this.routerMap.get(routerName);
        if (router != null) {
            router.addRule(match, port, nextHop);
            updateRule(router);
        }
    }

    public void addRuleFast(String routerName, String match, String port, String nextHop) {
        Router router = this.routerMap.get(routerName);
        if (router != null) {
            RouterRule rule = router.addRule(match, port, nextHop);
            int var = this.bdd.subtract(router.getTree().getRoot().getMatch(), rule.getMatchBdd());
            if (this.bdd.oneSAT(var) != 0) {
                NetworkProbeSet networkProbeSet = new NetworkProbeSet(bdd, this);
                networkProbeSet.setMatch(var);
                networkProbeSet.addRouter(router);
                this.probeSets.add(networkProbeSet);
            }
        }
    }
}

class P4TesterProbeSetConstructor implements Runnable {
    private P4Tester p4tester;
    private String router;
    private String fileName;

    P4TesterProbeSetConstructor(P4Tester tester, String router, String fileName) {
        this.router = router;
        this.p4tester = tester;
        this.fileName = fileName;
    }
    @Override
    public void run() {
        this.p4tester.parseInternet2(router, fileName);
    }
}
