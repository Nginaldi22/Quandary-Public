package ast;

public class IfStmt extends Stmt{
    BlockStatement b;
    Condition c;
    public IfStmt(BlockStatement b, Condition c, Location loc){
        super(null,null,null,loc);
        this.b=b;
        this.c=c;
    }
    
}
