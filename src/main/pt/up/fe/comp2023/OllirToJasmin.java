package pt.up.fe.comp2023;

import org.specs.comp.ollir.ClassUnit;

public class OllirToJasmin {

    private final ClassUnit classUnit;

    public OllirToJasmin(ClassUnit classUnit) {
        this.classUnit = classUnit;
        this.classUnit.buildVarTables();
    }

    public String createJasmin(){
        StringBuilder jasminCode = new StringBuilder();

        jasminCode.append(".class public ").append(classUnit.getClassName()).append("\n");

        String superClass = classUnit.getSuperClass();
        if(superClass != null){
            jasminCode.append(".super ").append(superClass).append("\n\n");
        }
        else{
            jasminCode.append(".super java/lang/Object \n\n");
        }

        return jasminCode.toString();
    }
}
