import java.util.*;
import java.io.*;
import java.lang.*;
import java.awt.*;

class Node {
    ArrayList<Node> neighbours = new ArrayList<>();
    int nr = -1;

    //paths
    Node prev;

    @Override
    public String toString() {
        return ""+nr;
    }
}

class Inference {
    int in = 0;
    ArrayList<Quartet> start = new ArrayList<>();
    Quartet end = new Quartet();

    boolean consistent(ArrayList<Quartet> quartets) {
        return false;
    }
}

class Quartet {
    int r1;
    int r2;
    int l1;
    int l2;

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof Quartet)) {
            return false;
        }
        Quartet q = (Quartet) o;
        return  ((r1==q.r1)&&(r2==q.r2)&&(l1==q.l1)&&(l2==q.l2))||
                ((r2==q.r1)&&(r1==q.r2)&&(l1==q.l1)&&(l2==q.l2))||
                ((r1==q.r1)&&(r2==q.r2)&&(l2==q.l1)&&(l1==q.l2))||
                ((r2==q.r1)&&(r1==q.r2)&&(l2==q.l1)&&(l1==q.l2))||
                ((r1==q.l1)&&(r2==q.l2)&&(l1==q.r1)&&(l2==q.r2))||
                ((r2==q.l1)&&(r1==q.l2)&&(l1==q.r1)&&(l2==q.r2))||
                ((r1==q.l1)&&(r2==q.l2)&&(l2==q.r1)&&(l1==q.r2))||
                ((r2==q.l1)&&(r1==q.l2)&&(l2==q.r1)&&(l1==q.r2));
    }

    public int min() {
        return Math.min(Math.min(r1, r2), Math.min(l1, l2));
    }

    public int kth(int num) {
        ArrayList<Integer> taxa = new ArrayList<>();
        taxa.add(r1);
        taxa.add(r2);
        taxa.add(l1);
        taxa.add(l2);
        Collections.sort(taxa);
        return taxa.get(num);
    }

    public int max() {
        return Math.max(Math.max(r1, r2), Math.max(l1, l2));
    }

    public boolean sameSet(ArrayList<Integer> two) {
        ArrayList<Integer> one = new ArrayList<>();
        one.add(r1);
        one.add(r2);
        one.add(l1);
        one.add(l2);
        Collections.sort(one);
        Collections.sort(two);
        for(int i = 0; i < one.size(); i++) {
            if(one.get(i) != two.get(i)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return (r1) ^ (r2) ^ (l1) ^ (l2);
    }

    static boolean validQuartet(int[] n, ArrayList<Node> t) {
        ArrayList<Node> path1 = new ArrayList<>();
        ArrayList<Node> path2 = new ArrayList<>();
        ArrayList<Node> now = new ArrayList<>();
        Node start = t.get(0);
        for(Node w : t) {
            if(w.nr == n[0]) {
                start = w;
                now.add(w);
                break;
            }
        }
        while(!now.isEmpty()) {
            ArrayList<Node> next = new ArrayList<>();
            for(Node u : now) {
                for(Node w : u.neighbours) {
                    if(!w.equals(u.prev)) {
                        w.prev = u;
                        next.add(w);
                    }
                }
            }
            now = next;
        }
        Node loc = t.get(0);
        for(Node w : t) {
            if(w.nr == n[1]) {
                loc = w;
                break;
            }
        }
        while(!loc.equals(start)) {
            path1.add(loc);
            loc = loc.prev;
        }
        for(Node u : t) {
            u.prev = null;
        }
        now = new ArrayList<>();
        start = t.get(0);
        for(Node w : t) {
            if(w.nr == n[2]) {
                start = w;
                now.add(w);
                break;
            }
        }
        while(!now.isEmpty()) {
            ArrayList<Node> next = new ArrayList<>();
            for(Node u : now) {
                for(Node w : u.neighbours) {
                    if(!w.equals(u.prev)) {
                        w.prev = u;
                        next.add(w);
                    }
                }
            }
            now = next;
        }
        loc = t.get(0);
        for(Node w : t) {
            if(w.nr == n[3]) {
                loc = w;
                break;
            }
        }
        while(!loc.equals(start)) {
            path2.add(loc);
            loc = loc.prev;
        }
        boolean good = true;
        for(Node u : path1) {
            for(Node w : path2) {
                if(u.equals(w)) {
                    good = false;
                    break;
                }
            }
            if(!good) {
                break;
            }
        }
        for(Node u : t) {
            u.prev = null;
        }
        return good;
    }

    @Override
    public String toString() {
        return "("+r1+" "+r2+"|"+l1+" "+l2+")";
    }
}

class QuartetRules {

    static ArrayList<String> findRules(int n, int qu) {
        // make all trees
        ArrayList<ArrayList<ArrayList<Node>>> treesPerSize = new ArrayList<>();
        Node n1 = new Node();
        Node n2 = new Node();
        n1.nr = 0;
        n2.nr = 1;
        n1.neighbours.add(n2);
        n2.neighbours.add(n1);
        ArrayList<Node> simpleTree = new ArrayList<>();
        simpleTree.add(n1);
        simpleTree.add(n2);
        ArrayList<ArrayList<Node>> twoTree = new ArrayList<>();
        twoTree.add(simpleTree);
        treesPerSize.add(twoTree);
        for(int i = 2; i < n; i++) {
            ArrayList<ArrayList<Node>> trees = treesPerSize.get(treesPerSize.size()-1);
            ArrayList<ArrayList<Node>> nextTrees = new ArrayList<>();
            for(ArrayList<Node> g : trees) {
                for(int j = 0; j < g.size()-1; j++) {
                    for(int k = j+1; k < g.size(); k++) {
                        if(g.get(j).neighbours.contains(g.get(k))) {
                            ArrayList<Node> copy = new ArrayList<>();
                            for(int l = 0; l < g.size(); l++) {
                                Node u = new Node();
                                u.nr = g.get(l).nr;
                                copy.add(u);
                            }
                            for(int l = 0; l < g.size(); l++) {
                                for(int m = 0; m < g.get(l).neighbours.size(); m++) {
                                    copy.get(l).neighbours.add(copy.get(g.get(l).neighbours.get(m).nr));
                                }
                            }
                            Node w1 = new Node();
                            Node w2 = new Node();
                            w1.nr = g.size();
                            w2.nr = g.size()+1;
                            copy.add(w1);
                            copy.add(w2);
                            copy.get(g.get(j).nr).neighbours.remove(copy.get(g.get(k).nr));
                            copy.get(g.get(j).nr).neighbours.add(w1);
                            copy.get(g.get(k).nr).neighbours.remove(copy.get(g.get(j).nr));
                            copy.get(g.get(k).nr).neighbours.add(w1);
                            w1.neighbours.add(copy.get(g.get(j).nr));
                            w1.neighbours.add(copy.get(g.get(k).nr));
                            w1.neighbours.add(w2);
                            w2.neighbours.add(w1);
                            if(!equalTrees(nextTrees, copy)) {
                                nextTrees.add(copy);
                            }
                            
                        }
                    }
                }
            }
            treesPerSize.add(nextTrees);
            System.out.println(nextTrees.size()+" "+(i+1));
        }
        System.out.println("------");

        // renumber trees

        for(ArrayList<ArrayList<Node>> trees : treesPerSize) {
            for(ArrayList<Node> tree : trees) {
                for(Node w : tree) {
                    if(w.neighbours.size() == 3) {
                        w.nr = -1;
                    } else if(w.neighbours.size() == 1) {
                        w.nr = w.nr-(w.nr/2);
                    }
                }
            }
        }

        // find quartets

        ArrayList<ArrayList<ArrayList<Quartet>>> quartets = new ArrayList<>();
        int count = 2;
        for(int size = 0; size < treesPerSize.size(); size++) {
            ArrayList<ArrayList<Node>> trees = treesPerSize.get(size);
            ArrayList<ArrayList<Quartet>> quartetsAtSize = new ArrayList<>();
            for(ArrayList<Node> t : trees) {
                ArrayList<Quartet> treesQuartets = new ArrayList<>();
                for(int i = 0; i < t.size()-1; i++) {
                    for(int j = i+1; j < t.size(); j++) {
                        for(int k = i+1; k < t.size()-1; k++) {
                            for(int l = k+1; l < t.size(); l++) {
                                if(t.get(i).nr != -1 && t.get(j).nr != -1 && t.get(k).nr != -1 && t.get(l).nr != -1 && i!=k && i!=l && j!=k && j!=l) {
                                    int[] tuple = new int[4];
                                    tuple[0] = t.get(i).nr;
                                    tuple[1] = t.get(j).nr;
                                    tuple[2] = t.get(k).nr;
                                    tuple[3] = t.get(l).nr;
                                    if(Quartet.validQuartet(tuple, t)) {
                                        Quartet q = new Quartet();
                                        q.r1 = tuple[0];
                                        q.r2 = tuple[1];
                                        q.l1 = tuple[2];
                                        q.l2 = tuple[3];
                                        treesQuartets.add(q);
                                        //System.out.println(q);
                                    }
                                }
                            }
                        }
                    }
                }
                quartetsAtSize.add(treesQuartets);
            }
            System.out.println("At size "+count+" there are "+trees.size()+" unique trees");
            count++;
            quartets.add(quartetsAtSize);
            System.out.println("------");
        }

        // make inference rules

        System.out.println("Make inference");
        int maxTaxa = n;
        int maxQuartet = qu;

        Quartet startQ = new Quartet();
        startQ.r1 = 0;
        startQ.r2 = 1;
        startQ.l1 = 2;
        startQ.l2 = 3;
        ArrayList<Quartet> inferenceStart = new ArrayList<>();
        inferenceStart.add(startQ);
        ArrayList<ArrayList<Quartet>> previousRules = new ArrayList<>();
        previousRules.add(inferenceStart);
        ArrayList<ArrayList<ArrayList<Quartet>>> possibleRulesAtSize = new ArrayList<>();
        possibleRulesAtSize.add(previousRules);

        ArrayList<ArrayList<Quartet>> inputInference = new ArrayList<>();
        ArrayList<ArrayList<Quartet>> outputInference = new ArrayList<>();

        ArrayList<ArrayList<Quartet>> impossibleInference = new ArrayList<>();

        for(int i = 2; i <= maxQuartet; i++) {
            System.out.println("size of inference: "+i);
            ArrayList<ArrayList<Quartet>> nextRules = new ArrayList<>();
            int possibleSum = 0;
            int time = 0;
            long timing = System.currentTimeMillis();
            long startTime = timing;
            for(ArrayList<Quartet> inference : previousRules) {
                if(time%10==0) {
                    long now = System.currentTimeMillis();
                    long div = now-timing;
                    long divTotal = now-startTime;
                    timing = now;
                    System.out.println(time + "/" + previousRules.size() + ", time: "+(div/3600000)+":"+((div/60000)%60)+":"+((div/1000)%60)+","+(div%1000) + ", total time: "+(divTotal/3600000)+":"+((divTotal/60000)%60)+":"+((divTotal/1000)%60)+","+(divTotal%1000));
                }
                time++;
                // Create all linking possibilities
                int taxa = 0;
                for(Quartet q : inference) {
                    if(q.max() > taxa) {
                        taxa = q.max();
                    }
                }
                ArrayList<ArrayList<Quartet>> possibleRules = makeInference(maxTaxa, inference, new ArrayList<>(), taxa+1);
                
                // Filter out impossible configurations
                for(int j = possibleRules.size()-1; j >= 0; j--) {
                    ArrayList<Quartet> inf = possibleRules.get(j);
                    int max1 = 0;
                    for(Quartet q : inf) {
                        max1 = Math.max(max1, q.max());
                    }
                    for(ArrayList<Quartet> impossible : impossibleInference) {
                        int max2 = 0;
                        for(Quartet q : impossible) {
                            max2 = Math.max(max2, q.max());
                        }
                        int[] perm = new int[Math.max(max1, max2)+1];
                        int[] permback = new int[Math.max(max1, max2)+1];
                        for(int k = 0; k < Math.max(max1, max2)+1; k++) {
                            perm[k] = -1;
                            permback[k] = -1;
                        }
                        if(isSameRule(impossible, inf, perm, permback)) {
                            possibleRules.remove(inf);
                        }
                    }
                }

                // Filter out supersets of known inference rules
                for(int j = possibleRules.size()-1; j >= 0; j--) {
                    ArrayList<Quartet> inf = possibleRules.get(j);
                    int max1 = 0;
                    for(Quartet q : inf) {
                        max1 = Math.max(max1, q.max());
                    }
                    for(ArrayList<Quartet> realInf : inputInference) {
                        int max2 = 0;
                        for(Quartet q : realInf) {
                            max2 = Math.max(max2, q.max());
                        }
                        int[] perm = new int[Math.max(max1, max2)+1];
                        int[] permback = new int[Math.max(max1, max2)+1];
                        for(int k = 0; k < Math.max(max1, max2)+1; k++) {
                            perm[k] = -1;
                            permback[k] = -1;
                        }
                        if(isSameRule(realInf, inf, perm, permback)) {
                            possibleRules.remove(inf);
                        }
                    }
                }

                possibleSum += possibleRules.size();
                //System.out.println("possible: "+possibleRules.size());
                //for(ArrayList<Quartet> inf : possibleRules) {
                    //System.out.println(inf);
                //}

                // Filter out all duplicates up to permutation
                for(ArrayList<Quartet> inf : possibleRules) {
                    addUniqueRule(inf, nextRules);
                }
                //System.out.println("filtered: "+nextRules.size());
                //for(ArrayList<Quartet> inf : nextRules) {
                    //System.out.println(inf);
                //}
            }
            System.out.println("possible sum: " + possibleSum);
            System.out.println("filtered: " + nextRules.size());

            // Filter out all that are not connected enough
            ArrayList<ArrayList<Quartet>> connectedRules1 = new ArrayList<>();
            for(ArrayList<Quartet> inf : nextRules) {
                if(isConnectedEnough(inf, 1)) {
                    connectedRules1.add(inf);
                }
            }
            System.out.println("connected 1: "+connectedRules1.size());
            for(ArrayList<Quartet> inf : connectedRules1) {
                //System.out.println(inf);
            }
            ArrayList<ArrayList<Quartet>> connectedRules2 = new ArrayList<>();
            for(ArrayList<Quartet> inf : nextRules) {
                if(isConnectedEnough(inf, 2)) {
                    connectedRules2.add(inf);
                }
            }
            System.out.println("connected 2: "+connectedRules2.size());
            for(ArrayList<Quartet> inf : connectedRules2) {
                //System.out.println(inf);
            }

            time = 0;
            timing = System.currentTimeMillis();
            startTime = timing;
            int ruleCount = 0;
            for(ArrayList<Quartet> inference : connectedRules1) {

                if(time%100==0) {
                    long now = System.currentTimeMillis();
                    long div = now-timing;
                    long divTotal = now-startTime;
                    timing = now;
                    System.out.println("rules found: " + ruleCount + ", total: " + inputInference.size() + ", todo: " + time + "/" + connectedRules1.size() + ", time: "+(div/3600000)+":"+((div/60000)%60)+":"+((div/1000)%60)+","+(div%1000) + ", total time: "+(divTotal/3600000)+":"+((divTotal/60000)%60)+":"+((divTotal/1000)%60)+","+(divTotal%1000));
                    ruleCount = 0;
                }
                time++;

                int taxa = 0;
                for(Quartet q : inference) {
                    if(q.max() > taxa) {
                        taxa = q.max();
                    }
                }
                ArrayList<Quartet> restriction = new ArrayList<>();
                for(int j = 0; j < taxa-2; j++) {
                    for(int k = j+1; k < taxa-1; k++) {
                        for(int l = k+1; l < taxa; l++) {
                            for(int m = l+1; m < taxa+1; m++) {
                                Quartet q1 = new Quartet();
                                Quartet q2 = new Quartet();
                                Quartet q3 = new Quartet();
                                q1.r1 = j;
                                q1.r2 = k;
                                q1.l1 = l;
                                q1.l2 = m;
                                q2.r1 = j;
                                q2.r2 = l;
                                q2.l1 = k;
                                q2.l2 = m;
                                q3.r1 = j;
                                q3.r2 = m;
                                q3.l1 = k;
                                q3.l2 = l;
                                restriction.add(q1);
                                restriction.add(q2);
                                restriction.add(q3);
                            }
                        }
                    }
                }
                int[] perm = new int[taxa+1];
                int[] permback = new int[taxa+1];
                for(int j = 0; j < taxa+1; j++) {
                    perm[j] = -1;
                    permback[j] = -1;
                }
                boolean possible = false;
                for(ArrayList<Quartet> tree : quartets.get(taxa-1)) {
                    boolean treeBool = treePerm(inference, tree, perm, permback, restriction);
                    possible = (possible || treeBool);
                    if(restriction.isEmpty()) {
                        break;
                    }
                }
                if(!possible) {
                    impossibleInference.add(inference);
                    System.out.println(inference + " impossible");
                    nextRules.remove(inference);
                } else if(!restriction.isEmpty()) {
                    int max1 = 0;
                    for(Quartet q : inference) {
                        max1 = Math.max(max1, q.max());
                    }
                    for(int j = 0; j < inputInference.size(); j++) {
                        ArrayList<Quartet> realInf = inputInference.get(j);
                        ArrayList<Quartet> realOut = outputInference.get(j);
                        int max2 = 0;
                        for(Quartet q : realInf) {
                            max2 = Math.max(max2, q.max());
                        }
                        int[] perm2 = new int[Math.max(max1, max2)+1];
                        int[] permback2 = new int[Math.max(max1, max2)+1];
                        for(int k = 0; k < Math.max(max1, max2)+1; k++) {
                            perm2[k] = -1;
                            permback2[k] = -1;
                        }
                        subsetRestrict(realInf, inference, perm2, permback2, realOut, restriction);
                        if(restriction.isEmpty()) {
                            break;
                        }
                    }
                    if(!restriction.isEmpty()) {
                        //System.out.println("New rule " + inference + " -> " + restriction);
                        ruleCount++;
                        inputInference.add(inference);
                        outputInference.add(restriction);
                        nextRules.remove(inference);
                    }
                }
            }

            previousRules = nextRules;
            possibleRulesAtSize.add(nextRules);

            // Check them against trees and add to 
            System.out.println(i + " " + nextRules.size());
        }
        System.out.println("Number of rules "+inputInference.size());
        try {
            FileWriter myWriter = new FileWriter("C:/Users/20202991/Dropbox/My PC (S20202991)/Desktop/inferenceRules.txt");
            String text = maxTaxa + " " + maxQuartet + "\n" + inputInference.size() + "\n";
            for(int i = 0; i < inputInference.size(); i++) {
                text += inputInference.get(i) + " -> " + outputInference.get(i) + "\n";
            }
            
            myWriter.write(text);
            myWriter.close();
            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        

        return new ArrayList<>();
    }

    static void subsetRestrict(ArrayList<Quartet> realInf, ArrayList<Quartet> inf, int[] perm, int[] permback, ArrayList<Quartet> realOut, ArrayList<Quartet> out) {
        if(realInf.isEmpty()) {
            for(Quartet q : realOut) {
                Quartet newQ = new Quartet();
                newQ.r1 = perm[q.r1];
                newQ.r2 = perm[q.r2];
                newQ.l1 = perm[q.l1];
                newQ.l2 = perm[q.l2];
                if(out.contains(newQ)) {
                    out.remove(newQ);
                }
            }
            return;
        }
        Quartet q1 = realInf.get(0);
        for(int i = 0; i < inf.size(); i++) {
            Quartet q2 = inf.get(i);
            // Try all 8 configurations and check with perm if it works
            int[] r1 = new int[]{q2.r1, q2.r2, q2.r1, q2.r2, q2.l1, q2.l1, q2.l2, q2.l2};
            int[] r2 = new int[]{q2.r2, q2.r1, q2.r2, q2.r1, q2.l2, q2.l2, q2.l1, q2.l1};
            int[] l1 = new int[]{q2.l1, q2.l1, q2.l2, q2.l2, q2.r1, q2.r2, q2.r1, q2.r2};
            int[] l2 = new int[]{q2.l2, q2.l2, q2.l1, q2.l1, q2.r2, q2.r1, q2.r2, q2.r1};
            for(int j = 0; j < 8; j++) {
                if((perm[q1.r1] == -1 || perm[q1.r1] == r1[j]) && (perm[q1.r2] == -1 || perm[q1.r2] == r2[j]) &&
                (perm[q1.l1] == -1 || perm[q1.l1] == l1[j]) && (perm[q1.l2] == -1 || perm[q1.l2] == l2[j]) &&
                (permback[r1[j]] == -1 || permback[r1[j]] == q1.r1) && (permback[r2[j]] == -1 || permback[r2[j]] == q1.r2) &&
                (permback[l1[j]] == -1 || permback[l1[j]] == q1.l1) && (permback[l2[j]] == -1 || permback[l2[j]] == q1.l2)) {
                    realInf.remove(0);
                    boolean was1 = (perm[q1.r1] == -1);
                    boolean was2 = (perm[q1.r2] == -1);
                    boolean was3 = (perm[q1.l1] == -1);
                    boolean was4 = (perm[q1.l2] == -1);
                    boolean was5 = (permback[r1[j]] == -1);
                    boolean was6 = (permback[r2[j]] == -1);
                    boolean was7 = (permback[l1[j]] == -1);
                    boolean was8 = (permback[l2[j]] == -1);
                    perm[q1.r1] = r1[j];
                    perm[q1.r2] = r2[j];
                    perm[q1.l1] = l1[j];
                    perm[q1.l2] = l2[j];
                    permback[r1[j]] = q1.r1;
                    permback[r2[j]] = q1.r2;
                    permback[l1[j]] = q1.l1;
                    permback[l2[j]] = q1.l2;
                    inf.remove(i);
                    subsetRestrict(realInf, inf, perm, permback, realOut, out);
                    realInf.add(0, q1);
                    inf.add(i, q2);
                    if(was1) {
                        perm[q1.r1] = -1;
                    }
                    if(was2) {
                        perm[q1.r2] = -1;
                    }
                    if(was3) {
                        perm[q1.l1] = -1;
                    }
                    if(was4) {
                        perm[q1.l2] = -1;
                    }
                    if(was5) {
                        permback[r1[j]] = -1;
                    }
                    if(was6) {
                        permback[r2[j]] = -1;
                    }
                    if(was7) {
                        permback[l1[j]] = -1;
                    }
                    if(was8) {
                        permback[l2[j]] = -1;
                    }
                    if(out.isEmpty()) {
                        return;
                    }
                }
            }
        }
        return;
    }

    static boolean treePerm(ArrayList<Quartet> inference, ArrayList<Quartet> tree, int[] perm, int[] permback, ArrayList<Quartet> restriction) {
        if(inference.isEmpty()) {
            ArrayList<Quartet> newRestrictions = new ArrayList<>();
            for(Quartet q : tree) {
                Quartet newQ = new Quartet();
                newQ.r1 = permback[q.r1];
                newQ.r2 = permback[q.r2];
                newQ.l1 = permback[q.l1];
                newQ.l2 = permback[q.l2];
                newRestrictions.add(newQ);
            }
            for(int i = restriction.size()-1; i >= 0; i--) {
                if(!newRestrictions.contains(restriction.get(i))) {
                    restriction.remove(i);
                }
            }
            return true;
        }
        boolean restrict = false;
        Quartet q1 = inference.get(0);
        for(int i = 0; i < tree.size(); i++) {
            Quartet q2 = tree.get(i);
            // Try all 8 configurations and check with perm if it works
            int[] r1 = new int[]{q2.r1, q2.r2, q2.r1, q2.r2, q2.l1, q2.l1, q2.l2, q2.l2};
            int[] r2 = new int[]{q2.r2, q2.r1, q2.r2, q2.r1, q2.l2, q2.l2, q2.l1, q2.l1};
            int[] l1 = new int[]{q2.l1, q2.l1, q2.l2, q2.l2, q2.r1, q2.r2, q2.r1, q2.r2};
            int[] l2 = new int[]{q2.l2, q2.l2, q2.l1, q2.l1, q2.r2, q2.r1, q2.r2, q2.r1};
            for(int j = 0; j < 8; j++) {
                if((perm[q1.r1] == -1 || perm[q1.r1] == r1[j]) && (perm[q1.r2] == -1 || perm[q1.r2] == r2[j]) &&
                (perm[q1.l1] == -1 || perm[q1.l1] == l1[j]) && (perm[q1.l2] == -1 || perm[q1.l2] == l2[j]) &&
                (permback[r1[j]] == -1 || permback[r1[j]] == q1.r1) && (permback[r2[j]] == -1 || permback[r2[j]] == q1.r2) &&
                (permback[l1[j]] == -1 || permback[l1[j]] == q1.l1) && (permback[l2[j]] == -1 || permback[l2[j]] == q1.l2)) {
                    inference.remove(0);
                    boolean was1 = (perm[q1.r1] == -1);
                    boolean was2 = (perm[q1.r2] == -1);
                    boolean was3 = (perm[q1.l1] == -1);
                    boolean was4 = (perm[q1.l2] == -1);
                    boolean was5 = (permback[r1[j]] == -1);
                    boolean was6 = (permback[r2[j]] == -1);
                    boolean was7 = (permback[l1[j]] == -1);
                    boolean was8 = (permback[l2[j]] == -1);
                    perm[q1.r1] = r1[j];
                    perm[q1.r2] = r2[j];
                    perm[q1.l1] = l1[j];
                    perm[q1.l2] = l2[j];
                    permback[r1[j]] = q1.r1;
                    permback[r2[j]] = q1.r2;
                    permback[l1[j]] = q1.l1;
                    permback[l2[j]] = q1.l2;
                    tree.remove(i);
                    boolean treeBool = treePerm(inference, tree, perm, permback, restriction);
                    restrict = (restrict || treeBool);
                    inference.add(0, q1);
                    tree.add(i, q2);
                    if(was1) {
                        perm[q1.r1] = -1;
                    }
                    if(was2) {
                        perm[q1.r2] = -1;
                    }
                    if(was3) {
                        perm[q1.l1] = -1;
                    }
                    if(was4) {
                        perm[q1.l2] = -1;
                    }
                    if(was5) {
                        permback[r1[j]] = -1;
                    }
                    if(was6) {
                        permback[r2[j]] = -1;
                    }
                    if(was7) {
                        permback[l1[j]] = -1;
                    }
                    if(was8) {
                        permback[l2[j]] = -1;
                    }
                    if(restriction.isEmpty()) {
                        return restrict;
                    }
                }
            }
        }
        return restrict;
    }

    static ArrayList<ArrayList<Quartet>> makeInference(int maxTaxa, ArrayList<Quartet> inference, ArrayList<Integer> partial, int taxa) {
        ArrayList<ArrayList<Quartet>> extraInference = new ArrayList<>();
        if(partial.size() < 4) {
            int i = 0;
            Quartet q = inference.get(inference.size()-1);
            if(partial.isEmpty()) {
                i = q.min();
            } else {
                boolean same = true;
                for(int j = 0; j < partial.size(); j++) {
                    if(partial.get(j) != q.kth(j)) {
                        same = false;
                        break;
                    }
                }
                if(same) {
                    i = q.kth(partial.size());
                } else {
                    i = partial.get(partial.size()-1)+1;
                }
            }
            for(;i <= Math.min(taxa, maxTaxa); i++) {
                partial.add(i);
                extraInference.addAll(makeInference(maxTaxa, inference, partial, i == taxa ? taxa+1 : taxa));
                partial.remove((Integer)i);
            }
        } else {
            if(!inference.get(inference.size()-1).sameSet(partial)) {
                Quartet q1 = new Quartet();
                q1.r1 = partial.get(0);
                q1.r2 = partial.get(1);
                q1.l1 = partial.get(2);
                q1.l2 = partial.get(3);
                Quartet q2 = new Quartet();
                q2.r1 = partial.get(0);
                q2.r2 = partial.get(2);
                q2.l1 = partial.get(1);
                q2.l2 = partial.get(3);
                Quartet q3 = new Quartet();
                q3.r1 = partial.get(0);
                q3.r2 = partial.get(3);
                q3.l1 = partial.get(1);
                q3.l2 = partial.get(2);
                ArrayList<Quartet> newInference1 = new ArrayList<>();
                newInference1.addAll(inference);
                newInference1.add(q1);
                extraInference.add(newInference1);
                ArrayList<Quartet> newInference2 = new ArrayList<>();
                newInference2.addAll(inference);
                newInference2.add(q2);
                extraInference.add(newInference2);
                ArrayList<Quartet> newInference3 = new ArrayList<>();
                newInference3.addAll(inference);
                newInference3.add(q3);
                extraInference.add(newInference3);
            }
        }
        return extraInference;
    }

    static boolean isConnectedEnough(ArrayList<Quartet> inference, int minCon) {
        int taxa = 0;
        for(Quartet q : inference) {
            if(q.max() > taxa) {
                taxa = q.max();
            }
        }
        ArrayList<ArrayList<Integer>> tuples = new ArrayList<>();
        for(int i = 0; i < Math.round(Math.pow(taxa, minCon)); i++) {
            int num = i;
            ArrayList<Integer> tuple = new ArrayList<>();
            for(int j = 0; j < minCon; j++) {
                tuple.add(num%taxa);
                num /= taxa;
            }
            boolean good = true;
            for(int j = 0; j < minCon-1; j++) {
                if(tuple.get(j) >= tuple.get(j+1)) {
                    good = false;
                    break;
                }
            }
            if(good) {
                tuples.add(tuple);
            }
        }
        for(ArrayList<Integer> tuple : tuples) {
            ArrayList<HashSet<Integer>> segments = new ArrayList<>();
            for(Quartet q : inference) {
                int in = -1;
                for(int j = 0; j < segments.size(); j++) {
                    HashSet segment = segments.get(j);
                    if(segment.contains(q.r1) || segment.contains(q.r2) || segment.contains(q.l1) || segment.contains(q.l2)) {
                        if(in == -1) {
                            in = j;
                            if(!tuple.contains(q.r1)) {
                                segment.add(q.r1);
                            }
                            if(!tuple.contains(q.r2)) {
                                segment.add(q.r2);
                            }
                            if(!tuple.contains(q.l1)) {
                                segment.add(q.l1);
                            }
                            if(!tuple.contains(q.l2)) {
                                segment.add(q.l2);
                            }
                        } else {
                            segments.remove(j);
                            segments.get(in).addAll(segment);
                            j--;
                        }
                    }
                }
                if(in == -1) {
                    HashSet<Integer> segment = new HashSet<>();
                    if(!tuple.contains(q.r1)) {
                        segment.add(q.r1);
                    }
                    if(!tuple.contains(q.r2)) {
                        segment.add(q.r2);
                    }
                    if(!tuple.contains(q.l1)) {
                        segment.add(q.l1);
                    }
                    if(!tuple.contains(q.l2)) {
                        segment.add(q.l2);
                    }
                    segments.add(segment);
                }
            }
            if(segments.size() > 1) {
                return false;
            }
        }
        return true;
    }

    static void addUniqueRule(ArrayList<Quartet> inference, ArrayList<ArrayList<Quartet>> rules) {
        int taxa = 0;
        for(Quartet q : inference) {
            if(q.max() > taxa) {
                taxa = q.max();
            }
        }
        int[] perm = new int[taxa+1];
        int[] permback = new int[taxa+1];
        for(int i = 0; i < taxa+1; i++) {
            perm[i] = -1;
            permback[i] = -1;
        }
        for(ArrayList<Quartet> check : rules) {
            int taxaCheck = 0;
            for(Quartet q : check) {
                if(q.max() > taxaCheck) {
                    taxaCheck = q.max();
                }
            }
            if(taxa == taxaCheck && isSameRule(check, inference, perm, permback)) {
                return;
            }
        }
        rules.add(inference);
    }

    static boolean isSameRule(ArrayList<Quartet> inf1, ArrayList<Quartet> inf2, int[] perm, int[] permback) {
        if(inf1.isEmpty()) {
            return true;
        }
        Quartet q1 = inf1.get(0);
        for(int i = 0; i < inf2.size(); i++) {
            Quartet q2 = inf2.get(i);
            // Try all 8 configurations and check with perm if it works
            int[] r1 = new int[]{q2.r1, q2.r2, q2.r1, q2.r2, q2.l1, q2.l1, q2.l2, q2.l2};
            int[] r2 = new int[]{q2.r2, q2.r1, q2.r2, q2.r1, q2.l2, q2.l2, q2.l1, q2.l1};
            int[] l1 = new int[]{q2.l1, q2.l1, q2.l2, q2.l2, q2.r1, q2.r2, q2.r1, q2.r2};
            int[] l2 = new int[]{q2.l2, q2.l2, q2.l1, q2.l1, q2.r2, q2.r1, q2.r2, q2.r1};
            for(int j = 0; j < 8; j++) {
                if((perm[q1.r1] == -1 || perm[q1.r1] == r1[j]) && (perm[q1.r2] == -1 || perm[q1.r2] == r2[j]) &&
                (perm[q1.l1] == -1 || perm[q1.l1] == l1[j]) && (perm[q1.l2] == -1 || perm[q1.l2] == l2[j]) &&
                (permback[r1[j]] == -1 || permback[r1[j]] == q1.r1) && (permback[r2[j]] == -1 || permback[r2[j]] == q1.r2) &&
                (permback[l1[j]] == -1 || permback[l1[j]] == q1.l1) && (permback[l2[j]] == -1 || permback[l2[j]] == q1.l2)) {
                    inf1.remove(0);
                    boolean was1 = (perm[q1.r1] == -1);
                    boolean was2 = (perm[q1.r2] == -1);
                    boolean was3 = (perm[q1.l1] == -1);
                    boolean was4 = (perm[q1.l2] == -1);
                    boolean was5 = (permback[r1[j]] == -1);
                    boolean was6 = (permback[r2[j]] == -1);
                    boolean was7 = (permback[l1[j]] == -1);
                    boolean was8 = (permback[l2[j]] == -1);
                    perm[q1.r1] = r1[j];
                    perm[q1.r2] = r2[j];
                    perm[q1.l1] = l1[j];
                    perm[q1.l2] = l2[j];
                    permback[r1[j]] = q1.r1;
                    permback[r2[j]] = q1.r2;
                    permback[l1[j]] = q1.l1;
                    permback[l2[j]] = q1.l2;
                    inf2.remove(i);
                    boolean isSame = isSameRule(inf1, inf2, perm, permback);
                    inf1.add(0, q1);
                    inf2.add(i, q2);
                    if(was1) {
                        perm[q1.r1] = -1;
                    }
                    if(was2) {
                        perm[q1.r2] = -1;
                    }
                    if(was3) {
                        perm[q1.l1] = -1;
                    }
                    if(was4) {
                        perm[q1.l2] = -1;
                    }
                    if(was5) {
                        permback[r1[j]] = -1;
                    }
                    if(was6) {
                        permback[r2[j]] = -1;
                    }
                    if(was7) {
                        permback[l1[j]] = -1;
                    }
                    if(was8) {
                        permback[l2[j]] = -1;
                    }
                    if(isSame) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    static boolean equalTrees(ArrayList<ArrayList<Node>> trees, ArrayList<Node> t) {
        for(ArrayList<Node> t2 : trees) {
            if(equalTree(t, t2)) {
                return true;
            }
        }
        return false;
    }

    static boolean equalTree(ArrayList<Node> t1, ArrayList<Node> t2) {
        Node start = new Node();
        for(Node n : t1) {
            if(n.neighbours.size()==1) {
                start = n;
                break;
            }
        }
        for(Node n : t2) {
            if(n.neighbours.size()==1) {
                if(equalTreeRoot(start.neighbours.get(0), n.neighbours.get(0), start, n)) {
                    return true;
                }
            }
        }
        return false;
    }

    static boolean equalTreeRoot(Node r1, Node r2, Node prevR1, Node prevR2) {
        if(r1.neighbours.size() == 1 && r2.neighbours.size() == 1) {
            return true;
        }
        if(r1.neighbours.size() != r2.neighbours.size()) {
            return false;
        }

        Node r11 = new Node();
        Node r12 = new Node();
        Node r21 = new Node();
        Node r22 = new Node();
        for(Node n : r1.neighbours) {
            if(n != prevR1 && r11.nr == -1) {
                r11 = n;
            } else if(n != prevR1) {
                r12 = n;
            }
        }
        for(Node n : r2.neighbours) {
            if(n != prevR2 && r21.nr == -1) {
                r21 = n;
            } else if(n != prevR2) {
                r22 = n;
            }
        }
        return (equalTreeRoot(r11, r21, r1, r2) && equalTreeRoot(r12, r22, r1, r2)) || (equalTreeRoot(r11, r22, r1, r2) && equalTreeRoot(r12, r21, r1, r2));
    }

    public static void main(String[] args) {
        ArrayList<String> rules = new ArrayList<>();
        rules = findRules(16, 5);
        for(int i = 0; i < rules.size(); i++) {
            System.out.println(rules.get(i));
        }
    }
}