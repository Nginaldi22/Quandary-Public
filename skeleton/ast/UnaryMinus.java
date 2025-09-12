package ast;

public class UnaryMinus extends Expr{
    final Expr expr;
    public UnaryMinus(Expr expr, Location loc){
        super(loc);
        this.expr = expr;
    }
    public Expr getExpr(){
        return expr;
    }
}
