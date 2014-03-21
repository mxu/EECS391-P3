import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class Planner {

    // generate a plan using A* to do a forward state space search
    public static LinkedList<PlanState> plan(PlanState initial, PlanState goal, boolean canBuildPeasant, String fileName) {
        System.out.println("Planner initialized for:\n" +
                           "\tInitial: " + initial + "\n" +
                           "\tGoal: " + goal);

        ArrayList<PlanState> open = new ArrayList<PlanState>();
        ArrayList<PlanState> closed = new ArrayList<PlanState>();

        HashMap<PlanState, PlanState> parents = new HashMap<PlanState, PlanState>();
        HashMap<PlanState, Integer> gScore = new HashMap<PlanState, Integer>(); // cost along best known path
        HashMap<PlanState, Integer> fScore = new HashMap<PlanState, Integer>(); // total estimated cost

        open.add(initial);
        gScore.put(initial, 0);
        fScore.put(initial, getHScore(initial, goal));

        while(open.size() > 0) {
            PlanState current = getMinVal(fScore, open);
            // return the least cost path if the end has been reached
            if(goalTest(current, goal)) {
                LinkedList<PlanState> result = buildPath(parents, current);
                writeFile(result, fileName);
                return result;
            }
            // move expanded position to the closed list
            open.remove(current);
            closed.add(current);
            // evaluate next possible moves from current location
            for(PlanState neighbor: getNeighbors(current, canBuildPeasant)) {
                // ignore locations in the closed set
                if(closed.contains(neighbor)) continue;
                int tempScore = gScore.get(current) + getGScore(current, neighbor);
                // explore low cost paths
                if(!open.contains(neighbor) || tempScore <= gScore.get(neighbor)) {
                    // track the path
                    parents.put(neighbor, current);
                    gScore.put(neighbor, tempScore);
                    // calculate heuristic cost
                    fScore.put(neighbor, gScore.get(neighbor) + getHScore(neighbor, goal));
                    if(!open.contains(neighbor)) open.add(neighbor);
                }
            }
        }
        // unable to find a valid path from initial to goal state
        return null;
    }

    private static boolean goalTest(PlanState state, PlanState goal) {
        return state.gold == goal.gold && state.wood == goal.wood;
    }

    /*
    Get the heuristic cost of getting from state a to state b
    Priorities:
    peasants built          [0 to 2]
    gold collected          [0 to 3000]
    wood collected          [0 to 2000]
    resources carried       [0 to 300]
    distance to resource    [0 to 50]
     */
    private static int getHScore(PlanState a, PlanState b) {
        int score = 0;
        //@TODO: implement heuristic cost algorithm
        return score;
    }

    private static int getGScore(PlanState a, PlanState b) {
        int score = 0;
        //@TODO: implement cost algorithm
        return score;
    }

    // return item in list mapped to the lowest score
    private static PlanState getMinVal(HashMap<PlanState, Integer> score, List<PlanState> list) {
        PlanState result = null;
        int minScore = Integer.MAX_VALUE;
        for(PlanState state: list) {
            if(score.get(state) < minScore) {
                result = state;
                minScore = score.get(state);
            }
        }
        return result;
    }

    // get valid moves from a given state
    private static List<PlanState> getNeighbors(PlanState state, boolean canBuildPeasant) {
        ArrayList<PlanState> result = new ArrayList<PlanState>();
        //@TODO: generate valid moves from state
        // iterate over possible actions
            // check preconditions
                // if met add action
        return result;
    }

    // extract shortest path from a given point to the root node by tracing the parents
    private static LinkedList<PlanState> buildPath(HashMap<PlanState, PlanState> parents, PlanState state) {
        LinkedList<PlanState> result = buildPath(parents, parents.get(state));
        result.add(state);
        return result;
    }

    // write plan to file
    private static void writeFile(LinkedList<PlanState> plan, String fileName) {
        //@TODO: implement algorithm for writing plan to file
    }
}
