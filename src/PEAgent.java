import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.agent.Agent;
import edu.cwru.sepia.environment.model.history.History;
import edu.cwru.sepia.environment.model.state.ResourceNode;
import edu.cwru.sepia.environment.model.state.State;
import edu.cwru.sepia.environment.model.state.Unit;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.Map;

public class PEAgent extends Agent {

    private static final long serialVersionUID = 0L;

    private int targetGold;             // gold needed to complete scenario
    private int targetWood;             // wood needed to complete scenario
    private boolean canBuildPeasant;    // indicate if additional peasants can be built
    private String fileName;            // name of plan output file

    private LinkedList<PlanState> plan; // list of planned actions

    public PEAgent(int playernum, String[] args) {
        super(playernum);
        // parse arguments from configuration
        targetGold      = Integer.parseInt(args[0]);
        targetWood      = Integer.parseInt(args[1]);
        canBuildPeasant = Boolean.parseBoolean(args[2]);
        fileName        = args[3];
        System.out.println("PEAgent initialized:\n" +
                           "\tTarget Gold: " + targetGold + "\n" +
                           "\tTarget Wood: " + targetWood + "\n" +
                           "\tBuild Peasants: " + (canBuildPeasant ? "enabled" : "disabled") + "\n" +
                           "\tPlan File: " + fileName);
    }

    @Override
    public Map<Integer, Action> initialStep(State.StateView newState, History.HistoryView stateHistory) {
        // generate initial state
        PlanState initial = new PlanState(0, 0);

        // identify units and create minimal data structures needed for planning
        for(int id: newState.getAllUnitIds()) {
            Unit.UnitView unit = newState.getUnit(id);
            String typeName = unit.getTemplateView().getName();
            if(typeName.equals("TownHall")) initial.townHall = new PlanActor(unit);
            if(typeName.equals("Peasant")) initial.peasants.add(new PlanPeasant(unit));
        }
        // first peasant is guaranteed to start next to the town hall
        PlanPeasant peasant = initial.peasants.get(0);
        peasant.setNextTo(initial.townHall);

        // identify resources and create minimal data structures needed for planning
        for(int id: newState.getAllResourceIds()) {
            ResourceNode.ResourceView resource = newState.getResourceNode(id);
            if(resource.getType().equals(ResourceNode.Type.GOLD_MINE)) initial.mines.add(new PlanResource(resource));
            if(resource.getType().equals(ResourceNode.Type.TREE)) initial.trees.add(new PlanResource(resource));
        }

        // generate goal state
        PlanState goal = new PlanState(targetGold, targetWood);
        // add optimal number of peasants to the goal state
        for(int i = 0; i < getOptimalPeasants(); i++)
            goal.peasants.add(new PlanPeasant(newState.getUnit(peasant.getId())));

        // pass initial and goal states to planner and get a plan
        plan = Planner.plan(initial, goal, canBuildPeasant, fileName);

        return middleStep(newState, stateHistory);
    }

    /*
    Assuming each peasant takes one cycle of 4 actions (move-gather-move-deposit)
    to collect 100 of a resource, and building a new peasant takes 1 action,
    amount of resources gathered over x cycles ...
    1 peasant: gather at 100x rate
        100x
    2 peasants: 4 cycles at 100x rate, then gather at 200x rate
        200(x - 4.25)
    3 peasants: 4 cycles at 100x rate, 2 cycles at 200x rate, then gather at 300x rate
        300(x - 6.5)
    100x and 200(x - 4.25) intersect at y = 850, so 1 peasant is optimal for <= 800 resources
    200(x - 4.25) and 300(x - 6.5) intersect at y = 1350, so 2 peasants are optimal for <= 1200 resources
    These values do not change between the given scenarios so they can be precomputed.
    */
    private int getOptimalPeasants() {
        if(!canBuildPeasant || (targetGold + targetWood) <= 800) return 1;
        if((targetGold + targetWood) <= 1200) return 2;
        return 3;
    }

    @Override
    public Map<Integer, Action> middleStep(State.StateView newState, History.HistoryView stateHistory) {
        //@TODO: implement algorithm for translating plan actions to unit commands
        return null;
    }

    @Override
    public void terminalStep(State.StateView newState, History.HistoryView stateHistory) {}

    @Override
    public void savePlayerData(OutputStream out) {}

    @Override
    public void loadPlayerData(InputStream in) {}
}
