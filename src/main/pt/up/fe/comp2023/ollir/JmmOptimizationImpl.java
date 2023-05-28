package pt.up.fe.comp2023.ollir;

import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ollir.JmmOptimization;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp2023.analyser.MySymbolTable;
import pt.up.fe.comp2023.optimizer.LivenessAnalysis;


public class JmmOptimizationImpl implements JmmOptimization{
    @Override
    public OllirResult toOllir(JmmSemanticsResult semanticsResult){
        var OllirGenerator = new OllirGenerator((MySymbolTable) semanticsResult.getSymbolTable());

        OllirGenerator.visit(semanticsResult.getRootNode());

        return new OllirResult(semanticsResult,OllirGenerator.getCode(), semanticsResult.getReports());
    }

    @Override
    public OllirResult optimize(OllirResult ollirResult){
        if (ollirResult.getConfig().getOrDefault("debug", "false").equals("true")){
            System.out.println("Optimizing OLLIR code");
            System.out.println(ollirResult.getOllirCode());
        }

        if (ollirResult.getConfig().getOrDefault("registerAllocation", "-1").equals("-1")){
            System.out.println("Register allocation not implemented");
            return ollirResult;
        }

        LivenessAnalysis livenessAnalysis = new LivenessAnalysis(ollirResult);

        livenessAnalysis.getInAndOut();

        livenessAnalysis.getInterferenceGraph();

        livenessAnalysis.colorGraph();

        livenessAnalysis.allocateRegisters();

        return ollirResult;
    }
}
