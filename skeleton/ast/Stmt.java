package ast;

public class Stmt extends ASTNode {

    final Expr expr;
    final String t;
    public Stmt(Expr expr,String t, Location loc) {
        super(loc);
        this.expr = expr;
        this.t=t;
    }

    public Expr getExpr() {
        return expr;
    }
    public String getType(){
        return t;
    }

}
