package ast;

public class FormalDeclList extends ASTNode{
    NeFormalDeclList rest;

    public FormalDeclList(NeFormalDeclList list, Location loc){
        super(loc);
        this.rest=list;
    }
    public NeFormalDeclList getList(){
        return rest;
    }
}
