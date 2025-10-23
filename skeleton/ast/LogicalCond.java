package ast;

public class LogicalCond extends Condition {
    public static final int AND = 1;
    public static final int OR = 2;
    public static final int NOT=3;
    
    
    final Condition cond1;
    final int operator;
    final Condition cond2;

    public LogicalCond(Condition cond1, int op, Condition cond2, Location loc){
        super(loc);
        this.cond1=cond1;
        this.operator=op;
        this.cond2=cond2;
    }

    public Condition getLeftcond(){
        return cond1;
    }
    public Condition getRightcond(){
        return cond2;
    }
    public int getOp(){
        return operator;
    }
}
