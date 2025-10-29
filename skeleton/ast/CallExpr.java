package ast;

public class CallExpr extends Expr {
    String funcName;
    ExprList args;

    public CallExpr(String funcName, ExprList args, Location loc) {
        super(loc);
        this.funcName = funcName;
        this.args = args;
    }

    public String getFuncName() { 
        return funcName; 
    }
    public ExprList getArgs() {
         return args; 
        }
}
