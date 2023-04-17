package pt.up.fe.comp2023;

import org.specs.comp.ollir.*;
import pt.up.fe.specs.util.exceptions.NotImplementedException;

public class JasminUtils {
    public static String getJasminType(Type type){
        if(type instanceof ArrayType)
            return "[" + getJasminType(((ArrayType)type).getElementType());
        if(type instanceof ClassType)
            return "L" + ((ClassType) type).getName() + ";";
        return getJasminType(type.getTypeOfElement());
    }
    public static String getJasminType(ElementType type){
        return switch (type) {
            case INT32 -> "I";
            case STRING -> "Ljava/lang/String;";
            case VOID -> "V";
            case BOOLEAN -> "Z";
            default -> throw new NotImplementedException("Type " + type + " not implemented");
        };
    }

    public static String getQualifiedName(ClassUnit classUnit, String className){
        for(String importName : classUnit.getImports()){
            var split = importName.split("\\.");
            String last;
            if(split.length >= 1)
                last = split[split.length - 1];
            else
                last = importName;
            if(last.equals(className)){
                return importName.replace(".", "/");
            }
        }
        return classUnit.getClassName().replace(".", "/");
    }
}


