package pt.up.fe.comp2023.analyser;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.ArrayList;
import java.util.List;

public class MySymbolTableVisitor extends AJmmVisitor<MySymbolTable, List<Report>> {
    private String currentScope;

    public MySymbolTableVisitor(){
        buildVisitor();
    }
    @Override
    protected void buildVisitor() {
        addVisit("Program", this::dealWithProgram);
        addVisit("Import", this::dealWithImport);
        addVisit("Class", this::dealWithClass);
        addVisit("Method", this::dealWithMethod);
    }

    private void visitAndReduce(JmmNode jmmNode, MySymbolTable symbolTable, List<Report> reports) {
        reports.addAll(visit(jmmNode, symbolTable));
    }
    private List<Report> dealWithProgram(JmmNode jmmNode, MySymbolTable symbolTable) {
        List<Report> reports = new ArrayList<>();

        for (JmmNode child : jmmNode.getChildren()) {
            visitAndReduce(child, symbolTable, reports);
            //visit(child, symbolTable);
        }
        return reports;
    }

    private List<Report> dealWithImport(JmmNode jmmNode, MySymbolTable symbolTable) {
        List<Report> reports = new ArrayList<>();

        List<String> importValues = (List<String>) jmmNode.getObject("names");

        StringBuilder finalString = new StringBuilder();

        if(importValues != null) {
            finalString.append(importValues.get(0));
            for (int i = 1; i < importValues.size(); i++) {
                finalString.append(".").append(importValues.get(i));
            }
        }
        symbolTable.addImport(finalString.toString());
        return reports;
    }

    private List<Report> dealWithClass(JmmNode jmmNode, MySymbolTable symbolTable) {

        List<Report> reports = new ArrayList<>();

        String className = jmmNode.get("className");
        String superClassName = null;
        if(jmmNode.getAttributes().contains("extendName")){
            superClassName = jmmNode.get("extendName");
        }
        symbolTable.addClass(className, superClassName);
        currentScope = "CLASS";

        for(JmmNode child: jmmNode.getChildren()){
            if(child.getKind().equals("Field")){
                String fieldName = child.get("fieldName");
                boolean isArray = child.getChildren().get(0).get("isArray").equals("true");
                String t = child.getChildren().get(0).get("typeName");
                Type type = new Type(t, isArray);
                if (symbolTable.containsFieldInClass(fieldName,className))
                    reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt("-1"),Integer.parseInt("-1"),
                            "Field " + fieldName + " already exists in class " + className));
                else
                    symbolTable.addField(new Symbol(type, fieldName));

            }
            else {
                visitAndReduce(child, symbolTable, reports);
            }
        }
        return reports;
    }

    private List<Report> dealWithMethod(JmmNode jmmNode, MySymbolTable symbolTable) {
        List<Report> reports = new ArrayList<>();
        currentScope = "METHOD";
        String methodName = jmmNode.get("methodName");

        var parameters = new ArrayList<Symbol>();
        var returnTypes = new ArrayList<Type>();
        var localVariables = new ArrayList<Symbol>();

        if (!methodName.equals("main")) {

            boolean retIsArray = jmmNode.getChildren().get(0).get("isArray").equals("true");
            String retType = jmmNode.getChildren().get(0).get("typeName");
            returnTypes.add(new Type(retType, retIsArray));

            List<String> methodParameters = (List<String>) jmmNode.getObject("parameters");
            if (methodParameters.size() > 0) {
                for (int i = 1; i < jmmNode.getChildren().size(); i++) {

                    if (!jmmNode.getChildren().get(i).getKind().equals("Type")) {
                        continue;
                    }

                    String type = jmmNode.getChildren().get(i).get("typeName");

                    boolean isArray = jmmNode.getChildren().get(i).get("isArray").equals("true");
                    var fieldName = methodParameters.get(i - 1);

                    Type typeObject = new Type(type, isArray);

                    if (parameters.stream().anyMatch(symbol -> symbol.getName().equals(fieldName)))
                        reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt("-1"),Integer.parseInt("-1"),
                                "Field " + methodParameters.get(i-1) + " already exists in method " + methodName));
                    else
                        parameters.add(new Symbol(typeObject, methodParameters.get(i - 1)));

                }
            }

            for (JmmNode child: jmmNode.getChildren()) {

                if (child.getKind().equals("Field")) {
                    String varName = child.get("fieldName");
                    boolean isArray = child.getChildren().get(0).get("isArray").equals("true");
                    String type = child.getChildren().get(0).get("typeName");
                    Type typeObject = new Type(type, isArray);
                    Symbol symbol = new Symbol(typeObject, varName);

                    if (localVariables.stream().anyMatch(s -> s.getName().equals(varName)))
                        reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt("-1"),Integer.parseInt("-1"),
                                "Variable " + varName + " already exists in method " + methodName));
                    else
                        localVariables.add(symbol);
                }

            }

            symbolTable.addMethod(methodName, returnTypes.get(0), parameters,localVariables);
        }
        else {
            currentScope = "MAIN";
            String methodParameters = jmmNode.get("args");
            parameters.add(new Symbol(new Type("String", true), methodParameters));
            returnTypes.add(new Type("void", false));

            for (JmmNode child: jmmNode.getChildren()) {
                for (JmmNode grandChild: child.getChildren()) {
                    if (grandChild.getKind().equals("VarDeclaration")) {
                        String varName = grandChild.get("fieldName");
                        boolean isArray = grandChild.getChildren().get(0).get("isArray").equals("true");
                        String type = grandChild.getChildren().get(0).get("typeName");
                        Type typeObject = new Type(type, isArray);
                        Symbol symbol = new Symbol(typeObject, varName);
                        if (symbolTable.containsSymbolInMethod(varName, methodName))
                            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt("-1"),Integer.parseInt("-1"),
                                    "Variable " + varName + " already exists in method " + methodName));
                        else
                            localVariables.add(symbol);
                    }
                }
            }
            if (symbolTable.containsMethod(methodName, returnTypes.get(0), parameters))
                reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt("-1"),Integer.parseInt("-1"),
                        "Method " + methodName + " already exists"));
            else {
                symbolTable.addMethod(methodName, returnTypes.get(0), parameters,localVariables);
            }

        }

        return reports;
    }

}
