import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.agent.Agent;
import edu.cwru.sepia.environment.model.history.History;
import edu.cwru.sepia.environment.model.state.ResourceNode;
import edu.cwru.sepia.environment.model.state.ResourceType;
import edu.cwru.sepia.environment.model.state.State;
import edu.cwru.sepia.environment.model.state.Unit;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

public class PEAgent extends Agent {

    private static final long serialVersionUID = 0L;

    private int targetGold;             // gold needed to complete scenario
    private int targetWood;             // wood needed to complete scenario
    private boolean canBuildPeasant;    // indicate if additional peasants can be built
    private String fileName;            // name of plan output file

    private boolean busy;               // indicates if an action is currently in progress
    private int townHall;               // town hall id

    private LinkedList<PlanState> plan; // list of planned actions

    public PEAgent(int playernum, String[] args) {
        super(playernum);
        // parse arguments from configuration
        targetGold      = Integer.parseInt(args[0]);
        targetWood      = Integer.parseInt(args[1]);
        canBuildPeasant = Boolean.parseBoolean(args[2]);
        fileName        = args[3];
        busy            = false;
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
        for(int id: newState.getUnitIds(playernum)) {
            Unit.UnitView unit = newState.getUnit(id);
            String typeName = unit.getTemplateView().getName();
            if(typeName.equals("TownHall")) townHall = id;
            if(typeName.equals("Peasant")) initial.peasants.add(new PlanPeasant());
        }

        // identify resources and create minimal data structures needed for planning
        for(int id: newState.getAllResourceIds()) {
            initial.resources.add(new PlanResource(newState.getResourceNode(id), newState.getUnit(townHall)));
        }

        // generate goal state
        PlanState goal = new PlanState(targetGold, targetWood);
        // add optimal number of peasants to the goal state
        for(int i = 0; i < getMaxPeasants(); i++)
            goal.peasants.add(new PlanPeasant());

        // pass initial and goal states to planner and get a plan
        plan = Planner.plan(initial, goal, fileName);
        // remove initial state since we are already here
        plan.removeFirst();
        System.out.println("Executing plan");
        //for(PlanState s: plan)
        //    System.out.println(s.parentAction + " -> " + s);
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
    private int getMaxPeasants() {
        if(!canBuildPeasant || (targetGold + targetWood) <= 800) return 1;
        if((targetGold + targetWood) <= 1200) return 2;
        return 3;
    }

    /*
    @TODO: Fix translation between plan steps and game state actions
    Pretty close but not working fully
     */
    @Override
    public Map<Integer, Action> middleStep(State.StateView newState, History.HistoryView stateHistory) {
        Map<Integer, Action> actions = new HashMap<Integer, Action>();
        List<Integer> peasants = new ArrayList<Integer>();
        PlanState nextState = plan.peek();
        PlanAction pAction = nextState.parentAction;

        for(int id: newState.getUnitIds(playernum)) {
            Unit.UnitView unit = newState.getUnit(id);
            String typeName = unit.getTemplateView().getName();
            if(typeName.equals("Peasant")) peasants.add(id);
        }

        if(pAction instanceof MoveAction) {
            MoveAction mAction = (MoveAction) pAction;
            Unit.UnitView townHallUnit = newState.getUnit(townHall);
            PlanResource resource = nextState.getResourceWithId(mAction.getOriginId() == null ?
                                                                mAction.getDestId() :
                                                                mAction.getOriginId());
            boolean done = false;
            boolean toTownHall = mAction.getDestId() == null;
            int i = 0, j = 0;
            int originX, originY, destX, destY;
            if(toTownHall) {
                originX = resource.getX();
                originY = resource.getY();
                destX = townHallUnit.getXPosition();
                destY = townHallUnit.getYPosition();
            } else {
                originX = townHallUnit.getXPosition();
                originY = townHallUnit.getYPosition();
                destX = resource.getX();
                destY = resource.getY();
            }
            // get the number of peasants that should be at the destination
            for(PlanPeasant peasant: nextState.peasants) {
                if(toTownHall && peasant.getNextTo() == null) i++;
                if(!toTownHall && peasant.getNextTo() != null) i++;
            }
            // check to see if the right number of peasants are there
            for(int id: peasants) {
                Unit.UnitView peasant = newState.getUnit(id);
                if(isAdjacent(peasant.getXPosition(), peasant.getYPosition(), destX, destY) &&
                   ++j == i) done = true;
            }
            if(done) {
                plan.removeFirst();
                busy = false;
            } else if(!busy) {
                busy = true;
                int k = 0;
                // order each peasant to move to the destination
                for(int id: peasants) {
                    Unit.UnitView peasant = newState.getUnit(id);
                    if(isAdjacent(peasant.getXPosition(), peasant.getYPosition(),
                                  originX, originY) && k++ < mAction.getK())
                        actions.put(id, Action.createCompoundMove(id, destX, destY));
                }
            }
        }

        if(pAction instanceof GatherAction) {
            GatherAction gAction = (GatherAction) pAction;
            boolean done = false;
            int i = 0;
            // check if each peasant at the target is carrying cargo
            for(int id: peasants) {
                Unit.UnitView peasant = newState.getUnit(id);
                if(isAdjacent(peasant.getXPosition(), peasant.getYPosition(),
                              gAction.getX(), gAction.getY()) &&
                   peasant.getCargoAmount() > 0 && ++i == gAction.getK()) done = true;
            }
            if(done) {
                plan.removeFirst();
                busy = false;
            } else if(!busy){
                busy = true;
                int j = 0;
                // order peasants to gather the target resource
                for(int id: peasants) {
                    Unit.UnitView peasant = newState.getUnit(id);
                    if(isAdjacent(peasant.getXPosition(), peasant.getYPosition(),
                                  gAction.getX(), gAction.getY()) &&
                       j++ < gAction.getK())
                        actions.put(id, Action.createCompoundGather(id, newState.resourceAt(gAction.getX(), gAction.getY())));
                }
            }
        }

        if(pAction instanceof DepositAction) {
            Unit.UnitView townHallUnit = newState.getUnit(townHall);
            // check if the correct amount of gold/wood has been gathered
            if(newState.getResourceAmount(playernum, ResourceType.GOLD) == nextState.gold &&
               newState.getResourceAmount(playernum, ResourceType.WOOD) == nextState.wood) {
                plan.removeFirst();
                busy = false;
            } else if(!busy) {
                busy = true;
                int i = 0;
                // order peasants at the town hall to deposit resources
                for(int id: peasants) {
                    Unit.UnitView peasant = newState.getUnit(id);
                    if(isAdjacent(peasant.getXPosition(), peasant.getYPosition(),
                                  townHallUnit.getXPosition(), townHallUnit.getYPosition()) &&
                       peasant.getCargoAmount() > 0 && i++ < ((DepositAction) pAction).getK())
                        actions.put(id, Action.createCompoundDeposit(id, townHall));
                }
            }
        }

        if(pAction instanceof BuildPeasantAction) {
            // check if the correct number of peasants are present
            if(peasants.size() == nextState.peasants.size()) {
                plan.removeFirst();
                busy = false;
            } else if(!busy) {
                busy = true;
                // build a peasant
                actions.put(townHall, Action.createCompoundProduction(townHall, newState.getTemplate(playernum, "Peasant").getID()));
            }
        }
        return actions;
    }

    private boolean isAdjacent(int x1, int y1, int x2, int y2) {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2) <= 2;
    }

    @Override
    public void terminalStep(State.StateView newState, History.HistoryView stateHistory) {}

    @Override
    public void savePlayerData(OutputStream out) {}

    @Override
    public void loadPlayerData(InputStream in) {}
}
