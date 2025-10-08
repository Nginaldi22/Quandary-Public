package ast;

public class Block {
    Block b;
    Stmt s;
    boolean single= false;
    public Block(Stmt s, Block b){
        this.s=s;
        this.b=b;
        single =false;
    }
    public Block(String s){
        this.s=null;
        this.b=null;
    }
    public Block(Stmt s){
        this.s=s;
        single = true;
    }
    public Stmt get_stmt(){
        return s;
    }
    public Block get_next_Block(){
        return b;
    }
    public boolean get_single(){
        return single;
    }

}
