package ast;


public class BlockStatement extends Stmt {
final StmtList list;
    public BlockStatement(StmtList list, Location loc){
        super(null, "block", null, loc);
        this.list = list;
    }
    public BlockStatement(Stmt s, Condition cond, Location loc){
        super(s.getExpr(), s.getType(), cond, loc);
        this.list=null;
    }
    public StmtList get_stmtlist(){
        return list;
    }
    
}
