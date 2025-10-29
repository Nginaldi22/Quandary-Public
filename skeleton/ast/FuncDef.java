package ast;

public class FuncDef {
    VarDecl header;
    FormalDeclList params;
    StmtList body;
    Location loc;

    public FuncDef(VarDecl header, FormalDeclList params, StmtList body, Location loc) {
        this.header = header;
        this.params = params;
        this.body = body;
        this.loc = loc;
    }

    public String getName() {
     return header.getIdent();
    }
    
    public FormalDeclList getParams() { 
        return params; 
    }
    public StmtList getBody() { 
        return body; 
    }
}
