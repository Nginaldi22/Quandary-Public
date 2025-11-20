package ast;
public class CallStmt extends Stmt {
    private CallExpr call;

    public CallStmt(CallExpr call, Location loc) {
        super(loc);
        this.call = call;
    }

    public CallExpr getCall() {
        return call;
    }
}
