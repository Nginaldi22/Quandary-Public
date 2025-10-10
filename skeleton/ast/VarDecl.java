package ast;

public class VarDecl extends Stmt {
    public String name;
    public Expr init;

    public VarDecl(String name, Expr init, Location loc) {
        super(init, null, null, loc);
        this.name = name;
        this.init = init;
    }
    public String get_name(){
        return name;
    }
}