package ast;

import java.io.PrintStream;

public class Program extends ASTNode {

    final Stmt stmt;

    public Program(Stmt stmt, Location loc) {
        super(loc);
        this.stmt = stmt;
    }

    public Stmt getstmt() {
        return stmt;
    }

    public void println(PrintStream ps) {
        ps.println(stmt);
    }
}
