package pt.up.fe.comp2023.ollir;

import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ollir.JmmOptimization;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp2023.analyser.MySymbolTable;

import java.util.List;
import java.util.Collections;


public class JmmOptimizationImpl implements JmmOptimization{
    @Override
    public OllirResult toOllir(JmmSemanticsResult semanticsResult){
        var OllirGenerator = new OllirGenerator((MySymbolTable) semanticsResult.getSymbolTable());

        OllirGenerator.visit(semanticsResult.getRootNode());

        return new OllirResult(semanticsResult,OllirGenerator.getCode(), Collections.emptyList());
    }
}
