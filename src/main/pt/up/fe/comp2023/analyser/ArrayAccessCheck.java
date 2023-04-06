package pt.up.fe.comp2023.analyser;

import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;

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
        System.out.println(jmmNode.getAttributes());
        return 1;
    }
}
