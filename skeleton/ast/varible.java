package ast;

public class varible extends Expr {
    String name;
    public varible(String name, Location loc){
        super(loc);
        this.name=name;
    }
    public String get_name(){
        return name;
    }
}
