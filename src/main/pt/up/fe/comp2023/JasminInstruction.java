package pt.up.fe.comp2023;

import org.specs.comp.ollir.OperationType;

import java.util.ArrayList;

public class JasminInstruction {

    public static String instructionRegister(String inst, int reg) {
        if(reg >= 0 && reg <= 3){
            return "\t" + inst + "_" + reg + "\n";
        } else {
            return "\t" + inst + " " + reg + "\n";
        }
    }

    public static String instIinc(int register, String value) {
        return "\tiinc " + register + " " + value + "\n";
    }

    public static String instAload(int register) {
        return instructionRegister("aload", register);
    }

    public static String instAstore(int register) {
        return instructionRegister("astore", register);
    }

    public static String instIload(int register) {
        return instructionRegister("iload", register);
    }

    public static String instIstore(int register) {
        return instructionRegister("istore", register);
    }

    public static String instIconst(int register) {
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

    public static String instArraylength() { return "\tarraylength \n";}

    public static String instNewArray() { return "\tnewarray int\n"; }

    public static String instNew(String type) { return "\tnew " + type + "\n"; }

    public static String instDup() { return "\tdup\n"; }

    public static String instInvokestatic(String className, String methodName, String retType, String paramTypes) {
        return "\tinvokestatic " + className + "/" + methodName + "(" + paramTypes + ")" + retType + "\n";
    }

    public static String instInvokevirtual(String className, String methodName, String retType, String paramTypes) {
        return "\tinvokevirtual " + className + "/" + methodName + "(" + paramTypes + ")" + retType + "\n";
    }

    public static String instInvokespecial(String className, String methodName, String retType, String paramTypes) {
        return "\tinvokespecial " + className + "/" + methodName + "(" + paramTypes + ")" + retType + "\n";
    }

    public static String instGetfield(String className, String fieldName, String type) {
        return "\tgetfield " + className + "/" + fieldName + " " + type + "\n";
    }

    public static String instPutfield(String className, String fieldName, String type) {
        return "\tputfield " + className + "/" + fieldName + " " + type + "\n";
    }

    public static String instArithOp(OperationType op){
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

    public static String instPop() { return "\tpop\n"; }

}
