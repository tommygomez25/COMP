package pt.up.fe.comp2023;

import org.specs.comp.ollir.OperationType;
import java.util.ArrayList;


public class JasminInstruction {

    public static LimitController limitController = new LimitController();

    public static String instructionRegister(String inst, int reg) {
        limitController.addRegister(reg);
        if(reg >= 0 && reg <= 3){
            return "\t" + inst + "_" + reg + "\n";
        } else {
            return "\t" + inst + " " + reg + "\n";
        }
    }

    public static String instIinc(int register, int value) {
        return "\tiinc " + register + " " + value + "\n";
    }

    public static String instAload(int register) {
        limitController.updateStack(1);
        return instructionRegister("aload", register);
    }

    public static String instAstore(int register) {
        limitController.updateStack(-1);
        return instructionRegister("astore", register);
    }

    public static String instIload(int register) {
        limitController.updateStack(1);
        return instructionRegister("iload", register);
    }

    public static String instIstore(int register) {
        limitController.updateStack(-1);
        return instructionRegister("istore", register);
    }

    public static String instIaload(){
        limitController.updateStack(-1);
        return "\tiaload\n";
    }

    public static String instIastore(){
        limitController.updateStack(-3);
        return "\tiastore\n";
    }

    public static String instIconst(int register) {
        limitController.updateStack(1);
        if(register == -1){
            return "\ticonst_m1\n";
        }
        else if(register >= 0 && register <= 5){
            return "\ticonst_" + register + "\n";
        }
        else if(register >= -128 && register <= 127){
            return "\tbipush " + register + "\n";
        }
        else if(register >= -32768 && register <= 32767){
            return "\tsipush " + register + "\n";
        }
        else {
            return "\tldc " + register + "\n";
        }
    }

    public static String instIreturn() { return "\tireturn\n"; }

    public static String instAreturn() { return "\tareturn\n"; }

    public static String instNewArray() { return "\tnewarray int\n"; }

    public static String instNew(String type) {
        limitController.updateStack(1);
        return "\tnew " + type + "\n"; }

    public static String instDup() {
        limitController.updateStack(1);
        return "\tdup\n";
    }

    public static String instInvokestatic(String className, String methodName, String retType, String paramTypes, int paramNum) {
        limitController.updateStack(-paramNum);
        return "\tinvokestatic " + className + "/" + methodName + "(" + paramTypes + ")" + retType + "\n";
    }

    public static String instInvokevirtual(String className, String methodName, String retType, String paramTypes, int paramNum) {
        limitController.updateStack(-paramNum);
        return "\tinvokevirtual " + className + "/" + methodName + "(" + paramTypes + ")" + retType + "\n";
    }

    public static String instInvokespecial(String className, String methodName, String retType, String paramTypes, int paramNum) {
        limitController.updateStack(-paramNum);
        return "\tinvokespecial " + className + "/" + methodName + "(" + paramTypes + ")" + retType + "\n";
    }

    public static String instGetfield(String className, String fieldName, String type) {
        return "\tgetfield " + className + "/" + fieldName + " " + type + "\n";
    }

    public static String instPutfield(String className, String fieldName, String type) {
        limitController.updateStack(-2);
        return "\tputfield " + className + "/" + fieldName + " " + type + "\n";
    }

    public static String instArithOp(OperationType op){
        limitController.updateStack(-1);
        switch (op){
            case ADD -> {
                return "\tiadd\n";
            }
            case SUB -> {
                return "\tisub\n";
            }
            case MUL -> {
                return "\timul\n";
            }
            case DIV -> {
                return "\tidiv\n";
            }
            case ANDB -> {
                return "\tiand\n";
            }
            default -> {
                return "";
            }
        }
    }

    public static String instGoto(String label) { return "goto " + label + '\n'; }

    public static String instPop() {
        limitController.updateStack(-1);
        return "\tpop\n";
    }

    public static String ifne(String label) {
        limitController.updateStack(-1);
        return "\tifne " + label + "\n"; }

    public static String iflt(String label) {
        limitController.updateStack(-1);
        return "iflt " + label + '\n';
    }

    public static String ifge(String label) {
        limitController.updateStack(-1);
        return "ifge " + label + '\n';
    }

    public static String if_icmplt(String label) {
        limitController.updateStack(-2);
        return "if_icmplt " + label + '\n';
    }

    public static String if_icmpge(String label) {
        limitController.updateStack(-2);
        return "if_icmpge " + label + '\n';
    }

    public static String instArraylength() { return "\tarraylength\n"; }

}
