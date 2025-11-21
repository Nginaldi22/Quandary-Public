package ast;

public class NilExpr extends Expr {
    private String ans;
    public NilExpr() {
        super(null);
        ans="nil";
    }

    @Override
    public String toString() {
        return ans;
    }
}