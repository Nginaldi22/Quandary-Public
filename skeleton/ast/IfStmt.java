package ast;

public class IfStmt extends Stmt{
    Block b;
    Condition c;
    public IfStmt(Block b, Condition c, Location loc){
        super(null,null,null,loc);
        this.b=b;
        this.c=c;
    }
    public Block getBlock(){
        return b;
    }
    public Condition getCondition(){
        return c;
    }
    
}
