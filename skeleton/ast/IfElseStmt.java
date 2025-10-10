package ast;

public class IfElseStmt extends Stmt{
    Block b;
    Condition c;
    Block b2;
    public IfElseStmt(Block b, Condition c, Block b2, Location loc){
        super(null,null,null,loc);
        this.b=b;
        this.c=c;
        this.b2=b2;
    }
    public Block getBlock(){
        return b;
    }
    public Condition getCondition(){
        return c;
    }
    public Block getBlockTwo(){
        return this.b2;
    }
    
}
