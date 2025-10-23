package ast;

public class CompCond extends Condition {
    public static final int GE = 1;
    public static final int LE = 2;
    public static final int GT=3;
    public static final int LT = 4;
    public static final int EQ = 5;
    public static final int NE =6;
    
    final Expr expr1;
    final int operator;
    final Expr expr2;

    public CompCond(Expr expr1, int op, Expr expr2, Location loc){
        super(loc);
        this.expr1=expr1;
        this.operator=op;
        this.expr2=expr2;
    }

    public Expr getLeftExpr(){
        return expr1;
    }
    public Expr getRightExpr(){
        return expr2;
    }
    public int getOp(){
        return operator;
    }
}
