package pt.up.fe.comp2023;

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
}
