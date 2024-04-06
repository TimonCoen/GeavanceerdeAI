import java.io.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class Main {
    static int q1 = 4;
    static int q2 = 2;
    static int nteams;
    static ArrayList<Umpire> solution = new ArrayList<>();
    //static ArrayList<Umpire> umpires = new ArrayList<>();

    private static ArrayList<Game> makeTournament(){
        ArrayList<Game> tournament = new ArrayList<>();
        InputReader inputReader = new InputReader("instances/umps8.txt");
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
                            Edge newEdge = new Edge(prevGame, newGame, travelDist);
                            prevGame.addEdge(newEdge);
                        }
                    }
                    tournament.add(newGame);
                }
            }
            roundCounter++;
        }
        return tournament;
    }

    public static boolean isFeasable(Game game, Umpire umpire, ArrayList<Umpire> umpires){
        boolean feasable = true;
        int c1 = q1;
        int c2 = q2;
        //already taken by other umpire
        for (Umpire u: umpires){
            if (u == umpire) continue;
            if (game.round >= u.schedule.size()) continue;
            if (u.schedule.get(game.round) == game) feasable = false;
        }
        if (game.round < c1) c1 = game.round+1;
        for (int r1 = game.round-(c1-1); r1<game.round; r1++){
            if(umpire.schedule.get(r1).home == game.home) feasable = false;
        }
        if (game.round < c2) c2 = game.round+1;
        for (int r2 = game.round-(c2-1); r2<game.round; r2++){
            if(umpire.schedule.get(r2).home == game.home) feasable = false;
            if(umpire.schedule.get(r2).out == game.home) feasable = false;
            if(umpire.schedule.get(r2).home == game.out) feasable = false;
            if(umpire.schedule.get(r2).out == game.out) feasable = false;
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

    public static boolean fullFeasable(ArrayList<Umpire> umpires){
        for (Umpire u: umpires){
            for (Integer i : u.homeTownVisit){
                if (i < 1){
                    return false;
                }
            }
        }
        return true;
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

    public static void BranchBound(ArrayList<Umpire> umpires, int u, int r){
        int up = ((u+1)%(nteams/2));
        int rp = r;
        if (u==nteams/2-1) rp++;
        ArrayList<Edge> A = sortedFeasable(umpires.get(u).schedule.get(r-1).edges, umpires.get(u), umpires);
        if (!checkForBetterSolution(umpires, false)) return;
        for (Edge e: A){
            umpires.get(u).addGameToSchedule(e.destination, e.distance);
            if (rp!=nteams*2-2){
                //if (!checkForBetterSolution(umpires, false)) continue;
                BranchBound(umpires, up, rp);
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
            umpires.get(u).removeFromSchedule(e.destination, e.distance);
        }
    }

    public static void writeToFile(ArrayList<Umpire> umpires){
        try {
            FileWriter writer = new FileWriter("solution.txt");
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
            System.out.println("Successfully wrote to the file: solution.txt");
        } catch (IOException e) {
            System.err.println("Error writing to the file: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        ArrayList<Game> tournament = makeTournament();
        ArrayList<Umpire> umpires = new ArrayList<>();
        for (int i=1; i<=nteams/2; i++){
            umpires.add(new Umpire(i));
        }
        for (int g=0; g<nteams/2; g++){
            umpires.get(g).addGameToSchedule(tournament.get(g), 0);
        }
        //fairtry(umpires);
        BranchBound(umpires, 0, 1);
        writeToFile(solution);
    }
}
