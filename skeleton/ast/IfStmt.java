package ast;

public class IfStmt  extends Stmt{
    final Condition cond;
    final Stmt theStmt;
    final Stmt elseStmt;

    public IfStmt(Condition cond, Stmt thenStmt, Stmt elseStmt, Location loc){
        super(loc);
        this.cond=cond;
        this.theStmt= thenStmt;
        this.elseStmt=elseStmt;
    }
    public Condition getCond(){
        return cond;
    }
    public Stmt getThenStmt(){
        return theStmt;
    }
    public Stmt getElseStmt(){
        return elseStmt;
    }
    

}
