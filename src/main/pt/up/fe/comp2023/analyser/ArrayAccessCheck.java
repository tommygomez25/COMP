package pt.up.fe.comp2023.analyser;

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
        System.out.println(jmmNode.getAttributes());
        JmmNode array = jmmNode.getChildren().get(0);
        JmmNode accessor = jmmNode.getChildren().get(1).getChildren().get(0);
        boolean isMathExpression = symbolTable.isMathExpression(accessor.getKind());
        boolean isBooleanExpression = symbolTable.isBooleanExpression(accessor.getKind());

        if (!array.getKind().equals("Id") || !array.getKind().equals("NewIntArray")) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), "Array access on non-array"));
        }

        else {
            Type arrayType = symbolTable.getVarType(array.get("name"));
            if (arrayType == null) {
                reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), "Array access on non-array"));
            }
            else if (!arrayType.getName().equals("int[]")) {
                reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), "Array access on non-array"));
            }
        }

        if (isBooleanExpression || accessor.getKind().equals("BoolLiteral") ||
            accessor.getKind().equals("NewIntArray") || accessor.getKind().equals("NewObject") || accessor.getKind().equals("Not")) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), "Array accessor must be an integer"));
        }
        else if (isMathExpression || accessor.getKind().equals("IntLiteral") || accessor.getKind().equals("ArrayLength")) {
            return 1;
        }

        else if (accessor.getKind().equals("Id")) {
            String variableType = symbolTable.getVarType(accessor.get("name")).getName();
            if (!variableType.equals("int")) {
                reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), "Array accessor must be an integer"));
            }
        }
        else if (accessor.getKind().equals("MethodCall")) {
            String methodReturnType = symbolTable.getReturnType(accessor.get("methodName")).getName();
            if (!methodReturnType.equals("int")) {
                reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), "Array accessor must be an integer"));
            }
        }
        else if (accessor.getKind().equals("ArrayAccess")) {
            visit(accessor, arg);
        }



        return 1;
    }
}
