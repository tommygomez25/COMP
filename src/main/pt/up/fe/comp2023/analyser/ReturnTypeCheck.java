package pt.up.fe.comp2023.analyser;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.List;

public class ReturnTypeCheck extends PreorderJmmVisitor<Integer,Integer> {

    private final MySymbolTable symbolTable;
    private final List<Report> reports;

    public ReturnTypeCheck(MySymbolTable symbolTable, List<Report> reports) {
        this.symbolTable = symbolTable;
        this.reports = reports;
        buildVisitor();
        setDefaultVisit((node, arg) -> 0);
    }

    @Override
    protected void buildVisitor() {
        addVisit("Method", this::visitMethod);
    }

    public Integer visitMethod(JmmNode node, Integer ret) {
        if (node.get("methodName").equals("main")) return 0;

        String methodName = node.get("methodName");

        Type returnType = symbolTable.getReturnType(methodName);

        JmmNode returnNode = node.getJmmChild(node.getNumChildren()-1);

        Type returnNodeType = AnalysisUtils.getType(returnNode.getJmmChild(0), symbolTable);
        Symbol returnNodeSymbol = AnalysisUtils.getSymbol(returnNode.getJmmChild(0), symbolTable);
        if (!returnType.equals(returnNodeType)) {
            if (symbolTable.isVarClass(returnNodeSymbol.getName())) {
                if (!symbolTable.isClassImported(returnNodeSymbol.getType().getName())) {
                    reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(node.get("lineStart")), "Class is not imported"));
                }
            }
            else {
                reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(node.get("lineStart")), "Method returns wrong type " ));
            }
        }
        return 1;
    }
}
