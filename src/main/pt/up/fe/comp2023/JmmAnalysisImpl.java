package pt.up.fe.comp2023;

import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.analysis.JmmAnalysis;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.examples.ExamplePostorderVisitor;
import pt.up.fe.comp.jmm.ast.examples.ExamplePreorderVisitor;
import pt.up.fe.comp.jmm.ast.examples.ExampleVisitor;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.jmm.report.ReportType;

import java.util.Collections;
import java.util.HashMap;

public class JmmAnalysisImpl implements JmmAnalysis {
    @Override
    public JmmSemanticsResult semanticAnalysis(JmmParserResult jmmParserResult){
        if (TestUtils.getNumReports(jmmParserResult.getReports(), ReportType.ERROR) > 0L) {
            return null;
        } else if (jmmParserResult.getRootNode() == null) {
            return null;
        } else {
            JmmNode node = jmmParserResult.getRootNode();
            MySymbolTable mySymbolTable = new MySymbolTable();
            ClassVisitor classVisitor = new ClassVisitor();
            classVisitor.visit(node);
            mySymbolTable.setClassName(classVisitor.getClassName());
            mySymbolTable.setSuperName(classVisitor.getSuperClassName());
            mySymbolTable.setFields(classVisitor.getFields());
            ImportVisitor importVisitor = new ImportVisitor();
            importVisitor.visit(node);
            mySymbolTable.setImports(importVisitor.getImports());
            MethodVisitor methodVisitor = new MethodVisitor();
            methodVisitor.visit(node);
            mySymbolTable.setMethods(methodVisitor.getMethods());
            mySymbolTable.setMethodsReturnTypes(methodVisitor.getMethodsReturnTypes());
            mySymbolTable.setMethodsParameters(methodVisitor.getMethodsParams());
            mySymbolTable.setMethodsLocalVariables(methodVisitor.getMethodsLocals());

            return new JmmSemanticsResult(jmmParserResult, mySymbolTable, Collections.emptyList());
        }
    }
}
