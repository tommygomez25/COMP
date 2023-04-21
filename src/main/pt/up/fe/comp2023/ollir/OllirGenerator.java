package pt.up.fe.comp2023.ollir;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2023.analyser.MySymbolTable;

import java.util.*;


public class OllirGenerator extends AJmmVisitor<String, List<String>> {

    private final StringBuilder ollirCode = new StringBuilder();

    private final MySymbolTable symbolTable;

    private int tempVarNum = 0;

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
        addVisit("BoolLiteral", (node,jef) -> Arrays.asList(String.format("%s.bool",(node.get("var").equals("true") ? "1": "0")), "bool") );
        addVisit("Id", this::visitIdentifier);
        addVisit("Expr", this::visitExpression);
        addVisit("MethodCall", this::visitMethodCall);
        addVisit("NewObject", this::visitNewObject);
        addVisit("BinaryOp", this::visitBinaryOp);
        setDefaultVisit((node,jef)-> null);
    }

    private List<String> visitBinaryOp(JmmNode node, String s) {

        List<String> lhsObj = visit(node.getJmmChild(0)); //if ID -> name of var
        List<String> rhsObj = visit(node.getJmmChild(1));

        //check lhs and rhs : can be ID's, Int/BoolLiteral or another BinaryOp (or even (Parenthesis))
        StringBuilder lhs = new StringBuilder();
        StringBuilder rhs = new StringBuilder();

        if ((!lhsObj.get(0).contains("."))) {
            lhs.append(lhsObj.get(0) + "." + lhsObj.get(1));
        } else {
            lhs.append(lhsObj.get(0));
        }
        if ((!rhsObj.get(0).contains("."))) {
            rhs.append(rhsObj.get(0) + "." + rhsObj.get(1));
        } else {
            rhs.append(rhsObj.get(0));
        }


        String type = "";

        String op = node.get("op");
        String tempVar = newTempVar();

        switch (op){
            case "+":
            case "-":
            case "*":
            case "/":
                ollirCode.append(String.format("%s.i32 :=.i32 %s %s.i32 %s;\n",tempVar,lhs.toString(),op,rhs.toString()));
                type = "i32";


        }

        return Arrays.asList(tempVar,type);
    }

    private List<String> visitNewObject(JmmNode node, String s) {

        String className = node.get("name");

        String var = node.getJmmParent().get("varName");

        ollirCode.append(String.format("%s.%s :=.%s new(%s).%s;\n",var,className,className,className,className));
        ollirCode.append(String.format("invokespecial(%s.%s, \"<init>\").V;\n",var,className));

        return null;
    }

    //check if identifier = class or var
    private boolean checkIfClass(String varName){
        for (var jef : symbolTable.getImports()){
            if (jef.equals(varName)){
                return true;
            }
        }
        if (varName.equals(symbolTable.getClassName())) {return true;}
        return false;
    }

    private List<String> visitMethodCall(JmmNode node, String s) {

        //Multiple options (static method)

        String parentMethod = node.getAncestor("Method").get().get("methodName");

        List<String> args = exprArgs(node.getJmmChild(1)); //visit arguments node
        String varName = node.getJmmChild(0).get("name"); //s.i32 -> s || io (class)
        String varType = "";
        String invokeType = "";

        if(!checkIfClass(varName)) {
            varType = "." + methodsVariablesType(parentMethod, varName);
            invokeType = "virtual";
        } else {
            invokeType = "static";
        }
        String method = node.get("caller");

        //static method
        if (node.getJmmParent().getKind().equals("Expr")){
            ollirCode.append(String.format("invoke%s(%s%s, \"%s\"",invokeType,varName,varType,method));

            if (!args.isEmpty()){
                for (var arg : args){
                    ollirCode.append(String.format(", %s",arg));
                }
            }
            ollirCode.append(").V;\n");
        }
        //instance method (assign)
        if (node.getJmmParent().getKind().equals("Assign")){

            String methodVarType = methodsVariablesType(parentMethod,varName);
            StringBuilder invokevirtualbody = new StringBuilder();

            invokevirtualbody.append(String.format("invokevirtual(%s.%s, \"%s\"",varName,methodVarType,method));
            if (!args.isEmpty()){
                for (var arg : args){
                    invokevirtualbody.append(String.format(", %s",arg));
                }
            }
            invokevirtualbody.append(")");
            return Arrays.asList(String.format(invokevirtualbody.toString()));

        }

        return null;
    }

    //called method list of args
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

        ollirCode.append("}\n"); //closing bracket of class
        return null;
    }

    private List<String> methodDeclarationVisit(JmmNode node, String jef){
        String methodName = node.get("methodName");
        tempVarNum = 0;
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

        String returnType = OllirUtils.convertType(symbolTable.getReturnType(node.getJmmParent().get("methodName")));
        if (!node.getChildren().isEmpty()) {
            String retVal = visit(node.getJmmChild(0)).get(0);
            if (node.getJmmChild(0).getKind().equals("BinaryOp")){
                ollirCode.append(String.format("ret.%s %s.%s;\n", returnType,
                        retVal,returnType));
            } else {
                ollirCode.append(String.format("ret.%s %s;\n", returnType, retVal));
            }
        }

        return null;
    }

    private List<String> assignVisit(JmmNode node, String s) {


        String childNodeKind = node.getJmmChild(0).getKind();
        //a = b wrong lmao
        if (childNodeKind.equals("NewObject")) {
            visit(node.getJmmChild(0));
            return null;
        }

        String varName = node.get("varName");

        List<String> nodeVals = visit(node.getJmmChild(0));
        String varType = methodsVariablesType(node.getAncestor("Method").get().get("methodName"),varName);
        String assignedVar = nodeVals.get(0);

        if (childNodeKind.equals("Id") || childNodeKind.equals("MethodCall") || childNodeKind.equals("BinaryOp")){
            ollirCode.append(String.format("%s.%s :=.%s %s.%s;",varName, varType, varType,assignedVar,varType));
        }
        else if (childNodeKind.equals("NewObject")){
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
        String varName = node.get("name");
        String type = methodsVariablesType(methodName,varName);

        //arg of func
        if (node.getJmmParent().getKind().equals("ReturnFromMethod") || node.getJmmParent().getKind().equals("Arguments")){
            return Arrays.asList(String.format("%s.%s",varName,type));
        }
        //2ndOne
        return Arrays.asList(varName,type);
    }

    //Auxfuns

    private String methodsVariablesType(String methodName,String varName){

        String type = "";
        List<Symbol> vars = symbolTable.getLocalVariables(methodName); //local vars
        vars.addAll(symbolTable.getParameters(methodName)); //parameters vars

        for (Symbol variable : vars){
            if (variable.getName().equals(varName)){
                type = OllirUtils.convertType(variable.getType());
                break;
            }
        }

        return type;
    }

    //function parameters
    private String methodArgs(String methodName){
        List<Symbol> methodsList = symbolTable.getParameters(methodName);

        StringBuilder methodArgs = new StringBuilder();

        if (!methodsList.isEmpty()) {

            Symbol firstArg = methodsList.get(0);
            methodArgs.append(OllirUtils.convertType(firstArg));

            for (var symbol : methodsList.subList(1,methodsList.size())) {
                methodArgs.append(", " + OllirUtils.convertType(symbol));
            }
        }
        return methodArgs.toString();
    }

    private String newTempVar(){
        return String.format("t%d",this.tempVarNum++);
    }

    public String getCode(){
        return ollirCode.toString();
    }
}
