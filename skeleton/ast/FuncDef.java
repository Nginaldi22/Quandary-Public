package ast;
import java.util.*;

public class FuncDef {
    VarDecl header;
    List<VarDecl> params;
    StmtList body;
    Location loc;

    public FuncDef(VarDecl header, List<VarDecl> params, StmtList body, Location loc) {
        this.header = header;
        this.params = params;
        this.body = body;
        this.loc = loc;
    }

    public String getName() {
     return header.getIdent();
    }
    
    public List<VarDecl> getParams() { 
        return params; 
    }
    public StmtList getBody() { 
        return body; 
    }
}
