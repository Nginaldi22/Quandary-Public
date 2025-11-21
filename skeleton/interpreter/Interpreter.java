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

    public static abstract class Q { }

    public static class IntValue extends Q {
        private final long value;
        public IntValue(long value) { 
            this.value = value; 
        }
        public long get() { 
            return value; 
        }
    }

    private static class Ref extends Q {
        private Q left;
        private Q right;
         
        public Ref(Q left, Q right) {
             this.left = left; this.right = right; 
        }
        
        public Q getLeft() { 
            return left; 
        }
        public Q getRight() {
             return right; 
        }
        public void setLeft(Q val){
            this.left=val;
        }
        public void setRight(Q val){
            this.right=val;
        }
        
    }
    public static class NilRef extends Q{
        private String nil;
        public NilRef(){
            this.nil="nil";
        }
        public String get_val(){
            return nil;
        }
    }

    static private Interpreter interpreter;
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
        HashMap<String, Q> env = new HashMap<>();
        FormalDeclList params = mainFunc.getParams();
        env.put(params.getList().getvar().getIdent(), new IntValue(arg)); 
        Q result= executeStmt(mainFunc.getBody(), env);
        return valueToString(result);
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
        Q executeStmt(Stmt stmt, HashMap<String, Q> env) {
        if (stmt == null) {
            return null;
        } else if (stmt instanceof StmtList) {
            StmtList sl = (StmtList) stmt;
            Q check = executeStmt(sl.getStmt(), env);
            if (check != null){
                return check;
            }
            if (sl.getrest() != null){
             return executeStmt(sl.getrest(), env);
            }
            return null;
        } else if (stmt instanceof DeclStmt) {
            DeclStmt s = (DeclStmt) stmt;
            Q val = evaluate(s.getExpr(), env);
            env.put(s.getVarName(), val);
            return null;
        } else if (stmt instanceof IfStmt) {
            IfStmt s = (IfStmt) stmt;
            boolean cond = evaluate(s.getCond(), env);
            if (cond) {
                return executeStmt(s.getThenStmt(), env);
            } else if (s.getElseStmt() != null) {
                return executeStmt(s.getElseStmt(), env);
            }
            return null;
        } else if (stmt instanceof WhileStmt) {
            WhileStmt w = (WhileStmt) stmt;
            while (evaluate(w.getCond(), env)) {
                Q result = executeStmt(w.getStmt(), env);
                if (result != null) return result;
            }
            return null;
        } else if (stmt instanceof CallStmt) {
            CallStmt cs = (CallStmt) stmt;
            evaluate(cs.getCall(), env); 
            return null;
        } else if (stmt instanceof PrintStmt) {
            PrintStmt p = (PrintStmt) stmt;
            Q v = evaluate(p.getExpr(), env);
            System.out.println(valueToString(v));
            return null;
        } else if (stmt instanceof ReturnStmt) {
            ReturnStmt r = (ReturnStmt) stmt;
            return evaluate(r.getExpr(), env);
        }
        return null;
    }
    Q evaluate(Expr expr, HashMap<String, Q> env) {
        if (expr instanceof ConstExpr) {
            Object val = ((ConstExpr) expr).getValue();
            return new IntValue((long) val);
        } else if (expr instanceof BinaryExpr) {
            BinaryExpr b = (BinaryExpr) expr;
            int op = b.getOperator();
            if (op == BinaryExpr.DOT) {
                Q leftVal = evaluate(b.getLeftExpr(), env);
                Q rightVal = evaluate(b.getRightExpr(), env);
                Ref obj = new Ref(leftVal, rightVal);
                return obj;
            } else {
                Q leftQ = evaluate(b.getLeftExpr(), env);
                Q rightQ = evaluate(b.getRightExpr(), env);
                long l = ((IntValue) leftQ).get();
                long r = ((IntValue) rightQ).get();
                switch (op) {
                    case BinaryExpr.PLUS: return new IntValue(l + r);
                    case BinaryExpr.MINUS: return new IntValue(l - r);
                    case BinaryExpr.MULT: return new IntValue(l * r);
                    default: throw new RuntimeException("Unhandled binary operator: " + op);
                }
            }
        } else if (expr instanceof UnaryMinus) {
            UnaryMinus u = (UnaryMinus) expr;
            Q ans = evaluate(u.getExpr(), env);
            return new IntValue(-((IntValue) ans).get());
        } else if (expr instanceof IdentExpr) {
            IdentExpr id = (IdentExpr) expr;
            String name = id.getVArName();
            return env.get(name);
        } else if (expr instanceof CallExpr) {
            CallExpr call = (CallExpr) expr;
            List<Expr> argExprs = new ArrayList<>();
            ExprList temp = call.getArgs();
            if (temp != null && temp.getList() != null) {
                NeExprList node = temp.getList();
                while (node != null) {
                    argExprs.add(node.getExpr());
                    node = node.getRest();
                }
            }
            String fname = call.getFuncName();
            if ("randomInt".equals(fname)) {
                Q boundQ = evaluate(argExprs.get(0), env);
                int bound = (int) ((IntValue) boundQ).get();
                return new IntValue((long) random.nextInt(bound));
            }else if("left".equals(fname)){
                Q Qval = evaluate(call.getArgs().getList().getExpr(), env);
                Ref ans = (Ref)Qval;
                return ans.getLeft();
            }else if("right".equals(fname)){
                Q Qval = evaluate(call.getArgs().getList().getExpr(), env);
                Ref ans = (Ref)Qval;
                return ans.getRight();
            }else if("isAtom".equals(fname)){
                Q Qval = evaluate(call.getArgs().getList().getExpr(), env);
                if(Qval instanceof IntValue || Qval instanceof NilRef){
                    return new IntValue(1);
                }else{
                    return new IntValue(0);
                }
            }else if("isNil".equals(fname)){
                Q Qval = evaluate(call.getArgs().getList().getExpr(), env);
                if(Qval instanceof NilRef){
                    return new IntValue(1);
                }else{
                    return new IntValue(0);
                }
            }else if("setLeft".equals(fname)){
                Q Qsource = evaluate(call.getArgs().getList().getExpr(), env);
                Ref source = (Ref) Qsource;
                Q change = evaluate(call.getArgs().getList().getRest().getExpr(), env);
                source.setLeft(change);
                return new IntValue(1);
            }else if("setRight".equals(fname)){
                Q Qsource = evaluate(call.getArgs().getList().getExpr(), env);
                Ref source = (Ref) Qsource;
                Q change = evaluate(call.getArgs().getList().getRest().getExpr(), env);
                source.setRight(change);
                return new IntValue(1);
            }
            FuncDef fd = functions.get(fname);
            List<VarDecl> params = new ArrayList<>();
            FormalDeclList temp2 = fd.getParams();
            if (temp2 != null && temp2.getList() != null) {
                NeFormalDeclList node = temp2.getList();
                while (node != null) {
                    params.add(node.getvar());
                    node = node.getRest();
                }
            }
            HashMap<String, Q> newEnv = new HashMap<>();
            for (int i = 0; i < argExprs.size(); i++) {
                Q aval = evaluate(argExprs.get(i), env);
                newEnv.put(params.get(i).getIdent(), aval);
            }

            Q result = executeStmt(fd.getBody(), newEnv);
            return result;
        } else if (expr instanceof NilExpr) {
            return new NilRef();
        } 

        throw new RuntimeException("Unhandled Expr type: " + expr.getClass().getName());
    }

    boolean evaluate(ast.Condition cond, HashMap<String, Q> env) {
        if (cond instanceof CompCond) {
            CompCond c = (CompCond) cond;
            Q leftQ = evaluate(c.getLeftExpr(), env);
            Q rightQ = evaluate(c.getRightExpr(), env);
            long left = ((IntValue) leftQ).get();
            long right = ((IntValue) rightQ).get();
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

    private String valueToString(Q v) {
        if (v instanceof IntValue){
            return Long.toString(((IntValue) v).get());
        } else if(v instanceof NilRef){
            NilRef nil = (NilRef)v;
            return nil.get_val();
        }else if (v instanceof Ref) {
            Ref obj = (Ref) v;
            return "(" + valueToString(obj.getLeft()) + " . " + valueToString(obj.getRight()) + ")";
        }
        return v.toString();
    }

    public static void fatalError(String message, int processReturnCode) {
        System.out.println(message);
        System.exit(processReturnCode);
    }
}
