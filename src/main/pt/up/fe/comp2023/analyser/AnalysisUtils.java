package pt.up.fe.comp2023.analyser;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp2023.analyser.MySymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;

public class AnalysisUtils {

    public static Type getType(JmmNode node, MySymbolTable symbolTable) {

        String kind = node.getKind();
        switch (kind) {
            case "Parenthesis", "ArrayAccess" -> {
                return getType(node.getChildren().get(0), symbolTable);
            }
            case "ArrayLength", "BinaryOp", "IntLiteral" -> {
                return new Type("int", false);
            }
            case "Not", "BoolLiteral", "BooleanOp" -> {
                return new Type("boolean", false);
            }
            case "NewIntArray" -> {
                return new Type("int", true);
            }
            case "NewObject" -> {
                return new Type(node.get("name"), false);
            }
            case "MethodCall" -> {
                String methodName = node.get("caller");
                return (symbolTable.getReturnType(methodName));
            }
            case "Id" -> {
                String varName = node.get("name");
                Symbol varSymbol = symbolTable.findField(varName);
                return varSymbol.getType();
            }
            case "This" -> {
                return new Type(symbolTable.getClassName(), false);
            }
            default -> throw new RuntimeException("Unknown kind: " + kind);
        }

    }

}
