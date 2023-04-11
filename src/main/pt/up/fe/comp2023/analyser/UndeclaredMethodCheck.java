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

        if (symbolTable.getMethods().contains(methodName)) {
            return 1;
        }

        // check child nodes, if kind is Arguments we dont care about it
        for (JmmNode child : jmmNode.getChildren()) {
            if (child.getKind().equals("Arguments")) {
                continue;
            }
            // if kind equals id

            // vou ver À symbol table se a variável é uma classe
            // se for uma classe, vou ver se é a class que estou a ver
            // se for a classe que estou a ver, verifico se ela tem o método que foi chamado
            // se nao for a classe que estou a ver, verifico se ela é a super classe e se essa super classe está importada
            // ou se nao for super classe, verifico se a classe está importada
            // se a classe não estiver importada, reporto erro
            if (child.getKind().equals("Id")) {
                Type varType = symbolTable.getVarType(child.get("name"));
                if (varType != null) {
                    if (symbolTable.isVarClass(child.get("name"))) {
                        if (varType.getName().equals(symbolTable.getClassName())) {
                            if (!symbolTable.getMethods().contains(methodName)) {
                                if (superClass != null) {
                                    if (!symbolTable.isClassImported(superClass)) {
                                        reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), Integer.parseInt(jmmNode.get("colStart")), "Method " + methodName + " not declared because " + superClass + " is not imported"));
                                    }
                                } else {
                                    reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), Integer.parseInt(jmmNode.get("colStart")), "Method " + methodName + " not declared"));
                                }
                            }
                        }
                        else {
                            if (varType.getName().equals(superClass)) {
                                if (!symbolTable.isClassImported(superClass)) {
                                    reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), Integer.parseInt(jmmNode.get("colStart")), "Method " + methodName + " not declared because " + superClass + " is not imported"));
                                }
                            }
                            else {
                                if (!symbolTable.isClassImported(varType.getName())) {
                                    reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), Integer.parseInt(jmmNode.get("colStart")), "Method " + methodName + " not declared because " + varType.getName() + " is not imported"));
                                }
                            }
                        }

                    }
                    else {
                        // var is not class
                        reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), Integer.parseInt(jmmNode.get("colStart")), "Method " + methodName + " not declared"));

                    }
                }
            }
            else if (child.getKind().equals("This")){
                if (!symbolTable.getMethods().contains(methodName)) {
                    if (superClass != null) {
                        if (!symbolTable.isClassImported(superClass)) {
                            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), Integer.parseInt(jmmNode.get("colStart")), "Method " + methodName + " not declared because " + superClass + " is not imported"));
                        }
                    } else {
                        reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), Integer.parseInt(jmmNode.get("colStart")), "Method " + methodName + " not declared"));
                    }
                }
            }
            else if (child.getKind().equals("BinaryOp") || child.getKind().equals("BooleanOp") || child.getKind().equals("BoolLiteral") ||
                    child.getKind().equals("IntLiteral") || child.getKind().equals("Not") || child.getKind().equals("ArrayLength") ||
                    child.getKind().equals("ArrayAccess") || child.getKind().equals("NewIntArray") || child.getKind().equals("NewObject")) {
                reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), Integer.parseInt(jmmNode.get("colStart")), "Method " + methodName + " not declared"));
            }
            else if (child.getKind().equals("MethodCall")) {
                visitMethod(child, ret);
            }
        }

        return 1;
    }
}
