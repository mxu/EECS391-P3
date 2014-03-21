public class MoveAction implements PlanAction {

    int numPeasants;
    String target; // T, G, W

    public MoveAction(int numPeasants, String target) {
        this.numPeasants = numPeasants;
        this.target = target;
    }

    @Override
    public boolean isAllowedFor(PlanState s) {
        if(target.equals("T"))
            return s.atRes().size() == numPeasants;
        else
            return s.atTownHall().size() == numPeasants;
    }

    @Override
    public PlanState applyTo(PlanState s) {
        PlanState newState = new PlanState(s);

        return newState;
    }

    @Override
    public int makeSpan() {
        return 1;
    }
}
