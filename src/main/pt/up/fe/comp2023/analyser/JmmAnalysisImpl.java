package pt.up.fe.comp2023.analyser;

import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.analysis.JmmAnalysis;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class JmmAnalysisImpl implements JmmAnalysis {
    @Override
    public JmmSemanticsResult semanticAnalysis(JmmParserResult jmmParserResult){

        if (TestUtils.getNumReports(jmmParserResult.getReports(), ReportType.ERROR) > 0L) {
            var errorReport = new Report(ReportType.ERROR,Stage.SEMANTIC,-1,
                    "Semantic analysis can't start if there are errors from previous stages");
            return new JmmSemanticsResult(jmmParserResult,null,Arrays.asList(errorReport));
        }

        else if (jmmParserResult.getRootNode() == null || jmmParserResult.getRootNode().getJmmParent() != null) {
            var errorReport = new Report(ReportType.ERROR, Stage.SEMANTIC,-1,
                    "Semantic analysis can't start if AST root node is null or has a parent");
            return new JmmSemanticsResult(jmmParserResult,null, Arrays.asList(errorReport));
        }

        else {
            List<Report> reports = new ArrayList<>();

            /* create Symbol Table */
            JmmNode rootNode = jmmParserResult.getRootNode();
            MySymbolTable mySymbolTable = new MySymbolTable();
            List<Report> symbolTableReports = new MySymbolTableVisitor().visit(rootNode, mySymbolTable);

            var methodUndeclaredCheck = new UndeclaredMethodCheck(mySymbolTable,symbolTableReports);
            methodUndeclaredCheck.visit(rootNode, null);

            var varNotDeclaredCheck = new VarNotDeclaredCheck(mySymbolTable,symbolTableReports);
            varNotDeclaredCheck.visit(rootNode, null);

            var returnTypeCheck = new ReturnTypeCheck(mySymbolTable,symbolTableReports);
            returnTypeCheck.visit(rootNode, null);

            var argTypeCheck = new ArgTypeCheck(mySymbolTable,symbolTableReports);
            argTypeCheck.visit(rootNode, null);

            var thisStaticCheck = new ThisStaticCheck(mySymbolTable,symbolTableReports);
            thisStaticCheck.visit(rootNode, null);

            var arrayAccessCheck = new ArrayAccessCheck(mySymbolTable,symbolTableReports);
            arrayAccessCheck.visit(rootNode, null);

            var assignmentCheck = new AssignmentCheck(mySymbolTable,symbolTableReports);
            assignmentCheck.visit(rootNode, null);

            var BinaryOpCheck = new BinaryOpCheck(mySymbolTable,symbolTableReports);
            BinaryOpCheck.visit(rootNode, null);

            var IfWhileCheck = new IfWhileCheck(mySymbolTable,symbolTableReports);
            IfWhileCheck.visit(rootNode, null);

            System.out.println(symbolTableReports);

            return new JmmSemanticsResult(jmmParserResult, mySymbolTable, symbolTableReports);
        }
    }
}
