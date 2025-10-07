package ast;

import java.io.PrintStream;

public class Program extends ASTNode {

    final StmtList stmtList;

    public Program(StmtList stmtList, Location loc) {
        super(loc);
        this.stmtList = stmtList;
    }

    public StmtList getstmtList() {
        return stmtList;
    }

    public void println(PrintStream ps) {
        ps.println(stmtList);
    }
}
