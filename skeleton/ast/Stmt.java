package ast;

public class Stmt extends ASTNode {

    final Expr expr;
    final String t;
    final Condition cond;
    public Stmt(Expr expr,String t, Condition cond, Location loc) {
        super(loc);
        this.expr = expr;
        this.t=t;
        this.cond=cond;
    }

    public Expr getExpr() {
        return expr;
    }
    public String getType(){
        return t;
    }
    public Condition getCondition(){
        return cond;
    }

}
