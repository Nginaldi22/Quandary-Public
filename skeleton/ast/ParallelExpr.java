package ast;

public class ParallelExpr extends Expr {
    BinaryExpr B_expr;
    public ParallelExpr(Location loc, BinaryExpr B_expr){
        super(loc);
        this.B_expr= B_expr;
    }
    public BinaryExpr getB_Expr(){
        return B_expr;
    }
}
