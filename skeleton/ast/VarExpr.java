package ast;

public class VarExpr extends Expr {
    public String name;

    public VarExpr(String name, Location loc) {
        super(loc);
        this.name = name;
    }
    public String get_name(){
        return name;
    }
}
