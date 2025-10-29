package ast;

public class NeFormalDeclList extends FormalDeclList {
    VarDecl var;
    NeFormalDeclList rest;
    public NeFormalDeclList(VarDecl var, NeFormalDeclList neExprList, Location loc){
        super(neExprList,loc);
        this.var=var;
        this.rest=neExprList;
    }
    public VarDecl getvar(){
        return var;
    }
    public NeFormalDeclList getRest(){
        return rest;
    }
}
