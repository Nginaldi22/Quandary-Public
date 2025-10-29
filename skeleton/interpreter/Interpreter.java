package interpreter;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    HashMap<String, FuncDef> functions = new HashMap<>();
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

    private void initMemoryManager(String gcType, long heapBytes) {
        if (gcType.equals("Explicit")) {
            throw new RuntimeException("Explicit not implemented");
        } else if (gcType.equals("MarkSweep")) {
            throw new RuntimeException("MarkSweep not implemented");
        } else if (gcType.equals("RefCount")) {
            throw new RuntimeException("RefCount not implemented");
        } else if (gcType.equals("NoGC")) {
            // nothing
        }
    }

    Object executeRoot(Program astRoot, long arg) {
        loadFunctionsFromProgram(astRoot);
        FuncDef mainFunc = functions.get("main");
        HashMap<String, Long> env = new HashMap<>();
        List<VarDecl> params = mainFunc.getParams();
        env.put(params.get(0).getIdent(), arg); 
        return executeStmt(mainFunc.getBody(), env);
    }

    private void loadFunctionsFromProgram(Program program) {
        FuncDefList fl = program.getFuncDefList();
        while (fl != null) {
            FuncDef f = fl.getFuncDef();
            String name = f.getName();
            functions.put(name, f);
            fl = fl.getRest();
        }
    }

    Object executeStmt(Stmt stmt, HashMap<String, Long> env) {
        if (stmt == null){

         return null;
        }
       else if (stmt instanceof StmtList) {
            StmtList sl = (StmtList) stmt;
            Object check = executeStmt(sl.getStmt(), env);
            if (check != null){
                return check;
            }
            if (sl.getrest() != null){
             return executeStmt(sl.getrest(), env);
            }
            return null;
        }else if (stmt instanceof DeclStmt) {
            DeclStmt s = (DeclStmt) stmt;
            long val = ((long) evaluate(s.getExpr(), env));
            env.put(s.getVarName(), val);
            return null;
        }else if (stmt instanceof IfStmt) {
            IfStmt s = (IfStmt) stmt;
            boolean cond = evaluate(s.getCond(), env);
            if (cond) {
                return executeStmt(s.getThenStmt(), new HashMap<>(env));
            } else if (s.getElseStmt() != null) {
                return executeStmt(s.getElseStmt(), new HashMap<>(env));
            }
            return null;
        }else if (stmt instanceof PrintStmt) {
            PrintStmt p = (PrintStmt) stmt;
            Object v = evaluate(p.getExpr(), env);
            System.out.println(v);
            return null;
        }else if (stmt instanceof ReturnStmt) {
            ReturnStmt r = (ReturnStmt) stmt;
            return evaluate(r.getExpr(), env);
        }
        return null;
    }

    Object evaluate(Expr expr, HashMap<String, Long> env) {
        if (expr instanceof ConstExpr) {
            Object type= ((ConstExpr) expr).getValue();
            return (long)type;
        }else if (expr instanceof BinaryExpr) {
            BinaryExpr b = (BinaryExpr) expr;
            long left = ((long) evaluate(b.getLeftExpr(), env));
            long right = ((long) evaluate(b.getRightExpr(), env));
            int op = b.getOperator();
            switch (op) {
                case BinaryExpr.PLUS:  return left + right;
                case BinaryExpr.MINUS: return left - right;
                case BinaryExpr.MULT:  return left * right;
                default: throw new RuntimeException("Unhandled binary operator: " + op);
            }
        }else if (expr instanceof UnaryMinus) {
            UnaryMinus u = (UnaryMinus) expr;
            return -((long) evaluate(u.getExpr(), env));
        }else if (expr instanceof IdentExpr) {
            IdentExpr id = (IdentExpr) expr;
            String name = id.getVArName();
            if (!env.containsKey(name))
                throw new RuntimeException("Undefined variable: " + name);
            return env.get(name);
        }else if (expr instanceof CallExpr) {
            CallExpr call = (CallExpr) expr;
            String fname = call.getFuncName();
            if ("randomInt".equals(fname)) {
                long bound = (long) evaluate(call.getArgs().get(0), env);
                return (long) random.nextInt((int) bound);
            }
            FuncDef fd = functions.get(fname);
            List<Expr> args = call.getArgs();
            List<VarDecl> params = fd.getParams();
            if (args == null) args = new ArrayList<>();
            if (params == null) params = new ArrayList<>();
            HashMap<String, Long> newEnv = new HashMap<>();
            for (int i = 0; i < args.size(); i++) {
                Object aval = evaluate(args.get(i), env);
                long avalLong = ((Number) aval).longValue();
                newEnv.put(params.get(i).getIdent(), avalLong);
            }
            return (long)executeStmt(fd.getBody(), newEnv);

        }

        throw new RuntimeException("Unhandled Expr type: " + expr.getClass().getName());
    }
    boolean evaluate(ast.Condition cond, HashMap<String, Long> env) {
        if (cond instanceof CompCond) {
            CompCond c = (CompCond) cond;
            long left = ((long) evaluate(c.getLeftExpr(), env));
            long right = ((long) evaluate(c.getRightExpr(), env));
            switch (c.getOp()) {
                case CompCond.EQ: return left == right;
                case CompCond.GT: return left > right;
                case CompCond.GE: return left >= right;
                case CompCond.LT: return left < right;
                case CompCond.LE: return left <= right;
                case CompCond.NE: return left != right;
                default: throw new RuntimeException("Unhandled comp op: " + c.getOp());
            }
        } else if (cond instanceof LogicalCond) {
            LogicalCond l = (LogicalCond) cond;
            switch (l.getOp()) {
                case LogicalCond.OR:
                    return evaluate(l.getLeftcond(), env) || evaluate(l.getRightcond(), env);
                case LogicalCond.AND:
                    return evaluate(l.getLeftcond(), env) && evaluate(l.getRightcond(), env);
                case LogicalCond.NOT:
                    return !evaluate(l.getLeftcond(), env);
                default:
                    throw new RuntimeException("Unhandled logical");
            }
        }
        throw new RuntimeException("Unhandled Condition type: " + cond.getClass().getName());
    }

    public static void fatalError(String message, int processReturnCode) {
        System.out.println(message);
        System.exit(processReturnCode);
    }
}
