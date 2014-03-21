public class BuildPeasantAction implements PlanAction {

    @Override
    public boolean isAllowedFor(PlanState s) {
        return s.gold >= 400 && s.peasants.size() < 3;
    }

    @Override
    public PlanState applyTo(PlanState s) {
        PlanState newState = new PlanState(s);
        PlanPeasant peasant = new PlanPeasant(newState.townHall);
        newState.peasants.add(peasant);
        newState.gold -= 400;
        return null;
    }

    @Override
    public int makeSpan() {
        return 1;
    }
}
