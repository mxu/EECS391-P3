import edu.cwru.sepia.environment.model.state.ResourceNode;

import java.util.ArrayList;
import java.util.List;

public class PlanState {

    public int gold;
    public int wood;
    public PlanActor townHall;
    public List<PlanPeasant> peasants;
    public List<PlanResource> mines;
    public List<PlanResource> trees;

    // constructor for creating a new PlanState
    public PlanState(int gold, int wood) {
        this.gold = gold;
        this.wood = wood;
        peasants = new ArrayList<PlanPeasant>();
        mines = new ArrayList<PlanResource>();
        trees = new ArrayList<PlanResource>();
    }

    // constructor for cloning another PlanState
    public PlanState(PlanState other) {
        this(other.gold, other.wood);
        this.townHall = new PlanActor(other.townHall);
        for(PlanPeasant peasant: other.peasants)
            this.peasants.add(new PlanPeasant(peasant));
        this.mines = other.mines;
        this.trees = other.trees;
    }

    public List<PlanPeasant> atTownHall() {
        ArrayList<PlanPeasant> result = new ArrayList<PlanPeasant>();
        for(PlanPeasant peasant: peasants)
            if(peasant.getNextTo().equals(townHall)) result.add(peasant);
        return result;
    }

    public List<PlanPeasant> atMines() {
        ArrayList<PlanPeasant> result = new ArrayList<PlanPeasant>();
        for(PlanPeasant peasant: peasants) {
            PlanActor nextTo = peasant.getNextTo();
            if(nextTo instanceof PlanResource &&
               ((PlanResource) nextTo).getType().equals(ResourceNode.Type.GOLD_MINE))
                result.add(peasant);
        }
        return result;
    }

    public List<PlanPeasant> atTrees() {
        ArrayList<PlanPeasant> result = new ArrayList<PlanPeasant>();
        for(PlanPeasant peasant: peasants) {
            PlanActor nextTo = peasant.getNextTo();
            if(nextTo instanceof PlanResource &&
               ((PlanResource) nextTo).getType().equals(ResourceNode.Type.TREE))
                result.add(peasant);
        }
        return result;
    }

    public List<PlanPeasant> atRes() {

    }

    @Override
    public String toString() {
        String str = "G:" + gold + ", W:" + wood;
        if(townHall != null)
            str += " T:" + townHall;
        if(peasants.size() > 0)
            str += " P:" + peasants;
        if(mines.size() > 0)
            str += " M:" + mines;
        if(trees.size() > 0)
            str += " T:" + trees;
        return str;
    }

}
