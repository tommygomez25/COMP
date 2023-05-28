package pt.up.fe.comp2023.optimizer;

import org.specs.comp.ollir.Descriptor;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import org.specs.comp.ollir.Method;

import java.util.ArrayList;
import java.util.HashMap;

public class LivenessAnalysis {

    private final OllirResult ollirResult;
    private final ArrayList<MethodLivenessAnalysis> methodFlowList;

    public LivenessAnalysis(OllirResult ollirResult) {
        this.ollirResult = ollirResult;
        this.methodFlowList = new ArrayList<>();
    }

    public void getInAndOut() {
        ollirResult.getOllirClass().buildCFGs();
        ArrayList<Method> methods = ollirResult.getOllirClass().getMethods();

        for (Method method : methods) {
            MethodLivenessAnalysis methodLivenessAnalysis = new MethodLivenessAnalysis(method, ollirResult);
            methodLivenessAnalysis.getInAndOut();
            methodFlowList.add(methodLivenessAnalysis);
        }
    }

    public void getInterferenceGraph() {
        for (MethodLivenessAnalysis methodLivenessAnalysis : methodFlowList) {
            methodLivenessAnalysis.getInterferenceGraph();
        }
    }

    public void colorGraph() {
        for (MethodLivenessAnalysis methodLivenessAnalysis : methodFlowList) {
            Integer n = Integer.parseInt(ollirResult.getConfig().get("registerAllocation"));
            methodLivenessAnalysis.colorGraph(n);
        }
    }

    public void allocateRegisters() {
        for (MethodLivenessAnalysis methodLivenessAnalysis : methodFlowList) {
            HashMap<String, Descriptor> methodVariables = methodLivenessAnalysis.getMethod().getVarTable();
            for (RegisterNode registerNode : methodLivenessAnalysis.interferenceGraph().getLocalVariables()) {
                methodVariables.get(registerNode.getName()).setVirtualReg(registerNode.getRegister());
            }
            for (RegisterNode registerNode : methodLivenessAnalysis.interferenceGraph().getParameters()) {
                methodVariables.get(registerNode.getName()).setVirtualReg(registerNode.getRegister());
            }
            if(methodVariables.get("this") != null){
                methodVariables.get("this").setVirtualReg(0);
            }
        }
    }
}
