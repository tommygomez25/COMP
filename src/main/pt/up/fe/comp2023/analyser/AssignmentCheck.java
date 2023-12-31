package pt.up.fe.comp2023.analyser;

import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.List;

public class AssignmentCheck extends PreorderJmmVisitor<Integer, Integer> {

    private final MySymbolTable symbolTable;
    private final List<Report> reports;
    public AssignmentCheck(MySymbolTable symbolTable, List<Report> reports) {
        this.symbolTable = symbolTable;
        this.reports = reports;
        buildVisitor();
        setDefaultVisit((node, arg) -> 0);
    }

    @Override
    protected void buildVisitor() {
        addVisit("Assign", this::visitAssign);
        addVisit("ArrayAssign",this::visitArrayAssign);
    }

    public Integer visitAssign(JmmNode node, Integer ret) {

        JmmNode right = node.getChildren().get(0);
        Type leftType;
        if (node.getAncestor("Method").isPresent()) {
            String methodName = node.getAncestor("Method").get().get("methodName");
            leftType = symbolTable.getVarType(node.get("varName"), methodName);
        }
        else {
            leftType = symbolTable.getVarType(node.get("varName"));
        }

        Type rightType = AnalysisUtils.getType(right,symbolTable);
        if (rightType == null) {
            return 0;
        }
        if (!AnalysisUtils.typeIsCompatibleWith(leftType,rightType,symbolTable)) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(node.get("lineStart")), "Cannot assign " + rightType.getName() + " to " + leftType.getName()));
            return 0;
        }

        return 1;
    }

    public Integer visitArrayAssign(JmmNode node, Integer ret) {

        JmmNode right = node.getChildren().get(1);
        JmmNode middle = node.getChildren().get(0);

        Type leftType;
        if (node.getAncestor("Method").isPresent()) {
            String methodName = node.getAncestor("Method").get().get("methodName");
            leftType = symbolTable.getVarType(node.get("varName"), methodName);

        }
        else {
            leftType = symbolTable.getVarType(node.get("varName"));
        }

        Type rightType = AnalysisUtils.getType(right,symbolTable);
        Type middleType = AnalysisUtils.getType(middle,symbolTable);

        if (!leftType.getName().equals(rightType.getName())) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(node.get("lineStart")), "Cannot assign " + rightType.getName() + " to " + leftType.getName()));
        }

        if (!middleType.getName().equals("int") || middleType.isArray()) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(node.get("lineStart")), "Array accessor must be of type int"));
        }

        return 1;
    }
}
