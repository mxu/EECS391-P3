import edu.cwru.sepia.environment.model.state.ResourceNode;
import edu.cwru.sepia.environment.model.state.Unit;

public class PlanPeasant extends PlanActor {

    PlanActor nextTo;
    ResourceNode.Type cargo;

    // constructor for adding a peasant
    public PlanPeasant(Unit.UnitView unit) {
        super(unit);
    }

    // constructor for a newly built peasant
    // no way to get new unit ID, so just clone for now
    public PlanPeasant(PlanActor other) {
        super(other);
        this.nextTo = other;
    }

    public PlanActor getNextTo() {
        return nextTo;
    }

    public void setNextTo(PlanActor nextTo) {
        this.nextTo = nextTo;
    }

    public ResourceNode.Type getCargo() {
        return cargo;
    }

    public void setCargo(ResourceNode.Type cargo) {
        this.cargo = cargo;
    }

    @Override
    public String toString() {
        String str = super.toString();
        if(nextTo != null) {
            str += ">";
            if(nextTo instanceof PlanResource) {
                str += ((PlanResource) nextTo).getType().equals(ResourceNode.Type.GOLD_MINE) ? "G" : "W";
            } else {
                str += "T";
            }
        }
        if(cargo != null) {
            str += "+" + (cargo.equals(ResourceNode.Type.GOLD_MINE) ? "G" : "W");
        }
        return str;
    }
}
