package ast;

import java.io.PrintStream;

public class Program extends ASTNode {

    final StmtList stmtList;
    String name;
    public Program(StmtList stmtList, String name, Location loc) {
        super(loc);
        this.stmtList = stmtList;
        this.name = name;
    }

    public StmtList getstmtList() {
        return stmtList;
    }
    public String get_name(){
        return name;
    }
    public void println(PrintStream ps) {
        ps.println(stmtList);
    }
}
