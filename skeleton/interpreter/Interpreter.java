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
        HashMap<String, Long> env = new HashMap<>();
        env.put(astRoot.get_name(), arg);
        return executeStmt(astRoot.getstmtList(), env);
    }


    Object executeStmt(Stmt stmt, HashMap<String, Long> env) {
        if(stmt instanceof StmtList){
            StmtList sl = (StmtList)stmt;
            Object retVal = executeStmt(sl.getStmt(), env);
            if(retVal!=null){
                return retVal;
            }
            if(sl.getrest()!=null){
                return executeStmt(sl.getrest(),env);
            }
            return null;
        }else if (stmt instanceof DeclStmt){
            DeclStmt s = (DeclStmt)stmt;
            env.put(s.getVarName(),(long)evaluate(s.getExpr(),env));
            return null;
        }
        return evaluate(((ReturnStmt)stmt).getExpr(),env);
    }

    Object evaluate(Expr expr, HashMap<String, Long> env) {
        if (expr instanceof ConstExpr) {
            return ((ConstExpr) expr).getValue();
        } else if (expr instanceof BinaryExpr) {
            BinaryExpr binaryExpr = (BinaryExpr) expr;
            long left = (Long) evaluate(binaryExpr.getLeftExpr(), env);
            long right = (Long) evaluate(binaryExpr.getRightExpr(), env);
            switch (binaryExpr.getOperator()) {
                case BinaryExpr.PLUS: return left + right;
                case BinaryExpr.MINUS: return left - right;
                case BinaryExpr.MULT: return left * right;
                default: throw new RuntimeException("Unhandled operator");
            }
        } else if (expr instanceof UnaryMinus) {
            UnaryMinus unaryMinus = (UnaryMinus) expr;
            return -(Long) evaluate(unaryMinus.getExpr(), env);
        } else if (expr instanceof VarExpr) {
            String name = ((VarExpr) expr).get_name();
            if (!env.containsKey(name))
                throw new RuntimeException("Variable '" + name + "' not declared");
            return env.get(name);
        } else {
            throw new RuntimeException("Unhandled Expr type");
        }
    }

    public static void fatalError(String message, int processReturnCode) {
        System.out.println(message);
        System.exit(processReturnCode);
    }

   
    
}
