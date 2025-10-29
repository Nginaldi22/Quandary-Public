package ast;

public class ExprList extends ASTNode{
    NeExprList rest;

    public ExprList(NeExprList list, Location loc){
        super(loc);
        this.rest=list;
    }
    public NeExprList getList(){
        return rest;
    }
}
