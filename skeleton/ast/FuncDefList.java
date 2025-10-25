package ast;

public class FuncDefList extends ASTNode {
    private final FuncDef funcDef;
    private final FuncDefList rest;

    public FuncDefList(FuncDef funcDef, FuncDefList rest, Location loc) {
        super(loc);
        this.funcDef = funcDef;
        this.rest = rest;
    }

    public FuncDef getFuncDef() {
        return funcDef;
    }

    public FuncDefList getRest() {
        return rest;
    }
}
