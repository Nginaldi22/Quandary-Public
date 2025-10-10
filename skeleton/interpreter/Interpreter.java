package interpreter;

import java.io.*;
import java.util.HashMap;
import java.util.Random;

import parser.ParserWrapper;
import ast.*;

public class Interpreter {

    // Process return codes
    public static final int EXIT_SUCCESS = 0;
    public static final int EXIT_PARSING_ERROR = 1;
    public static final int EXIT_STATIC_CHECKING_ERROR = 2;
    public static final int EXIT_DYNAMIC_TYPE_ERROR = 3;
    public static final int EXIT_NIL_REF_ERROR = 4;
    public static final int EXIT_QUANDARY_HEAP_OUT_OF_MEMORY_ERROR = 5;
    public static final int EXIT_DATA_RACE_ERROR = 6;
    public static final int EXIT_NONDETERMINISM_ERROR = 7;

    static private Interpreter interpreter;
    HashMap<String, Long> env = new HashMap<>();
    public static Interpreter getInterpreter() {
        return interpreter;
    }

    public static void main(String[] args) {
        String gcType = "NoGC"; // default for skeleton, which only supports NoGC
        long heapBytes = 1 << 14;
        int i = 0;
        String filename;
        long quandaryArg;
        try {
            for (; i < args.length; i++) {
                String arg = args[i];
                if (arg.startsWith("-")) {
                    if (arg.equals("-gc")) {
                        gcType = args[i + 1];
                        i++;
                    } else if (arg.equals("-heapsize")) {
                        heapBytes = Long.valueOf(args[i + 1]);
                        i++;
                    } else {
                        throw new RuntimeException("Unexpected option " + arg);
                    }
                } else {
                    if (i != args.length - 2) {
                        throw new RuntimeException("Unexpected number of arguments");
                    }
                    break;
                }
            }
            filename = args[i];
            quandaryArg = Long.valueOf(args[i + 1]);
        } catch (Exception ex) {
            System.out.println("Expected format: quandary [OPTIONS] QUANDARY_PROGRAM_FILE INTEGER_ARGUMENT");
            System.out.println("Options:");
            System.out.println("  -gc (MarkSweep|Explicit|NoGC)");
            System.out.println("  -heapsize BYTES");
            System.out.println("BYTES must be a multiple of the word size (8)");
            return;
        }

        Program astRoot = null;
        Reader reader;
        try {
            reader = new BufferedReader(new FileReader(filename));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        try {
            astRoot = ParserWrapper.parse(reader);
        } catch (Exception ex) {
            ex.printStackTrace();
            Interpreter.fatalError("Uncaught parsing error: " + ex, Interpreter.EXIT_PARSING_ERROR);
        }
       // astRoot.println(System.out);
        interpreter = new Interpreter(astRoot);
        interpreter.initMemoryManager(gcType, heapBytes);
        String returnValueAsString = interpreter.executeRoot(astRoot, quandaryArg).toString();
        System.out.println("Interpreter returned " + returnValueAsString);
    }

    final Program astRoot;
    final Random random;

    private Interpreter(Program astRoot) {
        this.astRoot = astRoot;
        this.random = new Random();
    }

    void initMemoryManager(String gcType, long heapBytes) {
        if (gcType.equals("Explicit")) {
            throw new RuntimeException("Explicit not implemented");            
        } else if (gcType.equals("MarkSweep")) {
            throw new RuntimeException("MarkSweep not implemented");            
        } else if (gcType.equals("RefCount")) {
            throw new RuntimeException("RefCount not implemented");            
        } else if (gcType.equals("NoGC")) {
            // Nothing to do
        }
    }

    Object executeRoot(Program astRoot, long arg) {
        env.put(astRoot.get_name(),arg);
        return executeStmtList(astRoot.getstmtList());
    }
    Object executeStmtList(StmtList stmtList){
        Object check = executeStmt(stmtList.getStmt());
        if(check!=null){
            return check;
        }
       return  executeStmtList(stmtList.getrest());
    }
    
    Object executeBlock(Block b) {
    if (b == null || b.get_stmt() == null) {
        return null; 
    }
    Object check = executeStmt(b.get_stmt());
    if (check != null) {
        return check;
    }
    return executeBlock(b.get_next_Block());
}
    Object executeStmt(Stmt stmt){
        if(stmt instanceof IfStmt){
            return executeIfStmt((IfStmt)stmt);
        }else if (stmt instanceof IfElseStmt){
            return executeIfElseStmt((IfElseStmt)stmt);
        }else if (stmt instanceof VarDecl) {
        VarDecl varible = (VarDecl) stmt;
        if(stmt.getExpr()==null){
            env.put(varible.get_name(),null);
        }else{
            long value = (Long)evaluate(varible.getExpr());
        env.put(varible.get_name(), value);
        }
        return null;
        } 
        else if(stmt.getType()=="p"){
            System.out.println(evaluate(stmt.getExpr()));
            return null;
        }
        return evaluate(stmt.getExpr());
    }

    Object evaluate(Expr expr) {
        if (expr instanceof ConstExpr) {
            return ((ConstExpr)expr).getValue();
        } else if (expr instanceof BinaryExpr) {
            BinaryExpr binaryExpr = (BinaryExpr)expr;
            switch (binaryExpr.getOperator()) {
                case BinaryExpr.PLUS: return (Long)evaluate(binaryExpr.getLeftExpr()) + (Long)evaluate(binaryExpr.getRightExpr());
                case BinaryExpr.MINUS: return (Long)evaluate(binaryExpr.getLeftExpr()) - (Long)evaluate(binaryExpr.getRightExpr());
                case BinaryExpr.MULT: return (Long)evaluate(binaryExpr.getLeftExpr()) * (Long)evaluate(binaryExpr.getRightExpr());
                default: throw new RuntimeException("Unhandled operator");
            }
        } else if (expr instanceof UnaryMinus){
            UnaryMinus unaryMinus= (UnaryMinus)expr;
            return -(Long)evaluate(unaryMinus.getExpr());
        }else if (expr instanceof VarExpr) {
            String name = ((VarExpr)expr).get_name();
            if (!env.containsKey(name)) {
            throw new RuntimeException("var is already declared");
            }
            return env.get(name);
        }
        else {
            throw new RuntimeException("Unhandled Expr type");
        }
    }

	public static void fatalError(String message, int processReturnCode) {
        System.out.println(message);
        System.exit(processReturnCode);
	}
//////////////////////////////////////////////////////////////////////////
/// ////////////////////////////////////////////////////////////////////////
/// /////////////////////////////////////////////////////////////////
    Object executeIfElseStmt(IfElseStmt stmt){
    Block use =  stmt.getBlock();
    Block use_else = stmt.getBlockTwo();
    Condition c = stmt.getCondition();
            switch(c.getType()){
                case Condition.LESS_EQUALS:
                if((long)evaluate(c.getfirstExpr())<=(long)evaluate(c.getsecondExpr())){
                    if(use.get_single()){
                        return executeStmt(use.get_stmt());
                    }else{
                        Object check =executeStmt(use.get_stmt());
                        if(check != null){
                            return check;
                        }else{
                            check =executeBlock(use.get_next_Block());
                            return check;
                        }
                    }
                    //end of first
                }else{
                    Object check =executeStmt(use_else.get_stmt());
                        if(check != null){
                            return check;
                        }else{
                            check =executeBlock(use_else.get_next_Block());
                            return check;
                        }
                }
                case Condition.LESS:
                if((long)evaluate(c.getfirstExpr())<(long)evaluate(c.getsecondExpr())){
                    if(use.get_single()){
                        return executeStmt(use.get_stmt());
                    }else{
                        Object check =executeStmt(use.get_stmt());
                        if(check != null){
                            return check;
                        }else{
                            check =executeBlock(use.get_next_Block());
                            return check;
                        }
                    }
                    //end of Second
                }else{
                    Object check =executeStmt(use_else.get_stmt());
                        if(check != null){
                            return check;
                        }else{
                            check =executeBlock(use_else.get_next_Block());
                            return check;
                        }
                }
                case Condition.GREATER_EQUALS:
                if((long)evaluate(c.getfirstExpr())>=(long)evaluate(c.getsecondExpr())){
                    if(use.get_single()){
                        return executeStmt(use.get_stmt());
                    }else{
                        Object check =executeStmt(use.get_stmt());
                        if(check != null){
                            return check;
                        }else{
                            check =executeBlock(use.get_next_Block());
                            return check;
                        }
                    }
                }else{
                     Object check =executeStmt(use_else.get_stmt());
                        if(check != null){
                            return check;
                        }else{
                            check =executeBlock(use_else.get_next_Block());
                            return check;
                        }
                }
                case Condition.GREATER:
                if((long)evaluate(c.getfirstExpr())>(long)evaluate(c.getsecondExpr())){
                    if(use.get_single()){
                        return executeStmt(use.get_stmt());
                    }else{
                        Object check =executeStmt(use.get_stmt());
                        if(check != null){
                            return check;
                        }else{
                            check =executeBlock(use.get_next_Block());
                            return check;
                        }
                    }
                }else{
                     Object check =executeStmt(use_else.get_stmt());
                        if(check != null){
                            return check;
                        }else{
                            check =executeBlock(use_else.get_next_Block());
                            return check;
                        }
                }
                case Condition.ABS_EQUALS:
                if((long)evaluate(c.getfirstExpr())==(long)evaluate(c.getsecondExpr())){
                    if(use.get_single()){
                        return executeStmt(use.get_stmt());
                    }else{
                        Object check =executeStmt(use.get_stmt());
                        if(check != null){
                            return check;
                        }else{
                            check =executeBlock(use.get_next_Block());
                            return check;
                        }
                    }
                   
                }else{
                     Object check =executeStmt(use_else.get_stmt());
                        if(check != null){
                            return check;
                        }else{
                            check =executeBlock(use_else.get_next_Block());
                            return check;
                        }
                }
                case Condition.NOT_EQUALS:
                if((long)evaluate(c.getfirstExpr())!=(long)evaluate(c.getsecondExpr())){
                    if(use.get_single()){
                        return executeStmt(use.get_stmt());
                    }else{
                        Object check =executeStmt(use.get_stmt());
                        if(check != null){
                            return check;
                        }else{
                            check =executeBlock(use.get_next_Block());
                            return check;
                        }
                    }
                }else{
                     Object check =executeStmt(use_else.get_stmt());
                        if(check != null){
                            return check;
                        }else{
                            check =executeBlock(use_else.get_next_Block());
                            return check;
                        }
                }
                case Condition.AND:
                if(executeCondition(c.getfirstCondition())&& executeCondition(c.getsecondCondition())){
                    if(use.get_single()){
                        return executeStmt(use.get_stmt());
                    }else{
                        Object check =executeStmt(use.get_stmt());
                        if(check != null){
                            return check;
                        }else{
                            check =executeBlock(use.get_next_Block());
                            return check;
                        }
                    }
                }else{
                     Object check =executeStmt(use_else.get_stmt());
                        if(check != null){
                            return check;
                        }else{
                            check =executeBlock(use_else.get_next_Block());
                            return check;
                        }
                }
                
                case Condition.OR:
                if(executeCondition(c.getfirstCondition())|| executeCondition(c.getsecondCondition())){
                    if(use.get_single()){
                        return executeStmt(use.get_stmt());
                    }else{
                        Object check =executeStmt(use.get_stmt());
                        if(check != null){
                            return check;
                        }else{
                            check =executeBlock(use.get_next_Block());
                            return check;
                        }
                    }
                    
                }else{
                     Object check =executeStmt(use_else.get_stmt());
                        if(check != null){
                            return check;
                        }else{
                            check =executeBlock(use_else.get_next_Block());
                            return check;
                        }
                }
                case Condition.NOT_COND:
                if(!executeCondition(c.getfirstCondition())){
                    if(use.get_single()){
                        return executeStmt(use.get_stmt());
                    }else{
                        Object check =executeStmt(use.get_stmt());
                        if(check != null){
                            return check;
                        }else{
                            check =executeBlock(use.get_next_Block());
                            return check;
                        }
                    }
                    
                }else{
                     Object check =executeStmt(use_else.get_stmt());
                        if(check != null){
                            return check;
                        }else{
                            check =executeBlock(use_else.get_next_Block());
                            return check;
                        }
                }
                case Condition.JUST_COND:
                if(executeCondition(c.getfirstCondition())){
                    if(use.get_single()){
                        return executeStmt(use.get_stmt());
                    }else{
                        Object check =executeStmt(use.get_stmt());
                        if(check != null){
                            return check;
                        }else{
                            check =executeBlock(use.get_next_Block());
                            return check;
                        }
                    }
                    
                }else{
                     Object check =executeStmt(use_else.get_stmt());
                        if(check != null){
                            return check;
                        }else{
                            check =executeBlock(use_else.get_next_Block());
                            return check;
                        }
                }
            }
        return null;
}
////////////////////////////////////////////////////////////////////////////////////////////
/// /////////////////////////////////////////////////////////////////////////////////////////
Object executeIfStmt(IfStmt stmt){
    Block use =  stmt.getBlock();
    Condition c = stmt.getCondition();
            switch(c.getType()){
                case Condition.LESS_EQUALS:
                if((long)evaluate(c.getfirstExpr())<=(long)evaluate(c.getsecondExpr())){
                    if(use.get_single()){
                        return executeStmt(use.get_stmt());
                    }else{
                        Object check =executeStmt(use.get_stmt());
                        if(check != null){
                            return check;
                        }else{
                            check =executeBlock(use.get_next_Block());
                            return check;
                        }
                    }
                    //end of first
                }
                break;
                case Condition.LESS:
                if((long)evaluate(c.getfirstExpr())<(long)evaluate(c.getsecondExpr())){
                    if(use.get_single()){
                        return executeStmt(use.get_stmt());
                    }else{
                        Object check =executeStmt(use.get_stmt());
                        if(check != null){
                            return check;
                        }else{
                            check =executeBlock(use.get_next_Block());
                            return check;
                        }
                    }
                    //end of Second
                }
                break;
                case Condition.GREATER_EQUALS:
                if((long)evaluate(c.getfirstExpr())>=(long)evaluate(c.getsecondExpr())){
                    if(use.get_single()){
                        return executeStmt(use.get_stmt());
                    }else{
                        Object check =executeStmt(use.get_stmt());
                        if(check != null){
                            return check;
                        }else{
                            check =executeBlock(use.get_next_Block());
                            return check;
                        }
                    }
                    //end of third
                }
                break;
                case Condition.GREATER:
                if((long)evaluate(c.getfirstExpr())>(long)evaluate(c.getsecondExpr())){
                    if(use.get_single()){
                        return executeStmt(use.get_stmt());
                    }else{
                        Object check =executeStmt(use.get_stmt());
                        if(check != null){
                            return check;
                        }else{
                            check =executeBlock(use.get_next_Block());
                            return check;
                        }
                    }
                    //end of fourth
                }
                break;
                case Condition.ABS_EQUALS:
                if((long)evaluate(c.getfirstExpr())==(long)evaluate(c.getsecondExpr())){
                    if(use.get_single()){
                        return executeStmt(use.get_stmt());
                    }else{
                        Object check =executeStmt(use.get_stmt());
                        if(check != null){
                            return check;
                        }else{
                            check =executeBlock(use.get_next_Block());
                            return check;
                        }
                    }
                    //end of fith
                }
                break;
                case Condition.NOT_EQUALS:
                if((long)evaluate(c.getfirstExpr())!=(long)evaluate(c.getsecondExpr())){
                    if(use.get_single()){
                        return executeStmt(use.get_stmt());
                    }else{
                        Object check =executeStmt(use.get_stmt());
                        if(check != null){
                            return check;
                        }else{
                            check =executeBlock(use.get_next_Block());
                            return check;
                        }
                    }
                    //end of sixth
                }
                break;
                case Condition.AND:
                if(executeCondition(c.getfirstCondition())&& executeCondition(c.getsecondCondition())){
                    if(use.get_single()){
                        return executeStmt(use.get_stmt());
                    }else{
                        Object check =executeStmt(use.get_stmt());
                        if(check != null){
                            return check;
                        }else{
                            check =executeBlock(use.get_next_Block());
                            return check;
                        }
                    }
                    //end of seventh
                }
                break;
                case Condition.OR:
                if(executeCondition(c.getfirstCondition())|| executeCondition(c.getsecondCondition())){
                    if(use.get_single()){
                        return executeStmt(use.get_stmt());
                    }else{
                        Object check =executeStmt(use.get_stmt());
                        if(check != null){
                            return check;
                        }else{
                            check =executeBlock(use.get_next_Block());
                            return check;
                        }
                    }
                    //end of eigth
                }
                break;
                case Condition.NOT_COND:
                if(!executeCondition(c.getfirstCondition())){
                    if(use.get_single()){
                        return executeStmt(use.get_stmt());
                    }else{
                        Object check =executeStmt(use.get_stmt());
                        if(check != null){
                            return check;
                        }else{
                            check =executeBlock(use.get_next_Block());
                            return check;
                        }
                    }
                    //end of ninth
                }
                break;
                case Condition.JUST_COND:
                if(executeCondition(c.getfirstCondition())){
                    if(use.get_single()){
                        return executeStmt(use.get_stmt());
                    }else{
                        Object check =executeStmt(use.get_stmt());
                        if(check != null){
                            return check;
                        }else{
                            check =executeBlock(use.get_next_Block());
                            return check;
                        }
                    }
                    //end of tenth
                }
            }
        return null;
}
/////////////////////////////////////////////////////////////////////////////////////////////////////////////
/// //////////////////////////////////////////////////////////////////////////////////////////////////////////
Boolean executeCondition(Condition c){
    Boolean ans=false;
    switch(c.getType()){    
         case Condition.LESS_EQUALS:
            if((long)evaluate(c.getfirstExpr())<=(long)evaluate(c.getsecondExpr())){
                  ans = true;
                }
                 break;
                case Condition.LESS:
                if((long)evaluate(c.getfirstExpr())<(long)evaluate(c.getsecondExpr())){
                     ans = true;
                }
                break;
                case Condition.GREATER_EQUALS:
                if((long)evaluate(c.getfirstExpr())>=(long)evaluate(c.getsecondExpr())){
                    ans = true;
                }
                break;
                case Condition.GREATER:
                if((long)evaluate(c.getfirstExpr())>(long)evaluate(c.getsecondExpr())){
                    ans = true;
                }
                break;
                case Condition.ABS_EQUALS:
                if((long)evaluate(c.getfirstExpr())==(long)evaluate(c.getsecondExpr())){
                    ans = true;
                }
                break;
                case Condition.NOT_EQUALS:
                if((long)evaluate(c.getfirstExpr())!=(long)evaluate(c.getsecondExpr())){
                        ans=true;;
                }
                break;
            }
    return ans;
    }
}
