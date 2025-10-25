package ast;

public class VarDecl {
    Type type;
    String ident;
    Location loc;

    public VarDecl(Type type, String ident, Location loc) {
        this.type = type;
        this.ident = ident;
        this.loc = loc;
    }

    public String getIdent() { 
        return ident; 
    }
    public Type getType() {
         return type; 
    }
}
