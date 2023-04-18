package pt.up.fe.comp2023.ollir;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2023.analyser.MySymbolTable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Arrays;


public class OllirGenerator extends AJmmVisitor<String, List<String>> {

    private final StringBuilder ollirCode = new StringBuilder();

    private final MySymbolTable symbolTable;

    public OllirGenerator(MySymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        this.buildVisitor();
    }

    @Override
    protected void buildVisitor() {
        //List of visitMethods:

        addVisit("Program",this::programVisit);
        addVisit("Class", this::classDeclarationVisit);
        addVisit("Method",this::methodDeclarationVisit);
        addVisit("Assign", this::assignVisit);
        addVisit("ReturnFromMethod",this::returnFromMethodVisit);
        addVisit("IntLiteral", (node, jef) -> Arrays.asList(String.format("%s.i32",node.get("var")), "i32") );
        addVisit("BoolLiteral", (node,jef) -> Arrays.asList(String.format("%s.bool",node.get("var")), "bool") );
        addVisit("Id", this::visitIdentifier);
        addVisit("Expr", this::visitExpression);
        addVisit("MethodCall", this::visitMethodCall);
        addVisit("NewObject", this::visitNewObject);
        setDefaultVisit((node,jef)-> null);
    }

    private List<String> visitNewObject(JmmNode node, String s) {

        String className = node.get("name");

        String var = node.getJmmParent().get("varName");

        ollirCode.append(String.format("%s.%s :=.%s new(%s).%s;\n",var,className,className,className,className));
        ollirCode.append(String.format("invokespecial(%s.%s, \"<init>\").V;\n",var,className));

        return null;
    }

    private List<String> visitMethodCall(JmmNode node, String s) {

        //Multiple options (static method)
        // inside each option : with or without args

        //static
        if (node.getJmmChild(0).getKind().equals("Id")){
            ollirCode.append(String.format("invokestatic(%s, \"%s\"",node.getJmmChild(0).get("name"),node.get("caller")));

            List<String> args = exprArgs(node.getJmmChild(1)); //visit arguments node

            if (!args.isEmpty()){
                for (var arg : args){
                    ollirCode.append(String.format(", %s",arg));
                }
            }

            ollirCode.append(").V;");
        }

        return null;
    }

    private List<String> exprArgs(JmmNode node){

        List<String> list = new ArrayList<>();

        for(var child : node.getChildren()){
            list.add(visit(child).get(0));
        }

        return list;
    }

    private List<String> visitExpression(JmmNode node, String s) {

        for (var child : node.getChildren()){
            visit(child);
        }

        return null;
    }


    private List<String> programVisit(JmmNode node, String jef) {
        for (var importString : symbolTable.getImports()){
            ollirCode.append("import ").append(importString).append(";\n");
        }
        for (var child : node.getChildren()){
            visit(child);
        }
        return null;
    }

    private List<String> classDeclarationVisit(JmmNode node, String jef){
        String extend = symbolTable.getSuper() != null ? String.format("extends %s",symbolTable.getSuper()) : "";
        ollirCode.append(String.format("%s %s", symbolTable.getClassName(),extend));
        ollirCode.append("{\n");

        //private or public fields (accessType)
        List<Symbol> fields = symbolTable.getFields();
        if (fields != null) {
            for (var field : fields) {
                //remove nextline n change format in next1
                String accessType = node.getOptionalObject("accessType").equals("public") ? "public" : "";
                ollirCode.append(String.format(".field %s %s;\n", accessType , OllirUtils.convertType(field)));
            }
        }

        //constructor
        ollirCode.append(String.format("\t.construct %s().V {\n",symbolTable.getClassName()));
        ollirCode.append("\t\tinvokespecial(this, \"<init>\").V;\n" + "\t}\n");

        for (var child : node.getChildren()){
            visit(child);
        }

        ollirCode.append("}\n");
        return null;
    }

    private List<String> methodDeclarationVisit(JmmNode node, String jef){
        String methodName = node.get("methodName");
        if(methodName.equals("main")) {
            ollirCode.append(".method public static main(args.array.String).V {\n");
        }
        else {
            ollirCode.append(String.format(".method public %s(%s).%s {\n",
                    methodName,methodArgs(methodName),
                    OllirUtils.convertType(symbolTable.getReturnType(methodName))));

        }
        for (var child : node.getChildren()){
            visit(child);
        }


        ollirCode.append("}\n"); //closing bracket of method


        return null;
    }


    private List<String> returnFromMethodVisit(JmmNode node, String jef) {

        if (!node.getChildren().isEmpty()) {
            String retVal = visit(node.getJmmChild(0)).get(0);

            ollirCode.append(String.format("ret.%s %s;\n", OllirUtils.convertType(symbolTable.getReturnType(node.getJmmParent().get("methodName"))),
                retVal));
        }

        return null;
    }

    private List<String> assignVisit(JmmNode node, String s) {

        if (node.getJmmChild(0).getKind().equals("NewObject")) {return null;}

        String varName = node.get("varName");

        //String varType = fieldType(node.getJmmParent(),varName);

        List<String> nodeVals = visit(node.getJmmChild(0));
        String varType = nodeVals.get(1);
        String assignedVar = nodeVals.get(0);

        if (node.getJmmChild(0).getKind().equals("Id")){
            ollirCode.append(String.format("%s.%s :=.%s %s.%s;",varName, varType, varType,assignedVar,varType));
        }
        else if (node.getJmmChild(0).getKind().equals("NewObject")){
            visit(node.getJmmChild(0));
        }
        else {
            ollirCode.append(String.format("%s.%s :=.%s %s;",varName,varType,varType,assignedVar));
        }

        ollirCode.append("\n");

        return null;
    }

    private List<String> visitIdentifier(JmmNode node, String jef) {
        //either arg of function or created in function
        String methodName = node.getAncestor("Method").get().get("methodName");
        String type = OllirUtils.convertType(symbolTable.getReturnType(methodName));

        //arg of func
        if (node.getJmmParent().getKind().equals("ReturnFromMethod") || node.getJmmParent().getKind().equals("Arguments")){
            return Arrays.asList(String.format("%s.%s",node.get("name"),type));
        }
        //2ndOne
        return Arrays.asList(methodName,type);
    }

    //Auxfuns
    private String fieldType(JmmNode node, String varName){
        //possibly change to only 1st child
        for (var child : node.getChildren())
            if (child.getKind().equals("Field") && child.get("fieldName").equals(varName)){
                Type type = (Type) child.getObject("fieldType");
                return OllirUtils.convertType(type);
            }

        return null;
    }

    //function parameters
    private String methodArgs(String methodName){
        List<Symbol> methodsList = symbolTable.getParameters(methodName);

        StringBuilder methodArgs = new StringBuilder();

        if (!methodsList.isEmpty()) {

            Symbol firstArg = methodsList.get(0);
            methodArgs.append(OllirUtils.convertType(firstArg));
            methodsList.remove(0);

            for (var symbol : methodsList) {
                methodArgs.append(", " + OllirUtils.convertType(symbol));
            }
        }
        return methodArgs.toString();
    }


    public String getCode(){
        return ollirCode.toString();
    }
}
