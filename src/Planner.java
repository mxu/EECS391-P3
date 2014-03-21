import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class Planner {

    private static ArrayList<PlanAction> actions;

    // generate a plan using A* to do a forward state space search
    public static LinkedList<PlanState> plan(PlanState initial, PlanState goal, String fileName) {
        System.out.println("Planner initialized for:\n" +
                           "\tInitial: " + initial + "\n" +
                           "\tGoal: " + goal);

        registerActions(initial, goal.peasants.size());

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
                System.out.println("Plan complete");
                LinkedList<PlanState> result = buildPath(parents, current);
                writeFile(result, fileName);
                return result;
            }
            // move expanded position to the closed list
            open.remove(current);
            closed.add(current);
            // System.out.println("Expanding " + fScore.get(current) + ": " + current);
            // evaluate next possible moves from current location
            for(PlanState neighbor: getNeighbors(current, goal)) {
                // ignore locations in the closed set
                if(closed.contains(neighbor)) continue;
                int tempScore = gScore.get(current) + neighbor.parentAction.getMakeSpan();
                // explore low cost paths
                if(!open.contains(neighbor) || tempScore <= gScore.get(neighbor)) {
                    // track the path
                    parents.put(neighbor, current);
                    gScore.put(neighbor, tempScore);
                    // calculate heuristic cost
                    fScore.put(neighbor, gScore.get(neighbor) + getHScore(neighbor, goal));
                    // System.out.println("\t" + neighbor.parentAction + ": " + gScore.get(neighbor) + " + " + getHScore(neighbor, goal) + " = " + fScore.get(neighbor));
                    if(!open.contains(neighbor)) open.add(neighbor);
                }
            }
        }
        System.out.print("Unable to find a valid path from initial to goal state");
        return null;
    }

    private static void registerActions(PlanState s, int maxPeasants) {
        actions = new ArrayList<PlanAction>();
        // move to, gather, and return from each resource node
        for(PlanResource resource: s.resources) {
            int resId = resource.getId();
            for(int i = 1; i <= maxPeasants; i++) {
                actions.add(new MoveAction(i, s, null, resId));
                actions.add(new GatherAction(i, resId, resource.getX(), resource.getY()));
                actions.add(new MoveAction(i, s, resId, null));
            }
        }
        // deposit cargo
        for(int i = 1; i <= maxPeasants; i++)
            actions.add(new DepositAction(i));
        // build peasant
        if(maxPeasants > 1) actions.add(new BuildPeasantAction());
    }

    private static boolean goalTest(PlanState s, PlanState goal) {
        return s.gold == goal.gold && s.wood == goal.wood;
    }

    /*
    Get the heuristic cost of getting from state a to state b
     */
    private static int getHScore(PlanState a, PlanState b) {
        int score = 0;
        // prioritize making peasants
        score += (b.peasants.size() - a.peasants.size()) * 100;
        // estimate cycles needed to gather resources
        int cyclesForGold = Math.max(b.gold - a.gold, 0) / (a.peasants.size() * 100);
        int cyclesForWood = Math.max(b.wood - a.wood, 0) / (a.peasants.size() * 100);
        // assume every resource is 30 steps away
        score += (cyclesForGold + cyclesForWood) * 60;
        return score;
    }

    // return item in list mapped to the lowest score
    private static PlanState getMinVal(HashMap<PlanState, Integer> score, List<PlanState> list) {
        PlanState result = null;
        int minScore = Integer.MAX_VALUE;
        for(PlanState state: list) {
            int stateScore = score.get(state);
            if(stateScore < minScore) {
                result = state;
                minScore = stateScore;
            }
        }
        return result;
    }

    // get valid moves from a given state
    private static List<PlanState> getNeighbors(PlanState s, PlanState goal) {
        ArrayList<PlanState> result = new ArrayList<PlanState>();
        for(PlanAction a: actions)
            if(a.isAllowedFor(s, goal)) result.add(a.applyTo(s));
        return result;
    }

    // extract shortest path from a given point to the root node by tracing the parents
    private static LinkedList<PlanState> buildPath(HashMap<PlanState, PlanState> parents, PlanState s) {
        LinkedList<PlanState> result = parents.containsKey(s) ? buildPath(parents, parents.get(s)) : new LinkedList<PlanState>();
        result.add(s);
        return result;
    }

    // write plan to file
    private static void writeFile(LinkedList<PlanState> plan, String fileName) {
        System.out.println("Writing plan to " + fileName);
        try {
            PrintWriter out = new PrintWriter(fileName);
            int i = 0;
            for(PlanState s: plan)
                out.println(i++ + ": " + s.parentAction);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
