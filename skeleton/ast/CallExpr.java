package ast;
import java.util.*;

public class CallExpr extends Expr {
    String funcName;
    List<Expr> args;

    public CallExpr(String funcName, List<Expr> args, Location loc) {
        super(loc);
        this.funcName = funcName;
        this.args = args;
    }

    public String getFuncName() { 
        return funcName; 
    }
    public List<Expr> getArgs() {
         return args; 
        }
}
