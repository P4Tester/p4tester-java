package org.p4tester;

import java.util.ArrayList;

class BDDTreeNode extends ProbeSet {
    private BDDTreeNode parent;
    private P4TesterBDD bdd;
    private ArrayList<Router> routers;
    private ArrayList<BDDTreeNode> children;
    private int leafNum;
    private int visitedNum;
    private SwitchProbeSet switchProbeSet;
    private NetworkProbeSet networkProbeSet;

    BDDTreeNode (P4TesterBDD bdd, int match, int leaf) {
        super(match);
        this.bdd = bdd;
        this.routers = new ArrayList<>();
        this.children = new ArrayList<>();
        this.parent = null;
        this.visitedNum = 0;
        this.leafNum = leaf;
        this.switchProbeSet = null;
        this.networkProbeSet = null;
    }

    @Deprecated
    BDDTreeNode (ProbeSet probeSet, P4TesterBDD bdd) {
        // this.probeSet = probeSet;
        // this.complement = probeSet.getExp();
        super(probeSet.getMatch());
        this.routers = new ArrayList<>();
        this.children = new ArrayList<>();
        this.parent = null;
        this.visitedNum = 0;
        this.leafNum = 0;
    }


    @Deprecated
    public void setNetworkProbeSet(NetworkProbeSet networkProbeSet) {
        this.networkProbeSet = networkProbeSet;
    }

    void setSwitchProbeSet(SwitchProbeSet switchProbeSet) {
        this.switchProbeSet = switchProbeSet;
    }


    /**
     *
     * @param name the name of the visited router
     * @return if visited then true else false
     */
    public boolean isVisited(Router name) {
        return routers.contains(name);
    }

    void visit() {
        this.visitedNum = 1;
    }

    BDDTreeNode getParent() {
        return parent;
    }

    SwitchProbeSet getSwitchProbeSet() {
        return switchProbeSet;
    }

    /**
     * Merge a child node.
     * @param node the child node
     */
    BDDTreeNode mergeChild(BDDTreeNode node) {
        this.addChild(node);
        int var = match;
        if (this.match == 0) {
            this.match = node.match;
        } else {
            this.match = this.bdd.or(node.match, var);
        }
        this.bdd.deref(var);
        return this;
    }


    void removeChild (BDDTreeNode node) {
        this.children.remove(node);
    }

    void addChild(BDDTreeNode node) {
        this.children.add(node);
        // this.addRouters(node.getRouters());
        // node.addRouters(this.getRouters());
        // this.complement = bdd.and(complement, bdd.not(node.probeSet.getExp()));
        node.parent = this;
        this.leafNum += node.leafNum;
    }

    private void addRouter(Router router) {
        if (!routers.contains(router)) {
            this.routers.add(router);
        }
    }

    public void setMatch(int match) {
        this.match = match;
    }

    int getLeafNum() {
        return leafNum;
    }

    int getVisitedNum() {
        return visitedNum;
    }

    private void addRouters(ArrayList<Router> routers) {
        for(Router router: routers) {
            this.addRouter(router);
        }
    }

    ArrayList<BDDTreeNode> getChildren() {
        return children;
    }

    private ArrayList<Router> getRouters() {
        return routers;
    }

    boolean isLeaf() {
        return this.children.size() == 0;
    }
}

public class BDDTree {
    private BDDTreeNode root;
    private P4TesterBDD bdd;
    private ArrayList<BDDTreeNode> nodes;

    @Deprecated
    BDDTree (P4TesterBDD bdd) {
        root = new BDDTreeNode(bdd, bdd.getFalse(), 0);
        this.bdd = bdd;
        this.nodes = new ArrayList<>();
    }

    /**
     * Constructor for BDDTree
     * @param bdd the bdd instance
     * @param root the root node
     * @param nodes the leaf ndoes
     */
    private BDDTree (P4TesterBDD bdd, BDDTreeNode root, ArrayList<BDDTreeNode> nodes) {
        this.root = root;
        this.bdd = bdd;
        if (nodes == null) {
            this.nodes = new ArrayList<>();
        } else {
            this.nodes = nodes;
        }
    }

    @Deprecated
    void insert(ProbeSet probeSet) {
        BDDTreeNode node = root;
        boolean whileContinue = true;
        boolean enableComplement;
        while(whileContinue) {
            whileContinue = false;
            enableComplement = true;
            /*
            for (BDDTreeNode c: node.getChildren()) {
                if (!c.isVisited(probeSet.getRouters().get(0))) {
                    if (bdd.isOverlap(c.getProbeSet().getExp(), probeSet.getExp())) {
                        enableComplement = false;
                        if (bdd.isSubset(c.getProbeSet().getExp(), probeSet.getExp())) {
                            c.addRouters(probeSet.getRouters());
                            break;
                        } else {
                            c.addRouters(probeSet.getRouters());
                            node = c;
                            whileContinue = true;
                            break;
                        }
                    }
                }
            }
            if (enableComplement) {
            //                if (bdd.isOverlap(node.getComplement(), probeSet.getExp())) {
                    ProbeSet tmp = new ProbeSet(bdd.and(node.getProbeSet().getExp(), probeSet.getExp()));
                    BDDTreeNode tmpNode = new BDDTreeNode(tmp, bdd);
                    tmpNode.addRouters(probeSet.getRouters());
                    node.addChild(tmpNode);
                    nodes.add(tmpNode);
            //              }
            }
            */
        }
    }

    @Deprecated
    void insertSwitchProbeSet(SwitchProbeSet probeSet) {
        if (this.nodes.size() < 2) {
            BDDTreeNode node = new BDDTreeNode(bdd, probeSet.getMatch(), 1);
            root.mergeChild(node);
            this.nodes.add(node);
        } else {
            BDDTreeNode last = this.nodes.get(this.nodes.size() - 1);
            last.getParent().removeChild(last);
            BDDTreeNode newNode = new BDDTreeNode(bdd, probeSet.getMatch(), 1);
            newNode.setSwitchProbeSet(probeSet);
            BDDTreeNode parentNode = new BDDTreeNode(bdd, 0, 1);
            last.getParent().addChild(parentNode);
            parentNode.mergeChild(last);
            parentNode.mergeChild(newNode);
            BDDTreeNode node = newNode.getParent().getParent();
            while (node != null) {
                int var = node.getMatch();
                node.setMatch(this.bdd.or(var, probeSet.getMatch()));
                this.bdd.deref(var);
                node = node.getParent();
            }
            this.nodes.add(newNode);
        }
    }

    @Deprecated
    void insertNetworkProbeSet(NetworkProbeSet probeSet) {
        if (this.nodes.size() < 2) {
            BDDTreeNode node = new BDDTreeNode(bdd, probeSet.getMatch(), 1);
            root.mergeChild(node);
            this.nodes.add(node);
        } else {
            BDDTreeNode last = this.nodes.get(this.nodes.size() - 1);
            last.getParent().removeChild(last);
            BDDTreeNode newNode = new BDDTreeNode(bdd, probeSet.getMatch(), 1);
            newNode.setNetworkProbeSet(probeSet);
            BDDTreeNode parentNode = new BDDTreeNode(bdd, 0, 1);
            last.getParent().addChild(parentNode);
            parentNode.addChild(last);
            parentNode.addChild(newNode);
            BDDTreeNode node = newNode.getParent().getParent();
            while (node != null) {
                int var = node.getMatch();
                node.setMatch(this.bdd.or(var, probeSet.getMatch()));
                this.bdd.deref(var);
                node = node.getParent();
            }
            this.nodes.add(newNode);
        }
    }

    /**
     * Query a matched BDDTreeNode
     * @param target the target BDD
     * @return the BDDTree node
     */
    BDDTreeNode query(int target) {
        BDDTreeNode node = root;
        boolean found = true;

        if (!bdd.isOverlap(root.getMatch(), target)) {
            return null;
        }

        while (!node.isLeaf() && found) {
            found = false;
            // Search the left child
            BDDTreeNode child = node.getChildren().get(0);
            if (child.getLeafNum() > child.getVisitedNum()) {
                if (bdd.isOverlap(child.getMatch(), target)) {
                    node.visit();
                    node = child;
                    found = true;
                }
            }

            // Search the right child
            if (!found) {
                child = node.getChildren().get(1);
                if (child.getLeafNum() > child.getVisitedNum()) {
                    node.visit();
                    node = child;
                    found = true;
                } // else {
                    // System.out.println("1");
                // }
            }
        }
        if (found) {
            return node;
        } else {
            return null;
        }
    }

    /**
     * Recursively find the match nodes
     * @param node current node
     * @param target target BDD
     * @param nodes matched nodes
     */
    private void queryMatchNodesRecur(BDDTreeNode node, int target, ArrayList<BDDTreeNode> nodes) {

        if (node.isLeaf()) {
            nodes.add(node);
            return;
        }

        BDDTreeNode child = node.getChildren().get(0);
        if (bdd.isOverlap(child.getMatch(), target)) {
            queryMatchNodesRecur(child, target, nodes);
        }
        child = node.getChildren().get(1);
        if (bdd.isOverlap(child.getMatch(), target)) {
            queryMatchNodesRecur(child, target, nodes);
        }
    }

    /**
     * Get all BDDTree nodes that can match the target
     * @param target target BDD
     * @return a list of matched nodes
     */
    ArrayList<BDDTreeNode> queryMatchNodes(int target) {
        ArrayList<BDDTreeNode> nodes = new ArrayList<>();
        if (bdd.isOverlap(root.getMatch(), target)) {
            queryMatchNodesRecur(root, target, nodes);
        }
        return nodes;
    }

    @Deprecated
    public ArrayList<ProbeSet> getLeafNodes() {

        ArrayList<ProbeSet> probeSets = new ArrayList<>();

        /*
        ArrayList<BDDTreeNode> nodes = new ArrayList<>();
        for(BDDTreeNode node: this.nodes) {
            if (node.isLeaf()) {
                ProbeSet probeSet = new ProbeSet(node.getProbeSet().getExp());

                BDDTreeNode tmp = node;

                while(tmp != null && !nodes.contains(tmp.getParent())) {
                    probeSet.addRouters(tmp.getRouters());
                    nodes.add(tmp);
                    // System.out.println(tmp.getRouters().size());
                    tmp = tmp.getParent();
                }

                probeSet.addRouters(node.getRouters());

                for (Router r: probeSet.getRouters()) {
                    r.addNetworkProbeSets(probeSet);
                }
                probeSets.add(probeSet);
            }
        }
        */

        return  probeSets;
    }

    /**
     * Get the root node of the tree.
     * @return root node
     */
    public BDDTreeNode getRoot() {
        return root;
    }

    /**
     * Build a BDD tree from a probe set.
     * @param bdd bdd instance
     * @param probeSets probe sets for leaf nodes.
     * @return an instance of BDD tree
     */
    static BDDTree buildBinary(P4TesterBDD bdd, ArrayList<SwitchProbeSet> probeSets) {
        BDDTreeNode[] nodes = new BDDTreeNode[probeSets.size()];
        ArrayList<BDDTreeNode> leafNodes = new ArrayList<BDDTreeNode>();
        for (int i = 0; i < nodes.length; i++) {
            nodes[i] = new BDDTreeNode(bdd, probeSets.get(i).getMatch(), 1);
            nodes[i].setSwitchProbeSet(probeSets.get(i));
            probeSets.get(i).setNode(nodes[i]);
            leafNodes.add(nodes[i]);
        }
        int len = nodes.length;
        int count;
        while(len > 1) {
            count = 0;
            for (int i = 0; i < len - 1; i += 2) {
                // int var = new ProbeSet(bdd.or(nodes[i].getProbeSet().getExp(), nodes[i + 1].getProbeSet().getExp()));
                BDDTreeNode node = new BDDTreeNode(bdd, bdd.getFalse(), 0);
                node.mergeChild(nodes[i]).mergeChild(nodes[i + 1]);
                nodes[count] = node;
                count ++;
            }
            if (len % 2 == 1) {
                nodes[count++] = nodes[len - 1];
            }
            len = count;
        }
        return new BDDTree(bdd, nodes[0], leafNodes);
    }
}
