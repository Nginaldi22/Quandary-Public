package ast;

public class DeclStmt  extends Stmt{
    final String varName;
    final Expr expr;

    public DeclStmt(String varBame, Expr expr, Location loc){
        super(loc);
        this.varName=varBame;
        this.expr=expr;
    }
    public String getVarName(){
        return varName;
    }
    public Expr getExpr(){
        return expr;
    }


}
