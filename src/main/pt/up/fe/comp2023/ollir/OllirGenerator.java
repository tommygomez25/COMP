package pt.up.fe.comp2023.ollir;

import org.specs.comp.ollir.Ollir;
import org.stringtemplate.v4.ST;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2023.analyser.MySymbolTable;

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
        setDefaultVisit((node,jef)-> null);
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
        ollirCode.append(String.format("public %s %s", symbolTable.getClassName(),extend));
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
        ollirCode.append(String.format(".construct %s().V {\n",symbolTable.getClassName()));
        ollirCode.append("\tinvokespecial(this, \"<init>\").V;\n" + "}\n");

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

            for (var child : node.getChildren()){
                visit(child);
            }
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

        String varName = node.get("varName");

        //String varType = fieldType(node.getJmmParent(),varName);

        List<String> nodeVals = visit(node.getJmmChild(0));
        String varType = nodeVals.get(1);
        String assignedVar = nodeVals.get(0);

        if (node.getJmmChild(0).getKind().equals("Id")){
            ollirCode.append(String.format("%s.%s :=.%s %s.%s;",varName, varType, varType,assignedVar,varType));
        }
        else {
            ollirCode.append(String.format("%s.%s :=.%s %s;",varName,varType,varType,assignedVar));
        }

        ollirCode.append("\n");

        return null;
    }

    private List<String> visitIdentifier(JmmNode node, String jef) {
        //either arg of function or created in function
        String methodName = node.getJmmParent().getJmmParent().get("methodName");
        String type = OllirUtils.convertType(symbolTable.getReturnType(methodName));;
        //arg of func
        if (node.getJmmParent().getKind().equals("ReturnFromMethod")){
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
