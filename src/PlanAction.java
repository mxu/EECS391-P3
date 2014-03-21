interface PlanAction {

    public boolean isAllowedFor(PlanState s);

    public PlanState applyTo(PlanState s);

    public int makeSpan();

}
