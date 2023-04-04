package pt.up.fe.comp2023.analyser;

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

        String returnType = symbolTable.getReturnType(methodName).getName();

        // iterate child and if kind is returnFromMethod
        // then check if the type is the same as the method return type
        for (JmmNode child : node.getChildren()) {
            if (child.getKind().equals("ReturnFromMethod")) {

                // if Kind is BooleanOp
               if (symbolTable.isBooleanExpression(child.getChildren().get(0).getKind())) {
                   if (!returnType.equals("boolean")) {
                       reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt("-1"), "Method " + methodName + " should return " + returnType));

                   }
               }

               // if Kind is BinaryOp
               else if (symbolTable.isMathExpression(child.getChildren().get(0).getKind())) {
                   if (!returnType.equals("int")) {
                       reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt("-1"), "Method " + methodName + " should return " + returnType));

                   }
               }

               // if Kind is MethodCall
               else if (child.getChildren().get(0).getKind().equals("MethodCall")) {
                     String methodCallName = child.getChildren().get(0).get("caller");
                     if (!symbolTable.getMethods().contains(methodCallName)) {
                         reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt("-1"), "Method " + methodName + " is not declared"));

                     }
                     String methodCallReturnType = symbolTable.getReturnType(methodCallName).getName();
                     if (!returnType.equals(methodCallReturnType)) {
                          reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt("-1"), "Method " + methodName + " should return " + returnType));

                     }
                }

               // if Kinds is Not
                else if (child.getChildren().get(0).getKind().equals("Not")) {
                     if (!returnType.equals("boolean")) {
                          reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt("-1"), "Method " + methodName + " should return " + returnType));
                     }
                }

                // if Kind is IntLiteral
                else if (child.getChildren().get(0).getKind().equals("IntLiteral")) {
                    if (!returnType.equals("int")) {
                        reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt("-1"), "Method " + methodName + " should return " + returnType));
                    }
                }

                // if kind is BooleanLiteral
                else if (child.getChildren().get(0).getKind().equals("BoolLiteral")) {
                    if (!returnType.equals("boolean")) {
                        reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt("-1"), "Method " + methodName + " should return " + returnType));
                    }
                }

                // if kind is ArrayAccess
                else if (child.getChildren().get(0).getKind().equals("ArrayAccess")) {
                    if (!returnType.equals("int")) {
                        reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt("-1"), "Method " + methodName + " should return " + returnType));
                    }
                }

                // if kind is ArrayLength
                else if (child.getChildren().get(0).getKind().equals("ArrayLength")) {
                    if (!returnType.equals("int")) {
                        reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt("-1"), "Method " + methodName + " should return " + returnType));
                    }
                }

                // if kind is NewIntArray
                else if (child.getChildren().get(0).getKind().equals("NewIntArray")) {
                    if (!returnType.equals("int[]")) {
                        reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt("-1"), "Method " + methodName + " should return " + returnType));
                    }
                }

                // if kind is NewObject
                else if (child.getChildren().get(0).getKind().equals("NewObject")) {
                    var id = child.getChildren().get(0).get("name");
                    if (!returnType.equals(id)) {
                        reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt("-1"), "Method" + methodName + " should return " + returnType));
                    }
                }

                // if kind is Id
                else if (child.getChildren().get(0).getKind().equals("Id")) {
                    var id = child.getChildren().get(0).get("name");
                    var idSymbol = symbolTable.findField(id);
                    if (idSymbol == null) {reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt("-1"), "Variable " + id + " is not declared"));}
                    var idType = idSymbol.getType().getName();
                    if (!returnType.equals(idType)) {
                        reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt("-1"), "Method " + methodName + " should return " + returnType));
                    }

                }

                // falta parenteses

            }
        }


        return 1;
    }
}