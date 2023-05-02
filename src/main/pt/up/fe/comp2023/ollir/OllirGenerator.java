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
        addVisit("NewIntArray", this::visitNewIntArray);
        addVisit("ArrayLength",this::visitArrayLength);
        addVisit("ArrayAssign", this::visitArrayAssign);
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
            String returnType = "";
            if ((!varType.isEmpty()) && varType.substring(1).equals(symbolTable.getClassName())){returnType = OllirUtils.convertType(symbolTable.getReturnType(method));} else{
                returnType= "V";}

            ollirCode.append(")." + returnType +  ";\n");
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
            if (child.getKind().equals("BinaryOp")){
                List<String> args = visit(child);
                list.add(args.get(0)+ "." + args.get(1));
            } else {
                list.add(visit(child).get(0));
            }
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
        ollirCode.append("\t\tinvokespecial(this, \"<init>\").V;\n" + "\t}\n\n");

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
        if (methodName.equals("main")){
            ollirCode.append("ret.V;\n");
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

    private List<String> visitArrayAssign(JmmNode node, String s) {

        //index and value
        String arrayName = node.get("varName");
        List<String> index = visit(node.getJmmChild(0));
        List<String> value = visit(node.getJmmChild(1));

        if (node.getJmmChild(0).getKind().equals("IntLiteral")){
            String tempVar = newTempVar();
            ollirCode.append(String.format("%s.i32 :=.i32 %s;\n",tempVar,index.get(0)));
            if (node.getJmmChild(1).getKind().equals("IntLiteral")){
                ollirCode.append(String.format("%s[%s.i32].i32 :=.i32 %s;\n",arrayName,tempVar,value.get(0)));
            } else {
                ollirCode.append(String.format("%s[%s.i32].i32 :=.i32 %s.i32;\n",arrayName,tempVar,value.get(0)));
            }
        } else {
            if (node.getJmmChild(1).getKind().equals("IntLiteral")){
                ollirCode.append(String.format("%s[%s.i32].i32 :=.i32 %s;\n", arrayName, index.get(0), value.get(0)));
            } else {
                ollirCode.append(String.format("%s[%s.i32].i32 :=.i32 %s.i32;\n", arrayName, index.get(0), value.get(0)));
            }
        }
        return null;
    }

    private List<String> visitArrayLength(JmmNode node, String s) {
        String tempVar = newTempVar();
        String arrayVar = visit(node.getJmmChild(0)).get(0);
        ollirCode.append(String.format("%s.i32 :=.i32 arraylength(%s.array.i32).i32;\n",tempVar,arrayVar));
        return Arrays.asList(tempVar,"i32");
    }

    private List<String> visitNewIntArray(JmmNode node, String varName) {

        StringBuilder length = new StringBuilder();
        List<String> lengthVar = visit(node.getJmmChild(0));
        if (node.getJmmChild(0).getKind().equals("IntLiteral")){
            String tempVar = newTempVar();
            ollirCode.append(String.format("%s.i32 :=.i32 %s;\n",tempVar,lengthVar.get(0)));
            length.append(tempVar + ".i32");
        } else{
            length.append(lengthVar.get(0) + ".i32");
        }

        ollirCode.append(String.format("%s.array.i32 :=.array.i32 new(array, %s).array.i32;\n",varName,length.toString()));

        return null;
    }
    private String varScope(String varName, String methodName){

        for (var var : symbolTable.getLocalVariables(methodName)){
            if (var.getName().equals(varName)){return "local";}
        }
        for (int i = 0; i < symbolTable.getParameters(methodName).size(); i++){
            if (symbolTable.getParameters(methodName).get(i).getName().equals(varName))
                {
                    int param = i+1;
                    return "$" + param;
                }
        }
        if (isField(varName)){
            return "field";
        }
        return "";
    }

    private List<String> assignVisit(JmmNode node, String s) {


        String childNodeKind = node.getJmmChild(0).getKind();
        //New Object / NewIntArray
        if (childNodeKind.equals("NewObject") || childNodeKind.equals("NewIntArray")) {
            visit(node.getJmmChild(0),node.get("varName"));
            return null;
        }
        String parentMethod = node.getAncestor("Method").get().get("methodName");
        StringBuilder varNameBuilder = new StringBuilder();
        String varScope = varScope(node.get("varName"),parentMethod); //local, $varname, field
        //check if assigned var is a parameter
        if (varScope.startsWith("$")){
            varNameBuilder.append(varScope + ".");
        }
        varNameBuilder.append(node.get("varName"));
        String varName = varNameBuilder.toString();

        String varType = methodsVariablesType(parentMethod,node.get("varName"));

        List<String> nodeVals = visit(node.getJmmChild(0));
        String assignedVar = nodeVals.get(0);

        if (childNodeKind.equals("Id") || childNodeKind.equals("MethodCall") || childNodeKind.equals("BinaryOp") || childNodeKind.equals("ArrayLength")) {
            if (!varScope.equals("field")){
                ollirCode.append(String.format("%s.%s :=.%s %s.%s;", varName, varType, varType, assignedVar, varType));
            } else {
                ollirCode.append(String.format("putfield(this, %s.%s, %s.%s).V;",varName,varType,assignedVar,varType));
            }
        }
        else {
            if (!varScope.equals("field")){
                ollirCode.append(String.format("%s.%s :=.%s %s;",varName,varType,varType,assignedVar));
            } else {
                ollirCode.append(String.format("putfield(this, %s.%s, %s).V;",varName,varType,assignedVar));
            }
        }

        ollirCode.append("\n");

        return null;
    }

    private List<String> visitIdentifier(JmmNode node, String jef) {
        //either arg of function or created in function
        String methodName = node.getAncestor("Method").get().get("methodName");
        String variableName = node.get("name");
        StringBuilder variableNameBuilder = new StringBuilder();
        String type = methodsVariablesType(methodName,variableName);
        String varScope = varScope(variableName,methodName);

        //if var is a field create temp var and assign varName to that temp var
        //also verify that is not local
        if (varScope.equals("field")){
            String tempVar = newTempVar();
            ollirCode.append(String.format("%s.%s :=.%s getfield(this, %s.%s).%s;\n",tempVar,type,type,variableName,type,type));
            variableNameBuilder.append(tempVar);
        } else {
            if (varScope.startsWith("$")){
                variableNameBuilder.append(varScope + ".");
            }
            variableNameBuilder.append(variableName);
        }
        String varName = variableNameBuilder.toString();

        //arg of func
        if (node.getJmmParent().getKind().equals("ReturnFromMethod") || node.getJmmParent().getKind().equals("Arguments")){
            return Arrays.asList(String.format("%s.%s",varName,type));
        }
        //2ndOne
        return Arrays.asList(varName,type);
    }

    //Auxfuns

    private boolean isField(String varName){

        for (Symbol s : symbolTable.getFields()){
            if (s.getName().equals(varName)){
                return true;
            }
        }
        return false;
    }

    private String methodsVariablesType(String methodName,String varName){

        String type = "";
        List<Symbol> vars = new ArrayList<>();
        vars.addAll(new ArrayList<>((symbolTable.getLocalVariables(methodName)))); //local vars
        vars.addAll(new ArrayList<>((symbolTable.getParameters(methodName)))); //parameters vars
        vars.addAll(new ArrayList<>((symbolTable.getFields()))); //fields vars

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
