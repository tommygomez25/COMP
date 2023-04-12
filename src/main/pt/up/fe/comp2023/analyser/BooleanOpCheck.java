package pt.up.fe.comp2023.analyser;

import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.List;

public class BooleanOpCheck extends PreorderJmmVisitor<Integer,Integer> {
    private final MySymbolTable symbolTable;
    private final List<Report> reports;

    public BooleanOpCheck(MySymbolTable symbolTable,List<Report> reports) {
        this.reports = reports;
        this.symbolTable = symbolTable;
        setDefaultVisit((node,oi)->0);
    }

    public Integer visitBooleanOp(JmmNode node, Integer arg) {
        JmmNode left = node.getChildren().get(0);
        JmmNode right = node.getChildren().get(1);
        String op = node.get("op");

        Type leftType = AnalysisUtils.getType(left,symbolTable);
        Type rightType = AnalysisUtils.getType(right,symbolTable);

        if (leftType.getName().equals("unknown") || rightType.getName().equals("unknown")) {
            return 0;
        }

        if (!leftType.equals(rightType)) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(node.get("lineStart")), "Cannot apply operator" + op ));
            return 0;
        }

        if (AnalysisUtils.LOGICAL_OP.contains(op)) {
            if (leftType.isArray() || rightType.isArray()) {
                reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(node.get("lineStart")), "Cannot apply operator" + op + " to array"));
                return 0;
            }
            if (!(leftType.getName().equals("boolean") && rightType.getName().equals("boolean"))) {
                reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(node.get("lineStart")), "Cannot apply operator" + op + " to " + leftType.getName()));
                return 0;
            }

        }

        if (AnalysisUtils.COMPARISON_OP.contains(op)) {
            if (leftType.isArray() || rightType.isArray()) {
                reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(node.get("lineStart")), "Cannot apply operator" + op + " to array"));
                return 0;
            }
            if (!(leftType.getName().equals("int") && rightType.getName().equals("int"))) {
                reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(node.get("lineStart")), "Cannot apply operator" + op + " to " + leftType.getName()));
                return 0;
            }

        }


        return 1;
    }


    @Override
    protected void buildVisitor() {
        addVisit("BooleanOp", this::visitBooleanOp);
    }
}
