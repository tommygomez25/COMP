package pt.up.fe.comp2023.analyser;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp2023.analyser.MySymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.Arrays;
import java.util.List;

import static pt.up.fe.comp2023.analyser.AnalysisUtils.getSymbol;

public class AnalysisUtils {
    static final List<String> PRIMITIVES = Arrays.asList("int", "void", "boolean");
    static final List<String> ARITHMETIC_OP = Arrays.asList("+", "-", "*", "/");
    static final List<String> COMPARISON_OP = List.of("<");
    static final List<String> LOGICAL_OP = List.of("&&");
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

    public static Symbol getSymbol(JmmNode node, MySymbolTable symbolTable) {
        String kind = node.getKind();
        switch (kind) {
            case "Parenthesis", "ArrayAccess", "ArrayLength", "MethodCall", "NewIntArray", "Not", "BinaryOp", "BooleanOp" -> {
                return getSymbol(node.getChildren().get(0), symbolTable);
            }
            case "IntLiteral","BoolLiteral" -> {
                Type type = new Type("int", false);
                return new Symbol(type,node.get("var"));
            }
            case "NewObject" -> {
                Type type = new Type(node.get("name"), false);
                return new Symbol(type,node.get("name"));
            }
            case "Id" -> {
                String varName = node.get("name");
                return symbolTable.findField(varName);
            }
            case "This" -> {
                Type type = new Type(symbolTable.getClassName(), false);
                return new Symbol(type,symbolTable.getClassName());
            }
            default -> throw new RuntimeException("Unknown kind: " + kind);
        }
    }

    public static boolean typeIsCompatibleWith(Type type1, Type type2, MySymbolTable symbolTable) {
        if (type1.equals(type2)) return true;
        if (type1.isArray() != type2.isArray()) return false;
        if (PRIMITIVES.contains(type1.getName()) || PRIMITIVES.contains(type2.getName())) return false;
        if (type2.getName().equals(symbolTable.getClassName()) && symbolTable.getSuper() == null) return false; // if type2 is the current class and there is no super class
        if (!type2.getName().equals(symbolTable.getClassName())
                && (type1.getName().equals(symbolTable.getClassName()) || type1.getName().equals(symbolTable.getSuper()))) return false; // if type2 is not the current class and there is no super class
        if (symbolTable.getSuper() == null ) return true;
        return !(type1.getName().equals(symbolTable.getClassName()) && symbolTable.getSuper().equals(type2.getName())); // checks if type1 extends type2
    }

}
