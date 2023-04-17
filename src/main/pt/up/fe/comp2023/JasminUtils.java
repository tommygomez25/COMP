package pt.up.fe.comp2023;

import org.specs.comp.ollir.ArrayType;
import org.specs.comp.ollir.ClassType;
import org.specs.comp.ollir.ElementType;
import org.specs.comp.ollir.Type;
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
}


