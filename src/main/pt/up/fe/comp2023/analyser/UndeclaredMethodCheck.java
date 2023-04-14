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
        addVisit("MethodCall", this::visitMethod);
        setDefaultVisit((node, symbolTable) -> 0);
    }

    private Integer visitMethod(JmmNode jmmNode, Integer ret) {
        // iterate over children and if kind is MethodCall then check if it is declared

        String methodName = jmmNode.get("caller");
        String superClass = symbolTable.getSuper();
        Type returnType = symbolTable.getReturnType(methodName);
        if (returnType == null && symbolTable.getSuper() != null || returnType == null && !symbolTable.getImports().isEmpty()) {
            return 1;
        }
        else if (returnType == null && jmmNode.getJmmChild(0).getKind().equals("This")) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), "Method " + methodName + " is not declared"));
            return 0;
        }
        else if (returnType == null && symbolTable.getSuper() == null && symbolTable.getImports().isEmpty()) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), "Method " + methodName + " is not declared"));
            return 0;
        }

        /*
        if (symbolTable.getMethods().contains(methodName)) {
            return 1;
        }

        Type left = AnalysisUtils.getType(jmmNode.getChildren().get(0), symbolTable);
        if (symbolTable.isVarClass(left.getName())) {
            if (left.getName().equals(symbolTable.getClassName())) {
                if (!symbolTable.getMethods().contains(methodName)) {
                    reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), "Method " + methodName + " is not declared"));
                }
            } else if (left.getName().equals(superClass)) {
                if (!symbolTable.isClassImported(superClass)) {
                    reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), "Method " + methodName + " is not declared"));
                }
            } else {
                if (!symbolTable.isClassImported(left.getName())) {
                    reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), "Method " + methodName + " is not declared"));
                }
            }
        }
*/
            // vou ver À symbol table se a variável é uma classe
            // se for uma classe, vou ver se é a class que estou a ver
            // se for a classe que estou a ver, verifico se ela tem o método que foi chamado
            // se nao for a classe que estou a ver, verifico se ela é a super classe e se essa super classe está importada
            // ou se nao for super classe, verifico se a classe está importada
            // se a classe não estiver importada, reporto erro


        return 1;
    }
}
