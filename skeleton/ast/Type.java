package ast;

public class Type {
    public static final int INT = 0;
    public static final int REF = 1;
    int kind;

    public Type(int kind) {
        this.kind = kind;
    }

    public int getKind() {
         return kind; 
    }
}
