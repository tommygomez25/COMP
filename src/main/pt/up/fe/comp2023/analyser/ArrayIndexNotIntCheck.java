package pt.up.fe.comp2023.analyser;

import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.List;

public class ArrayIndexNotIntCheck extends PreorderJmmVisitor<Integer, Integer> {
    private final MySymbolTable symbolTable;
    private final List<Report> reports;

    public ArrayIndexNotIntCheck(MySymbolTable symbolTable, List<Report> reports) {
        this.reports = reports;
        this.symbolTable = symbolTable;
        buildVisitor();
        setDefaultVisit((node, arg) -> 0);
    }

    @Override
    protected void buildVisitor() {
        addVisit("ArrayAccess", this::visitArrayAccess);
    }

    public Integer visitArrayAccess(JmmNode jmmNode, Integer arg) {
        for (JmmNode child: jmmNode.getChildren()) {
            if (child.getKind().equals("Accessors")) {
                JmmNode accessNode = child;
                for (JmmNode grandChild : accessNode.getChildren()) {
                    boolean isBooleanExpression = symbolTable.isBooleanExpression(grandChild.getKind());
                    boolean isMathExpression = symbolTable.isMathExpression(grandChild.getKind());

                    if (isBooleanExpression || grandChild.getKind().equals("BoolLiteral") ||
                            grandChild.getKind().equals("Not")) {
                        reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt("-1"), "Array index must be an integer"));
                        return 0;
                    }

                    else if (isMathExpression || grandChild.getKind().equals("IntLiteral") ||
                            grandChild.getKind().equals("ArrayLength")){
                        return 1;
                    }

                    else if (grandChild.getKind().equals("Id")) {
                        var id = grandChild.get("name");
                        var idSymbol = symbolTable.findField(id);
                        if (idSymbol == null) {
                            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt("-1"), "Variable " + id + " is not declared"));
                            return 0;
                        }
                        var idType = idSymbol.getType().getName();
                        if (!idType.equals("int")) {
                            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt("-1"), "Array index must be an integer"));
                            return 0;
                        }
                    }

                    else if (grandChild.getKind().equals("This")) {
                        reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt("-1"), "Array index must be an integer"));
                        return 0;
                    }

                    else if (grandChild.getKind().equals("MethodCall")) {
                        String methodName = grandChild.get("methodName");

                        if (!symbolTable.getMethods().contains(methodName)) {
                            if (symbolTable.getSuper() != null) {
                                return 1;
                            }

                            if (!symbolTable.getImports().isEmpty()) {
                                return 1;
                            }
                        }

                        else {
                            var returnType = symbolTable.getReturnType(methodName).getName();
                            if (!returnType.equals("int")) {
                                reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt("-1"), "Array index must be an integer"));
                                return 0;
                            }
                        }

                    }

                    else if (grandChild.getKind().equals("ArrayAccess")) {
                        visit(accessNode, 0);
                    }

                    else if (grandChild.getKind().equals("NewObject") || grandChild.getKind().equals("NewIntArray")) {
                        reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt("-1"), "Array index must be an integer"));
                        return 0;
                    }
                }
            }
                }
                
        return 0;
    }
}
