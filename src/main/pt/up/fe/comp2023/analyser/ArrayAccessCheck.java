package pt.up.fe.comp2023.analyser;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.List;


public class ArrayAccessCheck extends PreorderJmmVisitor<Integer, Integer> {
    private final MySymbolTable symbolTable;
    private final List<Report> reports;

    public ArrayAccessCheck(MySymbolTable symbolTable, List<Report> reports) {
        this.symbolTable = symbolTable;
        this.reports = reports;
        buildVisitor();
        setDefaultVisit((node, arg) -> 0);
    }

    @Override
    protected void buildVisitor() {
        addVisit("ArrayAccess", this::visitArrayAccess);
    }

    public Integer visitArrayAccess(JmmNode jmmNode, Integer arg) {

        JmmNode array = jmmNode.getChildren().get(0);
        Symbol operandSymbol = AnalysisUtils.getSymbol(array, symbolTable);
        if (!symbolTable.isVarDeclared(operandSymbol.getName())) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), "Variable " + operandSymbol.getName() + " not declared"));
        }

        JmmNode accessor = jmmNode.getChildren().get(1).getChildren().get(0);

        Type operandType = AnalysisUtils.getType(array, symbolTable);
        Type accessorType = AnalysisUtils.getType(accessor, symbolTable);

        if (!operandType.isArray()) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), "Cannot access array element of non-array type"));
        }

        if (!accessorType.getName().equals("int")) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), "Array accessor must be of type int"));
        }

        return 1;
    }
}
