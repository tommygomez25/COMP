package pt.up.fe.comp2023;

import org.specs.comp.ollir.*;
import pt.up.fe.specs.util.exceptions.NotImplementedException;

public class JasminUtils {
    public static String getJasminType(Type type, ClassUnit context) {
        StringBuilder jasminCode = new StringBuilder();
        ElementType elemType = type.getTypeOfElement();
        if (type.getTypeOfElement() == ElementType.ARRAYREF){
            jasminCode.append("[");
            elemType = ((ArrayType) type).getArrayType();
        }
        switch (elemType) {
            case INT32 -> jasminCode.append("I");
            case BOOLEAN -> jasminCode.append("Z");
            case STRING -> jasminCode.append("Ljava/lang/String;");
            case OBJECTREF -> {
                assert type instanceof ClassType;
                String className = ((ClassType) type).getName();
                jasminCode.append("L").append(getQualifiedName(context, className)).append(";");
            }
            case VOID -> jasminCode.append("V");
            default -> throw new NotImplementedException("Type" + elemType +  "not implemented.");
        }

        return jasminCode.toString();
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

    public static int getValue(Element element, String sign){
        return Integer.parseInt(sign + ((LiteralElement) element).getLiteral());
    }
}


