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

    private int labels = 0;

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
        addVisit("Block", this::visitExpression);
        addVisit("MethodCall", this::visitMethodCall);
        addVisit("NewObject", this::visitNewObject);
        addVisit("BinaryOp", this::visitBinaryOp);
        addVisit("BooleanOp", this::visitBinaryOp);
        addVisit("NewIntArray", this::visitNewIntArray);
        addVisit("ArrayLength",this::visitArrayLength);
        addVisit("ArrayAssign", this::visitArrayAssign);
        addVisit("ArrayAccess", this::visitArrayAccess);
        addVisit("IfElse",this::visitIfElse);
        addVisit("While",this::visitWhile);
        addVisit("Not", this::visitNot);
        addVisit("Parenthesis",this::visitParenthesis);
        setDefaultVisit((node,jef)-> null);
    }

    private List<String> visitParenthesis(JmmNode node, String s) {

        List<String> childNode = visit(node.getJmmChild(0));
        return Arrays.asList(childNode.get(0),childNode.get(1));
    }

    // ! smth eg (!a<b)
    private List<String> visitNot(JmmNode node, String s) {

        String tempVar = newTempVar();
        List<String> childNode = visit(node.getJmmChild(0));
        String type = childNode.get(1);

        ollirCode.append(String.format("%s.%s :=.%s !.%s %s.%s;\n",tempVar,type,type,type,childNode.get(0),type));

        return Arrays.asList(tempVar,type);
    }

    // TODO: visitMethodCall : this.function of class
    /*

    */

    private List<String> visitWhile(JmmNode node, String s) {

        int labelNum = labels++;

        ollirCode.append(String.format("goto while_cond_%s;\n",labelNum));

        ollirCode.append(String.format("while_body_%s:\n",labelNum));

        visit(node.getJmmChild(1)); //while body

        ollirCode.append(String.format("while_cond_%s:\n",labelNum));

        List<String> condition = visit(node.getJmmChild(0)); // while condition

        ollirCode.append(String.format("if (%s.%s) goto while_body_%s;\n",condition.get(0),condition.get(1),labelNum));

        return null;
    }

    private List<String> visitIfElse(JmmNode node, String s) {

        int labelNum = labels++;

        List<String> condition = visit(node.getJmmChild(0)); // if condition

        ollirCode.append(String.format("if (%s.%s) goto then%s;\n", condition.get(0),condition.get(1),labelNum));

        visit(node.getJmmChild(2)); //after if block, result of else condition true;

        ollirCode.append(String.format("goto endif%s;\n",labelNum));

        ollirCode.append(String.format("then%s:\n",labelNum));

        visit(node.getJmmChild(1)); //else block

        ollirCode.append(String.format("endif%s:\n",labelNum));

        return null;
    }

    private List<String> visitBinaryOp(JmmNode node, String s) {

        List<String> lhsObj = visit(node.getJmmChild(0)); //if ID -> name of var
        List<String> rhsObj = visit(node.getJmmChild(1));

        //check lhs and rhs : can be ID's, Int/BoolLiteral or another BinaryOp (or even (Parenthesis))
        StringBuilder lhs = new StringBuilder();
        StringBuilder rhs = new StringBuilder();

        //basically this if contains was for if it was a intliteral (a.i32) so if it contains $1.a it works the same but it doesn't have type so wrong
        //ultimately it begs reformat for intliteral returning namvar.type, when it should return (namevar, type), in a list
        //above rant too bad, already fixed
        if (!(node.getJmmChild(0).getKind().endsWith("Literal"))) { //prev. condition: (!lhsObj.get(0).contains("."))
            lhs.append(lhsObj.get(0) + "." + lhsObj.get(1));
        } else {
            lhs.append(lhsObj.get(0));
        }
        if (!(node.getJmmChild(1).getKind().endsWith("Literal"))) { //prev. condition: (!rhsObj.get(0).contains("."))
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
                break;
            case "&&":
            case "<":
                ollirCode.append(String.format("%s.bool :=.bool %s %s.bool %s;\n",tempVar,lhs.toString(),op,rhs.toString()));
                type = "bool";
        }

        return Arrays.asList(tempVar,type);
    }

    private List<String> visitNewObject(JmmNode node, String var) {

        String className = node.get("name");

        //String var = node.getJmmParent().get("varName");

        String tempVar = newTempVar();

        ollirCode.append(String.format("%s.%s :=.%s new(%s).%s;\n",tempVar,className,className,className,className));
        ollirCode.append(String.format("invokespecial(%s.%s, \"<init>\").V;\n",tempVar,className));

        if (node.getJmmParent().getKind().equals("Assign")){
            ollirCode.append(String.format("%s.%s :=.%s %s.%s;\n",var,className,className,tempVar,className));
        } else {
            return Arrays.asList(tempVar,className);
        }
        //ollirCode.append(String.format(""));

        return null;
    }

    //check if identifier = class or var
    private boolean checkIfClass(String varName){
        for (var jef : symbolTable.getImports()){
            if (jef.equals(varName)){
                return true;
            }
        }
        //if (varName.equals(symbolTable.getClassName())) {return true;}
        return false;
    }

    // TODO : methodcall placement (if not just called, eg... io.println(..) but , smth = a.func()), temp var needed
    // TODO : THIS.METHOD CLASS , ARGS ETC... return arrays as list for others

    private List<String> visitMethodCall(JmmNode node, String s) {

        //Multiple options (static method)

        String parentMethod = node.getAncestor("Method").get().get("methodName");

        List<String> args = exprArgs(node.getJmmChild(1)); //visit arguments node

        String varName = ""; // node.getJmmChild(0).get("name"); //s.i32 -> s || io (class)
        String varType = "";
        String invokeType = "";
        String method = node.get("caller");

        // if a this.method() call

        if(node.getJmmChild(0).getKind().equals("This")){
            varName = "this" + "." + symbolTable.getClassName();
            invokeType = "virtual";
            varType = OllirUtils.convertType(symbolTable.getReturnType(method));

            //if its called 'alone' : this.method //or assigned/as arg of smth else : j = this.method
            if(node.getJmmParent().getKind().equals("Expr")){
                ollirCode.append(String.format("invoke%s(%s, \"%s\"",invokeType,varName,method));
                if (!args.isEmpty()){
                    for (var arg : args){
                        ollirCode.append(String.format(", %s",arg));
                    }
                }
                ollirCode.append(String.format(").%s;\n",varType));
                return null; //no need to return list
            }
            //or assigned/as arg of smth else : j = this.method //tempvar = invoke...
            else {

            String tempVar = newTempVar();

            ollirCode.append(String.format("%s.%s :=.%s invoke%s(%s, \"%s\"",tempVar,varType,varType,invokeType,varName,method));
            if (!args.isEmpty()){
                for (var arg : args){
                    ollirCode.append(String.format(", %s",arg));
                }
            }
            ollirCode.append(String.format(").%s;\n",varType));
            return Arrays.asList(tempVar,varType);
            }
        }
        else{
            varName = node.getJmmChild(0).get("name");     // s.i32 -> s || io (class)
        }
        // TODO : AFTER ALLRIGHT CHECK THIS:
        // can be newObject
        // check why needed "." first in varType
        if(!checkIfClass(varName)) {
            if (node.getJmmChild(0).getKind().equals("NewObject")){
                List<String> newObj = visit(node.getJmmChild(0));
                varName = newObj.get(0);
                varType = "." + newObj.get(1);
            }else {
                varType = "." + methodsVariablesType(parentMethod, varName);
            }
            invokeType = "virtual";
        } else {
            invokeType = "static";
        }

        // TODO (functionality: done; needed: refactoring): maybe no need of two separate ifs like this
        //  - Check if called directly, basically if its child of method(/expr) or if it's argument of something else
        //  - in order to then attribute it to a tempVar or not
        //  - structure to check if static or not already in place
        //static method (maybe different condition than Expr)
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
        //instance method (assign) (maybe needs change methodsVariablesType)
        else { //if (node.getJmmParent().getKind().equals("Assign")) , previous condition

            // TODO if checkIfClass check is without ".", remove wherever there's substring here

            String tempVar = newTempVar();

            //String methodVarType = methodsVariablesType(parentMethod,varName); // pointless varType defined above exactly like this
            StringBuilder invokevirtualbody = new StringBuilder();

            invokevirtualbody.append(String.format("invokevirtual(%s.%s, \"%s\"",varName,varType.substring(1),method));
            if (!args.isEmpty()){
                for (var arg : args){
                    invokevirtualbody.append(String.format(", %s",arg));
                }
            }
            invokevirtualbody.append(")");
            String returnType = "";
            if ((!varType.isEmpty()) && varType.substring(1).equals(symbolTable.getClassName())){returnType = OllirUtils.convertType(symbolTable.getReturnType(method));} else{
                returnType= "V";}

            ollirCode.append(String.format("%s.%s :=.%s %s.%s;\n",tempVar,returnType,returnType,invokevirtualbody.toString(),returnType));

            return Arrays.asList(tempVar,returnType);

        }

        return null;
    }

    //called method list of args
    private List<String> exprArgs(JmmNode node){

        List<String> list = new ArrayList<>();

        for(var child : node.getChildren()){
            if (child.getKind().endsWith("Literal") || child.getKind().equals("Id")){
                list.add(visit(child).get(0));
            } else {
                List<String> args = visit(child);
                list.add(args.get(0)+ "." + args.get(1));
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

    private List<String> visitArrayAccess(JmmNode node, String s) {

        String tempVar = newTempVar();

        List<String> arrayName = visit(node.getJmmChild(0)); // a[0] : a, i32

        List<String> arrayIndex = visit(node.getJmmChild(1).getJmmChild(0)); //firstchild : Accessors, child of that is the actual thing

        if(node.getJmmChild(1).getJmmChild(0).getKind().endsWith("Literal")){
            ollirCode.append(String.format("%s.i32 :=.i32 %s[%s].i32;\n",tempVar,arrayName.get(0),arrayIndex.get(0)));
        } else {
            ollirCode.append(String.format("%s.i32 :=.i32 %s[%s.%s].i32;\n",tempVar,arrayName.get(0),arrayIndex.get(0),arrayIndex.get(1)));
        }

        return Arrays.asList(tempVar,"i32"); //it's always int
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

        //refactoring IntLiteral here aswell xd
        // previous condition: childNodeKind.equals("Id") || childNodeKind.equals("MethodCall") || childNodeKind.equals("BinaryOp") || childNodeKind.equals("BooleanOp")  || childNodeKind.equals("ArrayLength") || childNodeKind.equals("ArrayAccess")
        // inverted body of if and else previously
        if (childNodeKind.endsWith("Literal")) {
            if (!varScope.equals("field")){
                ollirCode.append(String.format("%s.%s :=.%s %s;",varName,varType,varType,assignedVar));
            } else {
                ollirCode.append(String.format("putfield(this, %s.%s, %s).V;",varName,varType,assignedVar));
            }
        }
        else {
            if (!varScope.equals("field")){
                ollirCode.append(String.format("%s.%s :=.%s %s.%s;", varName, varType, varType, assignedVar, varType));
            } else {
                ollirCode.append(String.format("putfield(this, %s.%s, %s.%s).V;",varName,varType,assignedVar,varType));
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
