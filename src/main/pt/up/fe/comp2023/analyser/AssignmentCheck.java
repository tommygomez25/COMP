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

        String varName = node.get("varName");
        if (!symbolTable.isVarDeclared(varName)) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(node.get("lineStart")), "Variable " + varName + " not declared"));
        }

        var varType = symbolTable.getVarType(varName);

        for (JmmNode child : node.getChildren()) {
            if (child.getKind().equals("This")) {
                if (child.getAncestor("Method").isPresent()) {
                    if (child.getAncestor("Method").get().get("methodName").equals("main")) {
                        reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(node.get("lineStart")), "Cannot use this in main method"));

                    }
                }
                if (varType != null) {
                    if (symbolTable.isVarClass(varName)) {
                        if (varType.getName().equals(symbolTable.getClassName())) {continue;}
                        else if (varType.getName().equals(symbolTable.getSuper())) {
                            if (symbolTable.isClassImported(symbolTable.getSuper())) {
                                continue;
                            }
                        }
                        else {
                            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(node.get("lineStart")), "Cannot use this in static context"));
                        }
                    }
                }
            }
        }

        JmmNode right = node.getChildren().get(0);

        Type leftType = symbolTable.getVarType(node.get("varName"));
        Type rightType = AnalysisUtils.getType(right,symbolTable);

        if (!AnalysisUtils.typeIsCompatibleWith(leftType,rightType,symbolTable)) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(node.get("lineStart")), "Cannot assign " + rightType.getName() + " to " + leftType.getName()));
            return 0;
        }

        return 1;
    }

    public Integer visitArrayAssign(JmmNode node, Integer ret) {

        String varName = node.get("varName");
        if (!symbolTable.isVarDeclared(varName)) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(node.get("lineStart")), "Variable " + varName + " not declared"));
        }

        JmmNode right = node.getChildren().get(0);

        Type leftType = symbolTable.getVarType(node.get("varName"));
        Type rightType = AnalysisUtils.getType(right,symbolTable);

        if (!AnalysisUtils.typeIsCompatibleWith(leftType,rightType,symbolTable)) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(node.get("lineStart")), "Cannot assign " + rightType.getName() + " to " + leftType.getName()));
            return 0;
        }

        return 1;
    }
}
