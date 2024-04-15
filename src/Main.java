import java.io.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Main {
    static int q1 = 7;
    static int q2 = 2;
    static int nteams;
    static ArrayList<Umpire> solution = new ArrayList<>();
    static ArrayList<Game> tournament = new ArrayList<>();
    //static int[] matchSolutions;
    static int[][] S;
    static int[][] LB;
    //static ArrayList<Umpire> umpires = new ArrayList<>();
    static boolean solutionfound = false;
    static int nodeCount = 0;
    static boolean verbose = true;

    private static ArrayList<Game> makeTournament(){
        InputReader inputReader = new InputReader("instances/umps12.txt");
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

    public static boolean skereisFeasable(Game game, Umpire umpire, ArrayList<Umpire> umpires, int round){
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
        Collections.sort(sortedGames, Comparator.comparingInt(Edge::getDistance));
        return sortedGames; //returns null if no feasable solutions are found
    }

    public static ArrayList<Edge> skeresortedFeasable(ArrayList<Edge> edges, Umpire umpire, ArrayList<Umpire> umpires, int round){
        ArrayList<Edge> sortedGames = new ArrayList<>();
        for(Edge e: edges){
            if (!skereisFeasable(e.destination, umpire, umpires, round)) continue;
            sortedGames.add(e);
        }
        Collections.sort(sortedGames, Comparator.comparingInt(Edge::getDistance));
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

    public static void fairtry(ArrayList<Umpire> umpires){
        for (int h=1; h<nteams*2-2; h++){
            for (Umpire u: umpires){
                Edge coolestEdge = chooseSmallestFeasable(u.schedule.get(h-1).edges, u, umpires);
                if (coolestEdge == null){
                    System.out.println("Sad life");
                    writeToFile(umpires);
                    return;
                }
                u.schedule.add(coolestEdge.destination);
            }
        }
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
        return sumSolution>sumUmpires;
    }

    public static int sumUmpires(ArrayList<Umpire> umpires){
        int sumUmpires = 0;
        for (Umpire u: umpires){
            sumUmpires += u.distance;
        }
        return sumUmpires;
    }

    public static int sumSolution(){
        int sumSolution = 0;
        if (solution.isEmpty()) return sumSolution;
        for (Umpire u: solution){
            sumSolution += u.distance;
        }
        return sumSolution;
    }

    public static void BranchBound(ArrayList<Umpire> umpires, int u, int r){
        nodeCount++;
        int up = ((u+1)%(nteams/2));
        int rp = r;
        if (u==nteams/2-1) rp++;
        ArrayList<Edge> A = sortedFeasable(umpires.get(u).schedule.get(r-1).edges, umpires.get(u), umpires);
        if (!checkForBetterSolution(umpires, false)) return;
        for (Edge e: A){
            umpires.get(u).addGameToSchedule(e.destination, e.distance);
            if (rp!=nteams*2-2){
                //if (!checkForBetterSolution(umpires, false)) continue;
                if (homeTownFeasable(umpires.get(u))<=(nteams*2-2)-r){
                    if (sumUmpires(umpires)+LB[r][nteams*2-3]<=sumSolution() || sumSolution()==0){
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
                    if (verbose) writeToFile(solution);
                }
            }
            umpires.get(u).removeFromSchedule(e.destination, e.distance);
        }
    }

//    public static void MatchBound0(ArrayList<Game> roundgames, int u, int r, int rr, int thisSolution){
//        int up = ((u+1)%(nteams/2));
//        int rp = r;
//        if (u==nteams/2-1) rp++;
//        //constraint checkers up to a certain round? => work with roundgames! but with adapted u
//        for (Edge e: roundgames.get(r*nteams/2+u).edges){
//            thisSolution += e.distance;
//            if (rp!=rr){
//                //if (!checkForBetterSolution(umpires, false)) continue;
//                MatchBound(roundgames, up, rp, rr, thisSolution);
//            }
//            else{
//                if (thisSolution < S[r][rr] || S[r][rr]==0){
//                    S[r][rr] = thisSolution;
//                    System.out.println("ThisSolution: "+thisSolution);
//                }
//            }
//            thisSolution -= e.distance;
//        }
//    }

    public static void MatchBound(ArrayList<Umpire> umpires, int u, int r, int ir, int rr, int thisSolution){
        if (solutionfound) return;
        int up = ((u+1)%(nteams/2));
        int rp = r;
        if (u==nteams/2-1) rp++;
        //constraint checkers up to a certain round? => work with roundgames! but with adapted u
        //if (thisSolution + S[r][rr] > S[ir][rr] && S[ir][rr]!=0) return;
        //if (thisSolution >= S[ir][rr] && S[ir][rr]!=0) return;
        ArrayList<Edge> A = skeresortedFeasable(umpires.get(u).schedule.get(r-ir).edges, umpires.get(u), umpires, r-ir);
        for (Edge e: A){
            thisSolution += e.distance;
            umpires.get(u).addGameToSchedule(e.destination, e.distance);
            if (rp!=rr){
                //if (!checkForBetterSolution(umpires, false)) continue;
                if (thisSolution + LB[r+1][rr] <= S[ir][rr] || S[ir][rr]==0)
                    MatchBound(umpires, up, rp, ir, rr, thisSolution);
            }
            else{
                if (thisSolution < S[ir][rr] || S[ir][rr]==0){
                    S[ir][rr] = thisSolution;
                    if (verbose) System.out.println("ThisSolution: "+thisSolution);
                }
            }
            thisSolution -= e.distance;
            umpires.get(u).removeFromSchedule(e.destination, e.distance);
        }
    }


    //precalculate every matching? 2 layers deep?
    public static void calculateMatching(int round, int maxround){
        //use BranchBound to calculate for this round => make sure the edges of the second round are put to 0
        List<Game> roundlist = tournament.subList(round*nteams/2, maxround*nteams/2);
        ArrayList<Game> roundgames = new ArrayList<>(roundlist);
        ArrayList<Umpire> umpireGang = new ArrayList<>();
        for (int u=0; u<nteams/2; u++){
            umpireGang.add(new Umpire(9999));
            umpireGang.get(u).addGameToSchedule(new Game (roundgames.get(u)), 0);
        }
        //System.out.println("Start Matchbound: "+round+" - "+maxround);
        MatchBound(umpireGang, 0, round, round, maxround, 0);
    }

    public static void fakeCalculateLBs() {
        S = new int[nteams * 2 - 2][nteams * 2 - 2];
        LB = new int[nteams * 2 - 2][nteams * 2 - 2];
    }

    public static void correctCalculateLBs(){
        S = new int[nteams*2-2][nteams*2-2];
        LB = new int[nteams*2-2][nteams*2-2];
        for (int r=nteams*2-4; r>1; r--){
            if (S[r][r+1] == 0) calculateMatching(r, r+1);
            //S[r][r+1] = 0; //value of matching between rounds r and r+1 => solution => games??//tournament??
            for (int r2=r+1; r2<nteams*2-3; r2++){
                LB[r][r2] = S[r][r+1] + LB[r+1][r2];
            }
        }
        for (int k=1; k<nteams*2-4; k++){
            int r = nteams*2-3-k;
            while (r>=0){
                for (int rr=r+k-2; rr > r+1; rr--){
                    if (S[rr][r+k] == 0) calculateMatching(rr, r+k);
                    for (int r1 = rr; r1>0; r1--){
                        for (int r2 = r+k; r2 < nteams*2-3; r2++){
                            LB[r1][r2] = Math.max(LB[r1][r2], LB[r1][rr]+S[rr][r+k]+LB[r+k][r2]);
                        }
                    }
                }
                r -= k;
            }
        }
    }

    public static void CalculateLBs(){
        S = new int[nteams*2-2][nteams*2-2];
        LB = new int[nteams*2-2][nteams*2-2];
        for (int r=nteams*2-4; r>-1; r--){
            if (S[r][r+1] == 0) calculateMatching(r, r+1);
            //S[r][r+1] = 0; //value of matching between rounds r and r+1 => solution => games??//tournament??
            for (int r2=r+1; r2<nteams*2-3; r2++){
                LB[r][r2] = S[r][r+1] + LB[r+1][r2];
            }
        }
        for (int k=2; k<nteams*2-4; k++){
            int r = nteams*2-3-k;
            while (r>=1){
                for (int rr=r+k-1; rr > r+1; rr--){
                    if (S[rr][r+k] == 0) calculateMatching(rr, r+k);
                    for (int r1 = rr; r1>-1; r1--){
                        for (int r2 = r+k; r2 < nteams*2-2; r2++){
                            LB[r1][r2] = Math.max(LB[r1][r2], LB[r1][rr]+S[rr][r+k]+LB[r+k][r2]);
                        }
                    }
                }
                r -= k;
            }
        }
    }

    public static void writeToFile(ArrayList<Umpire> umpires){
        try {
            FileWriter writer = new FileWriter("solution_"+nteams+"_"+q1+"_"+q2+".txt");
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
            System.out.println("Successfully wrote to the file: solution"+"_"+nteams+"_"+q1+"_"+q2+".txt");
        } catch (IOException e) {
            System.err.println("Error writing to the file: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        tournament = makeTournament();
        ArrayList<Umpire> umpires = new ArrayList<>();
        for (int i=1; i<=nteams/2; i++){
            umpires.add(new Umpire(i));
        }
        for (int g=0; g<nteams/2; g++){
            umpires.get(g).addGameToSchedule(tournament.get(g), 0);
        }
//        matchSolutions = new int[nteams*2-3];
//        //calculateMatching(0, 3);
//        int totaldistance = 0;
//        for(int r=0; r<nteams*2-4; r++){
//            calculateMatching(r, r+1);
//            System.out.println("//////////////////////////////////////////////////////////////////////////");
//        }
//        for (int i: matchSolutions){
//            totaldistance += i;
//            System.out.println(i);
//        }
//        System.out.println("Totaldistance: "+totaldistance);
        //fairtry(umpires);
        LBThread lbt = new LBThread();
        lbt.start();
//        CalculateLBs();
        fakeCalculateLBs();
        //long BBstartTime = System.currentTimeMillis();
        BranchBound(umpires, 0, 1);
        solutionfound = true;
        writeToFile(solution);
        long endTime = System.currentTimeMillis();
        System.out.println("Total time: " + (endTime-startTime)/1000.0 + " s");
        //System.out.println("Preprocessing time: " + (BBstartTime-startTime)/1000.0 + " s");
        //System.out.println("BranchBound time: " + (endTime-BBstartTime)/1000.0 + " s");
        System.out.println("NodeCount: "+nodeCount);
        if (verbose){
            for (int a= 0; a < LB.length; a++){
                for (int b= 0; b < LB.length; b++){
                    System.out.print(LB[a][b]+" ");
                }
                System.out.println();
            }
        }
    }
}
