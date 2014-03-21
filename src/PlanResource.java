import edu.cwru.sepia.environment.model.state.ResourceNode;

public class PlanResource {

    ResourceNode.Type type;
    int amount;

    // constructor for adding a resource node
    public PlanResource(ResourceNode.ResourceView node) {
        super(node);
        this.type = node.getType();
        this.amount = node.getAmountRemaining();
    }

    // account for a gather action on this resource
    public int gather() {
        if(this.amount >= 100) {
            this.amount -= 100;
            return 100;
        }
        return 0;
    }

    public int getAmount() { return amount; }

    public ResourceNode.Type getType() { return type; }

    @Override
    public String toString() {
        return super.toString() + ":" + amount + (type.equals(ResourceNode.Type.GOLD_MINE) ? "G" : "W");
    }
}
