package ast;

public class StmtList extends ASTNode{
    Stmt current;
    StmtList rest;
    public StmtList(Stmt stmt, StmtList rest, Location loc){
        super(loc);
        this.current = stmt;
        this.rest =rest;
    }
    public StmtList(String empty, Location loc){
    super(loc);
    this.current=null;
    this.rest=null;
    }
    public Stmt getStmt(){
        return current;
    }
     public StmtList getrest(){
        return rest;
    }
}
