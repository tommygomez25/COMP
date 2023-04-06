package pt.up.fe.comp2023.analyser;

import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2023.analyser.MySymbolTable;

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

        if (!symbolTable.getMethods().contains(methodName)) {
            if (symbolTable.getSuper() != null) {
                if (!symbolTable.isClassImported(symbolTable.getSuper())) {
                    reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt("-1"), Integer.parseInt("-1"),
                            "Class " + symbolTable.getSuper() + " is not imported"));
                }
            }

        }

        for (JmmNode child : jmmNode.getChildren()) {
            if (child.getKind().equals("Arguments")) {
                continue;
            }
            if (child.getKind().equals("Id")) {
                String id = child.get("name");
                // if id starts by a capital letter then it is a class
                if (Character.isUpperCase(id.charAt(0))) {
                    if (!symbolTable.isClassImported(id)) {
                        reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt("-1"), Integer.parseInt("-1"),
                                "Class " + id + " is not imported"));
                    }
                }

                else {
                    var idType = symbolTable.findField(id).getType().getName();
                    if (Character.isUpperCase(idType.charAt(0))) {
                        if (!symbolTable.isClassImported(idType) && !symbolTable.isClassImported(symbolTable.getSuper())) {
                            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt("-1"), Integer.parseInt("-1"),
                                    "Class " + idType + " is not imported"));
                        }
                    }
                }

            }
        }

        return 0;
    }
}
