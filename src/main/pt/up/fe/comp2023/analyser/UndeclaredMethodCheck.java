package pt.up.fe.comp2023.analyser;

import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2023.analyser.MySymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UndeclaredMethodCheck extends PreorderJmmVisitor<Integer, Integer> {

    private final MySymbolTable symbolTable;
    private final List<Report> reports;
    public UndeclaredMethodCheck(MySymbolTable symbolTable, List<Report> reports) {
        this.symbolTable = symbolTable;
        this.reports = reports;
        buildVisitor();
    }

    @Override
    protected void buildVisitor() {
        addVisit("MethodCall", this::visitMethodCall);
        setDefaultVisit((node, symbolTable) -> 0);
    }

    private Integer visitMethodCall(JmmNode jmmNode, Integer ret) {

        String methodName = jmmNode.get("caller");
        String superClass = symbolTable.getSuper();
        Type returnType = symbolTable.getReturnType(methodName);
        Type classType = AnalysisUtils.getTypeOperand(jmmNode.getJmmChild(0), symbolTable);
        String className = classType.getName();

        if (className.equals(symbolTable.getClassName())) {
            if (symbolTable.getMethods().contains(methodName)) {
                List<Symbol> methodParams = symbolTable.getParameters(methodName);
                int methodParamsSize = 0;
                if (methodParams != null) methodParamsSize = methodParams.size();
                JmmNode argumentsNode = jmmNode.getJmmChild(1);
                if (methodParamsSize != argumentsNode.getNumChildren()) {
                    reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), "Method " + methodName + " has " + methodParams.size() + " parameters"));
                } else {
                    for (int i = 0; i < argumentsNode.getNumChildren(); i++) {
                        Type paramType = methodParams.get(i).getType();
                        Type argType = AnalysisUtils.getType(argumentsNode.getJmmChild(i), symbolTable);
                        if (!paramType.equals(argType)) {
                            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), "Method " + methodName + " has " + methodParams.size() + " parameters"));
                        }
                    }
                }
            }
            else if (!(superClass != null && symbolTable.isClassImported(superClass))) {
                reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), "Method " + methodName + " is not declared"));
            }
        }

        else {
            if (!symbolTable.isClassImported(className)) {
                reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), "Method " + methodName + " is not declared"));
            }
        }


            // vou ver À symbol table se a variável é uma classe
            // se for uma classe, vou ver se é a class que estou a ver
            // se for a classe que estou a ver, verifico se ela tem o método que foi chamado
            // se nao for a classe que estou a ver, verifico se ela é a super classe e se essa super classe está importada
            // ou se nao for super classe, verifico se a classe está importada
            // se a classe não estiver importada, reporto erro


        return 1;
    }
}
