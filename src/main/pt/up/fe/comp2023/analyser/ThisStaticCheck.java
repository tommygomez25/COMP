package pt.up.fe.comp2023.analyser;

import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.List;

public class ThisStaticCheck extends PreorderJmmVisitor<Integer, Integer>{
    private final MySymbolTable symbolTable;
    private final List<Report> reports;

    public ThisStaticCheck(MySymbolTable symbolTable, List<Report> reports) {
        this.reports = reports;
        this.symbolTable = symbolTable;
        buildVisitor();
        setDefaultVisit((node, arg) -> 0);
    }
    @Override
    protected void buildVisitor() {
        addVisit("Assign", this::visitAssign);
    }

    public Integer visitAssign(JmmNode jmmNode, Integer arg) {
        for (JmmNode child : jmmNode.getChildren()) {
            if (child.getKind().equals("This")) {
                if (child.getAncestor("Method").get().get("methodName").equals("main")) {
                    reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt("-1"), "Cannot use this in main method"));
                    return 0;
                }
            }
        }
        return 1;
    }

}
