import java.io.*;
import java.util.*;

public class Main {
    static int q1;
    static int q2;
    static int nteams;
    static List<Umpire> solution = Collections.synchronizedList(new ArrayList<>());
    static ArrayList<Game> tournament = new ArrayList<>();
    static int[][] S;
    static int[][] LB;
    static boolean solutionfound = false;
    static long nodeCount = 0;
    static long EffectiveNodeCount = 0;
    static long startTime;
    static String method;
    static String instanceName;
    static boolean parallel;
    static boolean fancyLB;
    static boolean hungarian;
    static boolean partialLB;
    static boolean useOldLBs;
    static boolean hash;
    static ArrayList<ArrayList<Edge>>[] UpperA;
    static ArrayList<ArrayList<Integer>> DDDistance = new ArrayList<>();
    static ArrayList<Integer> homeTOWNtoVIST = new ArrayList<>();
    static HashMap<String, Integer> partialMatchingList = new HashMap<>();

    private static ArrayList<Game> makeTournament(String fileName){
        InputReader inputReader = new InputReader(fileName);
        ArrayList<ArrayList<Integer>> dist = inputReader.getDist();
        ArrayList<ArrayList<Integer>> opponents = inputReader.getOpponents();
        nteams = inputReader.getnTeams();
        int ngames = nteams/2;
        int roundCounter = 0;

        for(ArrayList<Integer> round: opponents){
            for(Integer game: round){
                if(game > 0) {
                    Game newGame = new Game(round.indexOf(game) + 1, game, opponents.indexOf(round));

                    if(tournament.size() >= ngames) {
                        for (int i = ngames * (roundCounter-1); i < ngames * roundCounter; i++) {
                            Game prevGame = tournament.get(i);
                            int travelDist = dist.get(prevGame.home -1).get(newGame.home -1);
                            if (adjacentFeasable(prevGame, newGame)){
                                Edge newEdge = new Edge(prevGame, newGame, travelDist);
                                prevGame.addEdge(newEdge);
                            }
                        }
                    }
                    tournament.add(newGame);
                }
            }
            roundCounter++;
        }
        return tournament;
    }

    public static void calculatePartialBounds(){
        for (int i=0; i<nteams*2-2; i++){
            DDDistance.add(new ArrayList<>());
        }
        for(Game g: tournament){
            Collections.sort(g.edges, Comparator.comparingInt(Edge::getDistance));
            if (g.edges.size()==0){
                System.out.println("problem");
                DDDistance.get(g.round).add(0);
                continue;
            }
            DDDistance.get(g.round).add(g.edges.get(0).distance);
        }
        //sort distances
        for (ArrayList<Integer> list : DDDistance) {
            Collections.sort(list);
        }
        System.out.println("Completed");
    }

    public static void boostSolution(int distance){
        if (distance == 0) return;
        Umpire u = new Umpire(0);
        u.distance = distance;
        solution.add(u);
        for (int a=1; a<nteams/2; a++){
            Umpire uu = new Umpire(a);
            solution.add(uu);
        }
    }

    public static boolean adjacentFeasable(Game previous, Game game){
        if(q1>1)
            if(previous.home == game.home) return false;
        if(q2>1){
            if(previous.home == game.home) return false;
            if(previous.out == game.home) return false;
            if(previous.home == game.out) return false;
            if(previous.out == game.out) return false;
        }
        return true;
    }

    public static boolean isFeasable(Game game, Umpire umpire, ArrayList<Umpire> umpires){
        boolean feasable = true;
        int c1 = q1;
        int c2 = q2;
        //already taken by other umpire
        for (Umpire u: umpires){
            if (u == umpire) continue;
            if (game.round >= u.schedule.size()) continue;
            if (u.schedule.get(game.round) == game) return false;
        }
        if (game.round < c1) c1 = game.round+1;
        for (int r1 = game.round-(c1-1); r1<game.round; r1++){
            if(umpire.schedule.get(r1).home == game.home) return false;
        }
        if (game.round < c2) c2 = game.round+1;
        for (int r2 = game.round-(c2-1); r2<game.round; r2++){
            if(umpire.schedule.get(r2).home == game.home) return false;
            if(umpire.schedule.get(r2).out == game.home) return false;
            if(umpire.schedule.get(r2).home == game.out) return false;
            if(umpire.schedule.get(r2).out == game.out) return false;
        }
        return feasable;
    }

    public static boolean isFeasableNotR0(Game game, Umpire umpire, ArrayList<Umpire> umpires, int round){
        round++;
        boolean feasable = true;
        int c1 = q1;
        int c2 = q2;
        //already taken by other umpire
        for (Umpire u: umpires){
            if (u == umpire) continue;
            if (round >= u.schedule.size()) continue;
            if (u.schedule.get(round) == game) return false;
        }
        if (round < c1) c1 = round+1;
        for (int r1 = round-(c1-1); r1<round; r1++){
            if(umpire.schedule.get(r1).home == game.home) return false;
        }
        if (round < c2) c2 = round+1;
        for (int r2 = round-(c2-1); r2<round; r2++){
            if(umpire.schedule.get(r2).home == game.home) return false;
            if(umpire.schedule.get(r2).out == game.home) return false;
            if(umpire.schedule.get(r2).home == game.out) return false;
            if(umpire.schedule.get(r2).out == game.out) return false;
        }
        return feasable;
    }

    public static Edge chooseSmallestFeasable(ArrayList<Edge> edges, Umpire umpire, ArrayList<Umpire> umpires){
        Edge theGame = null;
        for(Edge e: edges){
            if (!isFeasable(e.destination, umpire, umpires)) continue;
            if (theGame == null) theGame = e;
            else if (theGame.distance < e.distance) theGame = e;
        }
        return theGame; //returns null if no feasable solutions are found
    }

    public static ArrayList<Edge> sortedFeasable(ArrayList<Edge> edges, Umpire umpire, ArrayList<Umpire> umpires){
        ArrayList<Edge> sortedGames = new ArrayList<>();
        for(Edge e: edges){
            if (!isFeasable(e.destination, umpire, umpires)) continue;
            sortedGames.add(e);
        }
//        Collections.sort(sortedGames, Comparator.comparingInt(Edge::getDistance)); // is actually not needed?
        return sortedGames; //returns null if no feasable solutions are found
    }

    public static ArrayList<Edge> sortedFeasableNotR0(ArrayList<Edge> edges, Umpire umpire, ArrayList<Umpire> umpires, int round){
        ArrayList<Edge> sortedGames = new ArrayList<>();
        for(Edge e: edges){
            if (!isFeasableNotR0(e.destination, umpire, umpires, round)) continue;
            sortedGames.add(e);
        }
//        Collections.sort(sortedGames, Comparator.comparingInt(Edge::getDistance));
        return sortedGames; //returns null if no feasable solutions are found
    }

    public static boolean fullFeasable(ArrayList<Umpire> umpires){
        for (Umpire u: umpires){
            for (Integer i : u.homeTownVisit){
                if (i < 1) return false;
            }
        }
        return true;
    }

    public static int homeTownFeasable(Umpire u){
        int numToGoTo = 0;
        for (Integer i : u.homeTownVisit){
            if (i < 1) numToGoTo++;
        }
        return numToGoTo;
    }

    public static void fillHomeTOWNtoVISIT(){
        for (int i=0; i<nteams; i++){
            homeTOWNtoVIST.add(0);
        }
        for (Game g: tournament){
            homeTOWNtoVIST.set(g.home-1, homeTOWNtoVIST.get(g.home-1)+1);
        }
    }

    public static int homeTownFeasableNew(Umpire u){
        int numToGoTo = 0;
        for (int i=0; i<nteams; i++){
            if (u.homeTownVisit.get(i) == 0) {
                if (homeTOWNtoVIST.get(i) == 0){
                    return 99999;
                }
                numToGoTo++;
            }
            //check if different visits are not in the same round!!!!!!!
        }
        return numToGoTo;
    }

    public static boolean checkForBetterSolution(ArrayList<Umpire> umpires, boolean v){
        int sumSolution = 0;
        int sumUmpires = 0;
        if (solution.isEmpty()) return true;
        for (Umpire u: umpires){
            sumUmpires += u.distance;
        }
        for (Umpire u: solution){
            sumSolution += u.distance;
        }
        if (v && sumSolution<sumUmpires) System.out.println("New is sadder");
        else if (v && sumSolution>sumUmpires) System.out.println("NEW IS ALWAYS BETTER");
        return sumSolution>=sumUmpires;
    }

    public static int sumUmpires(ArrayList<Umpire> umpires){
        int sumUmpires = 0;
        for (Umpire u: umpires){
            sumUmpires += u.distance;
        }
        return sumUmpires;
    }

    public synchronized static int sumSolution(){
        int sumSolution = 0;
        if (solution.isEmpty()) return sumSolution;
        for (Umpire u: solution){
            sumSolution += u.distance;
        }
        return sumSolution;
    }

    public static int hL(ArrayList<Umpire> umpires, int u, int r){
        if (!hungarian) return 0;
        if (!partialLB) return 0;
        int sum = 0;
        for (int i=0; i<umpires.size()-u-1; i++){
            sum += DDDistance.get(r).get(i);
        }
        return sum;
    }

    public static ArrayList<Integer> hList(List<Game> games, int r){
        ArrayList<Integer> hList = new ArrayList<>();
        int [][] distances = new int[nteams/2][nteams/2];
        // is deze stap nodig?
        for (int ss1=0; ss1<nteams/2; ss1++){
            for (int ss2=0; ss2<nteams/2; ss2++){
                distances[ss1][ss2] = Integer.MAX_VALUE;
            }
        }
        int startIndex = 0;
        Map<Integer, Integer> i = new HashMap<>();
        for (int a=0; a<nteams/2; a++){
            for (Edge e: games.get(a).edges) {
                if (!i.containsKey(e.destination.home - 1)) {
                    i.put(e.destination.home - 1, startIndex++);
                }
                distances[a][i.get(e.destination.home - 1)] = e.distance;
            }
        }
        // copy to a new list
        int [][] newdistances = new int[nteams/2][nteams/2];
        for (int h=0; h<distances.length; h++){
            for (int g=0; g<distances.length; g++){
                newdistances[h][g] = distances[h][g];
            }
        }
        HungarianAlgorithm hungarianAlgorithm = new HungarianAlgorithm();
        int[][] assignments = hungarianAlgorithm.computeAssignments(distances);
        for (int h=0; h<assignments.length; h++){
            hList.add(newdistances[assignments[h][0]][assignments[h][1]]);
        }
        return hList;
    }

    public static int h(ArrayList<Umpire> umpires, int u, int r){
        if (!hungarian) return 0;
        if (partialLB) return hL(umpires, u, r);
        if (hash && partialMatchingList.containsKey(r+"_"+u)) return partialMatchingList.get(r+"_"+u);
        if (nteams/2==u+1) return 0;
        int [] visited = new int[nteams];
        for (int a=0; a<=u; a++){
            visited[umpires.get(a).schedule.get(r).home-1] = 1;
        }
        int [][] distances = new int[nteams/2-u-1][nteams/2-u-1];
        for (int ss1=0; ss1<nteams/2-u-1; ss1++){
            for (int ss2=0; ss2<nteams/2-u-1; ss2++){
                distances[ss1][ss2] = Integer.MAX_VALUE;
            }
        }
        int startIndex = 0;
        Map<Integer, Integer> i = new HashMap<>();
        for (int a=u+1; a<nteams/2; a++){
            boolean dirty = false;
            for (Edge e: umpires.get(a).schedule.get(r-1).edges){
                if (visited[e.destination.home-1]==1) continue;
                if (!i.containsKey(e.destination.home-1)){
                    i.put(e.destination.home-1, startIndex++);
                }
                if (isFeasable(e.destination, umpires.get(a), umpires)){
                    distances[a-u-1][i.get(e.destination.home-1)] = e.distance;
                    dirty = true;
                }
            }
            if (!dirty) return Integer.MAX_VALUE;
        }
        if (nteams/2-u-1==1) return distances[0][0];
        int [][] newdistances = new int[nteams/2-u-1][nteams/2-u-1];
        for (int h=0; h<distances.length; h++){
            for (int g=0; g<distances.length; g++){
                newdistances[h][g] = distances[h][g];
            }
        }
        HungarianAlgorithm hungarianAlgorithm = new HungarianAlgorithm();
        int[][] assignments = hungarianAlgorithm.computeAssignments(distances);
        int sum = 0;
        for (int h=0; h<assignments.length; h++){
            for (int g=0; g<assignments[0].length; g++){
            }
        }
        for (int l=0; l<assignments.length; l++){
            sum += newdistances[assignments[l][0]][assignments[l][1]];//0!
        }
        partialMatchingList.put(r+"_"+u, sum);
        return sum;
    }

    public static HashMap<ArrayList<Edge>, Integer> singleRound(ArrayList<Umpire> umpires, int u, int r, HashMap<ArrayList<Edge>, Integer> solution, ArrayList<Edge> edges){
        nodeCount++;
        int up = ((u+1)%(nteams/2));
        int rp = r;
        if (u==nteams/2-1) rp++;
        ArrayList<Edge> A = sortedFeasable(umpires.get(u).schedule.get(r-1).edges, umpires.get(u), umpires);
        for (Edge e: A){
            umpires.get(u).addGameToSchedule(e.destination, e.distance, false);
            edges.add(e);
            if (rp!=r+1){
                if (homeTownFeasableNew(umpires.get(u))<=(nteams*2-2)-r){
                    if (sumSolution()==0 || sumUmpires(umpires)+LB[r][nteams*2-3]+h(umpires, u, r)<=sumSolution()){
                        solution = singleRound(umpires, up, rp, solution, edges);
                    }
                }
            }
            else{
                //add to array solution
                solution.put(new ArrayList<Edge>(edges), sumUmpires(umpires));
            }
            umpires.get(u).removeFromSchedule(e.destination, e.distance, false);
            edges.remove(e);
        }
        return solution;
    }

    public static HashMap<ArrayList<Edge>, Integer> singleRoundNotR0(ArrayList<Umpire> umpires, int u, int r, int ir, int rr, HashMap<ArrayList<Edge>, Integer> solution, ArrayList<Edge> edges){
        nodeCount++;
        int up = ((u+1)%(nteams/2));
        int rp = r;
        if (u==nteams/2-1) rp++;
        ArrayList<Edge> A = sortedFeasableNotR0(umpires.get(u).schedule.get(r-ir).edges, umpires.get(u), umpires, r-ir);
        for (Edge e: A){
            umpires.get(u).addGameToSchedule(e.destination, e.distance, true);
            edges.add(e);
            if (rp!=r+1){
                if (sumUmpires(umpires)+LB[r+1][rr]+hL(umpires, u, r)<=S[ir][rr] || S[ir][rr]==0){ //solution
                    solution = singleRoundNotR0(umpires, up, rp, ir, rr, solution, edges); //solution!!
                }
            }
            else{
                //add to array solution
                solution.put(new ArrayList<Edge>(edges), sumUmpires(umpires));
            }
            umpires.get(u).removeFromSchedule(e.destination, e.distance, true);
            edges.remove(e);
        }
        return solution;
    }

    public static ArrayList<ArrayList<Edge>> sortedPossibleSolutions(ArrayList<Umpire> umpires, int r){
        HashMap<ArrayList<Edge>, Integer> solution = singleRound(umpires, 0, r, new HashMap<>(), new ArrayList<>());
        // Create a list to hold the sorted keys
        ArrayList<ArrayList<Edge>> sortedKeys = new ArrayList<>();

        // Sort the HashMap by values
        List<Map.Entry<ArrayList<Edge>, Integer>> list = new LinkedList<>(solution.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<ArrayList<Edge>, Integer>>() {
            public int compare(Map.Entry<ArrayList<Edge>, Integer> o1, Map.Entry<ArrayList<Edge>, Integer> o2) {
                return (o1.getValue()).compareTo(o2.getValue());
            }
        });

        // Add the sorted keys to the ArrayList
        for (Map.Entry<ArrayList<Edge>, Integer> entry : list) {
            sortedKeys.add(entry.getKey());
        }
        return sortedKeys;
    }

    public static ArrayList<ArrayList<Edge>> skereSortedPossibleSolutions(ArrayList<Umpire> umpires, int r, int ir, int rr){
        HashMap<ArrayList<Edge>, Integer> solution = singleRoundNotR0(umpires, 0, r, ir, rr, new HashMap<>(), new ArrayList<>());
        // Create a list to hold the sorted keys
        ArrayList<ArrayList<Edge>> sortedKeys = new ArrayList<>();

        // Sort the HashMap by values
        List<Map.Entry<ArrayList<Edge>, Integer>> list = new LinkedList<>(solution.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<ArrayList<Edge>, Integer>>() {
            public int compare(Map.Entry<ArrayList<Edge>, Integer> o1, Map.Entry<ArrayList<Edge>, Integer> o2) {
                return (o1.getValue()).compareTo(o2.getValue());
            }
        });
        // Add the sorted keys to the ArrayList
        for (Map.Entry<ArrayList<Edge>, Integer> entry : list) {
            sortedKeys.add(entry.getKey());
        }
        return sortedKeys;
    }


    public static boolean checkFeasableRound(ArrayList<Umpire> umpires, ArrayList<Edge> e){
        for (int a=0; a<nteams/2; a++){
            if (!isFeasable(e.get(a).destination, umpires.get(a), umpires)) return false;
        }
        return true;
    }

    public static void RoundBound(ArrayList<Umpire> umpires, int r){
        EffectiveNodeCount += nteams/2;
        ArrayList<ArrayList<Edge>> A = sortedPossibleSolutions(umpires, r);
        //if (!checkForBetterSolution(umpires, false)) return;
        for (ArrayList<Edge> e: A){
            for (int u=0; u<nteams/2; u++){
                umpires.get(u).addGameToSchedule(e.get(u).destination, e.get(u).distance, false);
            }
            if (r+1!=nteams*2-2){
                if (sumUmpires(umpires)+LB[r][nteams*2-3]<=sumSolution() || sumSolution()==0){
                    RoundBound(umpires, r+1);
                }
            }
            else{
                if (fullFeasable(umpires) && checkForBetterSolution(umpires, false)){
                    solution.clear();
                    for (Umpire ump: umpires){
                        solution.add(new Umpire(ump));
                    }
                    writeToFile(solution);
                }
//                else System.out.println("Infeasable: "+sumUmpires(umpires));
            }
            for (int u=0; u<nteams/2; u++){
                umpires.get(u).removeFromSchedule(e.get(u).destination, e.get(u).distance, false);
            }
        }
    }

    public static void BranchBound(ArrayList<Umpire> umpires, int u, int r){
        nodeCount++;
        int up = ((u+1)%(nteams/2));
        int rp = r;
        if (u==nteams/2-1) rp++;
        ArrayList<Edge> A = sortedFeasable(umpires.get(u).schedule.get(r-1).edges, umpires.get(u), umpires);
        if (!checkForBetterSolution(umpires, false)) return;
        for (Edge e: A){
            umpires.get(u).addGameToSchedule(e.destination, e.distance, false);
            if (rp!=nteams*2-2){
                //if (!checkForBetterSolution(umpires, false)) continue;
                if (homeTownFeasableNew(umpires.get(u))<=(nteams*2-2)-r){
                    if (sumSolution()==0 || sumUmpires(umpires)+LB[r][nteams*2-3]+h(umpires, u, r)<=sumSolution()){
                        BranchBound(umpires, up, rp);
                    }
                }
            }
            else{
                if (fullFeasable(umpires) && checkForBetterSolution(umpires, false)){
                    solution.clear();
                    for (Umpire ump: umpires){
                        solution.add(new Umpire(ump));
                    }
                    writeToFile(solution);
                }
            }
            umpires.get(u).removeFromSchedule(e.destination, e.distance, false);
        }
    }

    public static void MatchBound(ArrayList<Umpire> umpires, int u, int r, int ir, int rr, int thisSolution){
        if (solutionfound) return;
        int up = ((u+1)%(nteams/2));
        int rp = r;
        if (u==nteams/2-1) rp++;
        ArrayList<Edge> A = sortedFeasableNotR0(umpires.get(u).schedule.get(r-ir).edges, umpires.get(u), umpires, r-ir);
        for (Edge e: A){
            thisSolution += e.distance;
            umpires.get(u).addGameToSchedule(e.destination, e.distance, true);
            if (rp!=rr){
                //if (!checkForBetterSolution(umpires, false)) continue;
                if (thisSolution + LB[r+1][rr] /*+ h(umpires, u, r)*/ <= S[ir][rr] || S[ir][rr]==0)
                    MatchBound(umpires, up, rp, ir, rr, thisSolution);
            }
            else{
                if (thisSolution < S[ir][rr] || S[ir][rr]==0){
                    S[ir][rr] = thisSolution;
                }
            }
            thisSolution -= e.distance;
            umpires.get(u).removeFromSchedule(e.destination, e.distance, true);
        }
    }

    public static void MatchRound(ArrayList<Umpire> umpires, int r, int ir, int rr, int thisSolution){
        if (solutionfound) return;
        ArrayList<ArrayList<Edge>> A = skereSortedPossibleSolutions(umpires, r, ir, rr);
        for (ArrayList<Edge> e: A){
            for (int u=0; u<nteams/2; u++){
                umpires.get(u).addGameToSchedule(e.get(u).destination, e.get(u).distance, true);
                thisSolution += e.get(u).distance;
            }
            if (r+1!=rr){
                if (thisSolution+LB[r+1][rr]<=S[ir][rr] || S[ir][rr]==0){
                    MatchRound(umpires, r+1, ir, rr, thisSolution);
                }
            }
            else{
                if (thisSolution < S[ir][rr] || S[ir][rr]==0){
                    S[ir][rr] = thisSolution;
                }
            }
            for (int u=0; u<nteams/2; u++){
                umpires.get(u).removeFromSchedule(e.get(u).destination, e.get(u).distance, true);
                thisSolution -= e.get(u).distance;
            }
        }
    }

    public static void calculateMatching(int round, int maxround){
        //use BranchBound to calculate for this round => make sure the edges of the second round are put to 0
        List<Game> roundlist = tournament.subList(round*nteams/2, maxround*nteams/2);
        ArrayList<Game> roundgames = new ArrayList<>(roundlist);
        ArrayList<Umpire> umpireGang = new ArrayList<>();
        for (int u=0; u<nteams/2; u++){
            umpireGang.add(new Umpire(9999));
            umpireGang.get(u).addGameToSchedule(new Game (roundgames.get(u)), 0, true);
        }
        //System.out.println("Start Matchbound: "+round+" - "+maxround);
        if (!fancyLB) MatchBound(umpireGang, 0, round, round, maxround, 0);
        if (fancyLB) MatchRound(umpireGang, round, round, maxround, 0);
    }

    public static void iniatiliseCalculateLBs() {
        S = new int[nteams * 2 - 2][nteams * 2 - 2];
        LB = new int[nteams * 2 - 2][nteams * 2 - 2];
    }


    public static void CalculateLBs(){
        for (int r=nteams*2-4; r>-1; r--){
            if (S[r][r+1] == 0) calculateMatching(r, r+1);
            //S[r][r+1] = 0; //value of matching between rounds r and r+1 => solution => games??//tournament??
            for (int r2=r+1; r2<nteams*2-3; r2++){
                LB[r][r2] = S[r][r+1] + LB[r+1][r2];
            }
        }
        for (int k=2; k<nteams*2-4; k++){//-4
            int r = nteams*2-3-k;//-3
            while (r>=1){
                for (int rr=r+k-1; rr > r-2; rr--){ //-1 //+1
                    if (S[rr][r+k] == 0) calculateMatching(rr, r+k);
                    for (int r1 = rr; r1>-1; r1--){
                        for (int r2 = r+k; r2 < nteams*2-2; r2++){
                            LB[r1][r2] = Math.max(LB[r1][r2], LB[r1][rr]+S[rr][r+k]+LB[r+k][r2]);
                        }
                    }
                }
                r -= k; //k? //1
            }
            writeLBToFile();
            writeSToFile();
            writeIncrementLBToFile();
        }
    }

    public static void writeLBToFile(){
        try{
            FileWriter writer = new FileWriter("LB_"+instanceName+"_"+q1+"_"+q2+"_"+hungarian+"_"+partialLB+".txt");
            for (int a= 0; a < LB.length; a++) {
                for (int b = 0; b < LB.length; b++) {
                    writer.write(LB[a][b] + " ");
                }
                writer.write("\n");
            }
            writer.close();
        } catch (IOException e) {
        System.err.println("Error writing to the file: " + e.getMessage());
        }
    }

    public static void readOldLBs(){
        try{
            FileReader reader = new FileReader("S_"+instanceName+"_"+q1+"_"+q2+"_"+hungarian+"_"+partialLB+".txt");
            BufferedReader br = new BufferedReader(reader);
            String line;
            int row = 0;
            while ((line = br.readLine()) != null && row < nteams * 2 - 2) {
                String[] values = line.trim().split("\\s+");
                for (int col = 0; col < values.length; col++) {
                    S[row][col] = Integer.parseInt(values[col]);
                }
                row++;
            }
        } catch (IOException e) {
            System.err.println("File \"S_"+instanceName+"_"+q1+"_"+q2+"_"+hungarian+"_"+partialLB+".txt\" does not exist: " + e.getMessage());
        }
        for (int i = 0; i < nteams * 2 - 2; i++) {
            for (int j = 0; j < nteams * 2 - 2; j++) {
                System.out.print(S[i][j] + " ");
            }
            System.out.println();
        }
    }

    public static void writeSToFile(){
        try{
            FileWriter writer = new FileWriter("S_"+instanceName+"_"+q1+"_"+q2+"_"+hungarian+"_"+partialLB+".txt");
            for (int a= 0; a < S.length; a++) {
                for (int b = 0; b < S.length; b++) {
                    writer.write(S[a][b] + " ");
                }
                writer.write("\n");
            }
            writer.close();
        } catch (IOException e) {
            System.err.println("Error writing to the file: " + e.getMessage());
        }
    }

    public static void writeIncrementLBToFile(){
        try{
            FileWriter writer = new FileWriter("incrementLB_"+instanceName+"_"+q1+"_"+q2+"_"+hungarian+"_"+partialLB+".txt", true);
            writer.write(((System.currentTimeMillis()-startTime)/1000.0)+","+LB[0][nteams*2-3]+"\n");
            writer.close();
        } catch (IOException e) {
            System.err.println("Error writing to the file: " + e.getMessage());
        }
    }

    public static void writeToFile(List<Umpire> umpires){
        try {
            FileWriter writer = new FileWriter("solution_"+instanceName+"_"+q1+"_"+q2+".txt");
            for (Umpire u: umpires){
                System.out.println(u);
                writer.write(u+"\n");
            }
            writer.close();
            int sumSolution = 0;
            for (Umpire u: umpires){
                sumSolution += u.distance;
            }
            System.out.println("Distance: "+sumSolution);
            System.out.println("Successfully wrote to the file: solution"+"_"+instanceName+"_"+q1+"_"+q2+".txt");
            System.out.println("Time: " + (System.currentTimeMillis()-startTime)/1000.0 + " s");
            String fname = "increment_"+instanceName+"_"+q1+"_"+q2+"_"+method+"_"+parallel+".txt";
            if (fancyLB) fname = "increment_"+instanceName+"_"+q1+"_"+q2+"_"+method+"_"+parallel+"_"+hungarian+"_"+partialLB+".txt";
            FileWriter wr = new FileWriter(fname, true);
            wr.write(((System.currentTimeMillis()-startTime)/1000.0)+","+sumSolution+"\n");
            wr.close();
        } catch (IOException e) {
            System.err.println("Error writing to the file: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        // java Main instances/umps8.txt umps8 q1 q2
        int boostsln = 0;
        if (args.length == 4){
            startTime = System.currentTimeMillis();
            tournament = makeTournament(args[0]);
            instanceName = args[1];
            q1 = Integer.parseInt(args[2]);
            q2 = Integer.parseInt(args[3]);
            method = "RoundBound";
            parallel = true;
            fancyLB = true;
            hungarian = true;
            partialLB = true;
            useOldLBs = false;
            hash = false;
        }
        else {
            System.out.println("java Main <instance_file> <umpsN> <q1> <q2>");
        }
        fillHomeTOWNtoVISIT();
        ArrayList<Umpire> umpires = new ArrayList<>();
        for (int i=1; i<=nteams/2; i++){
            umpires.add(new Umpire(i));
        }
        for (int g=0; g<nteams/2; g++){
            umpires.get(g).addGameToSchedule(tournament.get(g), 0, false);
        }
        try{
            String fname = "increment_"+instanceName+"_"+q1+"_"+q2+"_"+method+"_"+parallel+".txt";
            if (fancyLB) fname = "increment_"+instanceName+"_"+q1+"_"+q2+"_"+method+"_"+parallel+"_"+hungarian+"_"+partialLB+".txt";
            FileWriter wr = new FileWriter(fname);
            wr.close();
            FileWriter write = new FileWriter("incrementLB_"+instanceName+"_"+q1+"_"+q2+"_"+hungarian+"_"+partialLB+".txt");
            write.close();
        } catch (IOException e) {System.err.println("Error writing to the file: " + e.getMessage());}
        calculatePartialBounds();
        boostSolution(boostsln);
        iniatiliseCalculateLBs();
        if (useOldLBs) readOldLBs();
        if (parallel){
            LBThread lbt = new LBThread();
            lbt.start();
        }
        if (!parallel) CalculateLBs();
        if (Objects.equals(method, "BranchBound")) BranchBound(umpires, 0, 1);
        if (Objects.equals(method, "RoundBound")) RoundBound(umpires, 1);
        solutionfound = true;
        writeToFile(solution);
        long endTime = System.currentTimeMillis();
        System.out.println("Total time: " + (endTime-startTime)/1000.0 + " s");
        System.out.println("NodeCount: "+nodeCount);
        System.out.println("EffectiveNodeCount: "+EffectiveNodeCount);
        for (int a= 0; a < LB.length; a++){
            for (int b= 0; b < LB.length; b++){
                System.out.print(LB[a][b]+" ");
            }
            System.out.println();
        }
        for (int a= 0; a < S.length; a++){
            for (int b= 0; b < S.length; b++){
                System.out.print(S[a][b]+" ");
            }
            System.out.println();
        }
    }
}
