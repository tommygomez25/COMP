package pt.up.fe.comp2023.ollir;

import org.specs.comp.ollir.Ollir;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import java.util.List;

public class OllirUtils {

    private OllirUtils(){};

    public static String convertType(Symbol symbol){
        return String.format("%s.%s",symbol.getName(),convertType(symbol.getType()));
    }

    public static String convertType(Type type){
       return String.format("%s%s",(type.isArray() ? "array." : ""),toOllir(type.getName()));
    }

    public static String toOllir(String type){
        switch (type) {
            case "int":
                return "i32";
            case "boolean":
                return "bool";
            case "void":
                return "V";
            default:
                return type;
        }
    }

}
