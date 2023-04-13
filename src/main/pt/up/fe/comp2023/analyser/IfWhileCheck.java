package pt.up.fe.comp2023.analyser;

import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.List;

public class IfWhileCheck extends PreorderJmmVisitor<Integer, Integer> {
    private final MySymbolTable symbolTable;
    private final List<Report> reports;

    public IfWhileCheck(MySymbolTable symbolTable, List<Report> reports) {
        this.reports = reports;
        this.symbolTable = symbolTable;
        setDefaultVisit((node, oi) -> 0);
    }

    public Integer visitIfElse(JmmNode node, Integer arg) {
        JmmNode condition = node.getChildren().get(0);
        Type conditionType = AnalysisUtils.getType(condition, symbolTable);
        if (conditionType.getName().equals("unknown")) {
            return 0;
        }
        if (conditionType.isArray()) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(node.get("lineStart")), "Cannot apply operator" + "if" + " to array"));
            return 0;
        }
        if (!conditionType.getName().equals("boolean")) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(node.get("lineStart")), "Cannot apply operator" + "if" + " to " + conditionType.getName()));
            return 0;
        }
        return 0;
    }

    public Integer visitWhile(JmmNode node, Integer arg) {
        JmmNode condition = node.getChildren().get(0);
        Type conditionType = AnalysisUtils.getType(condition, symbolTable);
        if (conditionType.getName().equals("unknown")) {
            return 0;
        }
        if (conditionType.isArray()) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(node.get("lineStart")), "Cannot apply operator" + "while" + " to array"));
            return 0;
        }
        if (!conditionType.getName().equals("boolean")) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(node.get("lineStart")), "Cannot apply operator" + "while" + " to " + conditionType.getName()));
            return 0;
        }
        return 0;
    }

    @Override
    protected void buildVisitor() {
        addVisit("IfElse", this::visitIfElse);
        addVisit("While", this::visitWhile);
    }

}
