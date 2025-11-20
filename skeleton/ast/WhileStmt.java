package ast;

public class WhileStmt extends Stmt{
    final Condition cond;
    final Stmt stmt;

    public WhileStmt(Condition cond, Stmt thenStmt, Location loc){
        super(loc);
        this.cond=cond;
        this.stmt= thenStmt;
        
    }
    public Condition getCond(){
        return cond;
    }
    public Stmt getStmt(){
        return stmt;
    }
    

}
