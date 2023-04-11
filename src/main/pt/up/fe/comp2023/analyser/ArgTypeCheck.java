package pt.up.fe.comp2023.analyser;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp.jmm.analysis.table.Type;

import java.util.ArrayList;
import java.util.List;

public class ArgTypeCheck extends PreorderJmmVisitor<Integer,Integer> {

    private final MySymbolTable symbolTable;
    private final List<Report> reports;

    public ArgTypeCheck(MySymbolTable symbolTable, List<Report> reports) {
        this.symbolTable = symbolTable;
        this.reports = reports;
        buildVisitor();
        setDefaultVisit((node, arg) -> 0);
    }

    @Override
    protected void buildVisitor() {
        addVisit("MethodCall", this::visitMethod);
    }

    public Integer visitMethod(JmmNode node, Integer ret) {
        String methodName = node.get("caller");
        List<Symbol> methodParameters = symbolTable.getParameters(methodName);
        List<JmmNode> methodArgs = new ArrayList<>();
        for (JmmNode child : node.getChildren()) {
            if (child.getKind().equals("Arguments")) {
                methodArgs = child.getChildren();
            }
        }

        if (methodParameters == null && methodArgs.size() != 0) {
            if (symbolTable.getImports().isEmpty()) {
                reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt("-1"), "Method " + methodName + " should have " + methodParameters.size() + " arguments"));
                return 0;
            }
            return 1;
        }

        else if (methodParameters == null && methodArgs.size() == 0) {
            return 1;
        }



        for (int i = 0; i < methodParameters.size(); i++) {
            String methodParameterType = methodParameters.get(i).getType().getName();

            boolean isMathExpression = symbolTable.isMathExpression(methodArgs.get(i).getKind());
            boolean isBooleanExpression = symbolTable.isBooleanExpression(methodArgs.get(i).getKind());

            String argumentType;
            if (methodArgs.get(i).getKind().equals("This")) {
                argumentType = symbolTable.getClassName();
            }
            else {
                if (isMathExpression || methodArgs.get(i).getKind().equals("IntLiteral")
                        || methodArgs.get(i).getKind().equals("ArrayAccess") || methodArgs.get(i).getKind().equals("ArrayLength")) {
                    argumentType = "int";
                }
                else if (isBooleanExpression || methodArgs.get(i).getKind().equals("BoolLiteral") || methodArgs.get(i).getKind().equals("Not")) {
                    argumentType = "boolean";
                }
                else if (methodArgs.get(i).getKind().equals("Id")) {
                    String id = methodArgs.get(i).get("name");
                    Symbol idSymbol = symbolTable.findField(id);
                    if (idSymbol == null) {
                        reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt("-1"), "Method " + methodName + " should have " + methodParameterType + " as argument"));
                        return 0;
                    }
                    argumentType = idSymbol.getType().getName();
                }
                else if (methodArgs.get(i).getKind().equals("NewIntArray")) {
                    argumentType = "int[]";
                }
                else if (methodArgs.get(i).getKind().equals("NewObject")) {
                    argumentType = methodArgs.get(i).get("name");
                }
                else {
                    String a = methodArgs.get(i).get("caller");
                    var returnType = symbolTable.getReturnType(a);
                    if( returnType == null){
                        if (symbolTable.getImports().isEmpty()) {
                            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt("-1"), "Method " + methodName + " should have " + methodParameterType + " as argument"));
                            return 0;
                        }
                        else {
                            return 1;
                        }
                    }
                    argumentType = returnType.getName();
                }
            }
            if (!methodParameterType.equals(argumentType)) {
                reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt("-1"), "Method " + methodName + " should have " + methodParameterType + " as argument"));
                return 0;
            }
        }
        return 0;
    }
}
