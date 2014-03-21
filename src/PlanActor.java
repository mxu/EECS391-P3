import edu.cwru.sepia.environment.model.state.ResourceNode;
import edu.cwru.sepia.environment.model.state.Unit;
import edu.cwru.sepia.util.Pair;

public class PlanActor {

    // @TODO: remove unit id from
    private int id;
    private Pair location;

    // constructor for adding a resource node
    public PlanActor(ResourceNode.ResourceView node) {
        this.id = node.getID();
        this.setLocation(node.getXPosition(), node.getYPosition());
    }

    // constructor for adding a unit
    public PlanActor(Unit.UnitView unit) {
        this.id = unit.getID();
        this.setLocation(unit.getXPosition(), unit.getYPosition());
    }

    // constructor for cloning another PlanActor
    public PlanActor(PlanActor other) {
        this.id = other.id;
        this.setLocation(other.getLocation());
    }

    public int getId() { return id; }

    public Pair getLocation() { return location; }

    public void setLocation(Pair location) { this.location = location; }

    public void setLocation(int x, int y) { this.location = new Pair(x, y); }

    @Override
    public String toString() { return id + "@(" + location.a + "," + location.b + ")"; }
}
