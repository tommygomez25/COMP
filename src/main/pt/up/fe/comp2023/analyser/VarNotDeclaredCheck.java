package pt.up.fe.comp2023.analyser;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.List;

public class VarNotDeclaredCheck extends PreorderJmmVisitor<Integer, Integer> {

    private final MySymbolTable symbolTable;
    private final List<Report> reports;

    public VarNotDeclaredCheck(MySymbolTable symbolTable, List<Report> reports) {
        this.symbolTable = symbolTable;
        this.reports = reports;
        buildVisitor();
        setDefaultVisit((node, arg) -> 0);
    }

    @Override
    protected void buildVisitor() {
        addVisit("Id", this::visitExpr);
        addVisit("Assign", this::visitAssign);
        addVisit("ArrayAssign",this::visitArrayAssign);
        addVisit("ArrayLength", this::visitArrayLength);
        addVisit("ArrayAccess", this::visitArrayAccess);
    }

    public Integer visitExpr(JmmNode node, Integer ret) {

        String varName = node.get("name");
        if (!symbolTable.isVarDeclared(varName)) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(node.get("lineStart")), "Variable " + varName + " not declared"));
            return 0;
        }

    return 1;
    }

    public Integer visitAssign(JmmNode node, Integer ret) {
        String varName = node.get("varName");
        if (!symbolTable.isVarDeclared(varName)) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(node.get("lineStart")), "Variable " + varName + " not declared"));
            return 0;
        }
        return 1;
    }

    public Integer visitArrayAssign(JmmNode node, Integer ret) {
        String varName = node.get("varName");
        if (!symbolTable.isVarDeclared(varName)) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(node.get("lineStart")), "Variable " + varName + " not declared"));
            return 0;
        }
        return 1;
    }

    public Integer visitArrayLength(JmmNode node, Integer ret) {
        JmmNode array = node.getChildren().get(0);
        Symbol operandType = AnalysisUtils.getSymbol(array, symbolTable);
        if (operandType == null) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(node.get("lineStart")), "Variable not declared"));
            return 0;
        }
        else {
            if (!symbolTable.isVarDeclared(operandType.getName())) {
                reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(node.get("lineStart")), "Variable " + operandType.getName() + " not declared"));
                return 0;
            }
        }
        return 1;
    }

    public Integer visitArrayAccess(JmmNode jmmNode, Integer ret) {
        JmmNode array = jmmNode.getChildren().get(0);
        Symbol operandType = AnalysisUtils.getSymbol(array, symbolTable);
        if (!symbolTable.isVarDeclared(operandType.getName())) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), "Variable " + operandType.getName() + " not declared"));
            return 0;
        }
        return 1;
    }
}
