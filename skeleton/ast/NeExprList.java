package ast;

public class NeExprList extends ExprList {
    Expr expr;
    NeExprList rest;
    public NeExprList(Expr expr, NeExprList neExprList, Location loc){
        super(neExprList,loc);
        this.expr=expr;
        this.rest=neExprList;
    }
    public Expr getExpr(){
        return expr;
    }
    public NeExprList getRest(){
        return rest;
    }
}
