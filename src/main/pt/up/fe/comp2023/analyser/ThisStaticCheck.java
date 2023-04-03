package pt.up.fe.comp2023.analyser;

import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;

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

    public Integer visitField(JmmNode node, Integer arg) {
        var method = node.getJmmParent();
        System.out.println(method.getKind());
        return 1;
    }

    @Override
    protected void buildVisitor() {
        addVisit("Field", this::visitField);
    }
}
