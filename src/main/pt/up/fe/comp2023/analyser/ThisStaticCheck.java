package pt.up.fe.comp2023.analyser;

import pt.up.fe.comp.jmm.analysis.table.Type;
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
        addVisit("This",this::visitThis);
    }

    public Integer visitAssign(JmmNode jmmNode, Integer arg) {
        var varName = jmmNode.get("varName");
        Type varType;
        if (jmmNode.getAncestor("Method").isPresent()) {
            var methodName = jmmNode.getAncestor("Method").get().get("methodName");
            varType = symbolTable.getVarType(varName, methodName);
        }
        else {
            varType = symbolTable.getVarType(varName);
        }

        for (JmmNode child : jmmNode.getChildren()) {
            if (child.getKind().equals("This")) {
                if (child.getAncestor("Method").isPresent()) {
                    if (child.getAncestor("Method").get().get("methodName").equals("main")) {
                        reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), "Cannot use this in main method"));
                        return 0;
                    }
                }
                if (!varType.getName().equals("unknown")) {
                    if (symbolTable.isVarClass(varName)) {
                           if (varType.getName().equals(symbolTable.getClassName())) {return 1;}
                           else if (varType.getName().equals(symbolTable.getSuper())) {
                               if (symbolTable.isClassImported(symbolTable.getSuper())) {
                                   return 1;
                               }
                           }
                           else {
                               reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), "Cannot use this in static context"));
                           }
                    }
                }
            }
        }

        return 1;
    }

    public Integer visitThis(JmmNode jmmNode, Integer arg) {

        if (jmmNode.getAncestor("Method").isPresent()) {
            if (jmmNode.getAncestor("Method").get().get("methodName").equals("main")) {
                reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), "Cannot use this in main method"));
            }
        }

        if (jmmNode.getAncestor("Method").isEmpty()) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), "Cannot use this outside a method"));
        }
        return 1;
    }

}
