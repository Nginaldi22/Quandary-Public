package ast;

public class IdentExpr extends Expr {
    final String varName;
    public IdentExpr(String varname, Location loc){
        super(loc);
        this.varName=varname;
    }
    public String getVArName(){
        return varName;
    }
}
