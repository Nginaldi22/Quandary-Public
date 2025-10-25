package ast;

public class Type {
    public static final int INT = 0;
    int kind;

    public Type(int kind) {
        this.kind = kind;
    }

    public int getKind() {
         return kind; 
    }
}
