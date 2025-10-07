package ast;

public class Condition extends ASTNode{
    public static final int LESS_EQUALS = 1;
    public static final int GREATER_EQUALS = 2;
    public static final int  ABS_EQUALS=3;
    public static final int NOT_EQUALS = 4;
    public static final int GREATER = 5;
    public static final int LESS =6;
    public static final int AND= 7;
    public static final int OR = 8;

    Expr exp1;
    Expr exp2;
    Condition cond1;
    Condition cond2;
    int type;
    public Condition(Expr exp1,Expr exp2, Condition cond1, Condition cond2,int type, Location loc){
        super(loc);
        this.exp1=exp1;
        this.exp2=exp2;
        this.cond1=cond1;
        this.cond2=cond2;
        this.type=type;
    }
    public Expr getfirstExpr(){
        return exp1;
    }
     public Expr getsecondExpr(){
        return exp2;
    }
     public Condition getfirstCondition(){
        return cond1;
    }
    public Condition getsecondCondition(){
        return cond2;
    }
    public int getType(){
        return type;
    }
    
    }
