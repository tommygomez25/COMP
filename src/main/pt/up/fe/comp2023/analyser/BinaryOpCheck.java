package pt.up.fe.comp2023.analyser;

import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;

import java.util.List;

public class BinaryOpCheck extends PreorderJmmVisitor<Integer,Integer> {
    private final MySymbolTable symbolTable;
    private final List<Report> reports;

    public BinaryOpCheck(MySymbolTable symbolTable,List<Report> reports) {
        this.reports = reports;
        this.symbolTable = symbolTable;
        setDefaultVisit((node,oi)->0);
    }

    public Integer visitBinaryOp(JmmNode node, Integer arg) {
        // get Ancestor of type Method
        String method = node.getAncestor("Method").toString();
        return 1;
    }


    @Override
    protected void buildVisitor() {
        addVisit("BinaryOp", this::visitBinaryOp);
    }
}
