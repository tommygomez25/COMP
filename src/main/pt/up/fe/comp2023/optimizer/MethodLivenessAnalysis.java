package pt.up.fe.comp2023.optimizer;

import pt.up.fe.comp.jmm.ollir.OllirResult;

import java.util.*;

import org.specs.comp.ollir.*;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;

public class MethodLivenessAnalysis {

    private final Method method;
    private final OllirResult ollirResult;
    private ArrayList<Set<String>> def;
    private ArrayList<Set<String>> use;
    private ArrayList<Set<String>> in;
    private ArrayList<Set<String>> out;
    private ArrayList<Node> nodeOrder;
    private InterferenceGraph interferenceGraph;

    public MethodLivenessAnalysis(Method method, OllirResult ollirResult){
        this.method = method;
        this.ollirResult = ollirResult;
    }

    public InterferenceGraph interferenceGraph(){
        return interferenceGraph;
    }

    public Method getMethod(){
        return method;
    }

    private void orderNodes(){
        Node start = method.getBeginNode();
        this.nodeOrder = new ArrayList<>();
        dsfOrder(start, new ArrayList<>());
    }

    private void dsfOrder(Node node, ArrayList<Node> visited){

        if(node == null || nodeOrder.contains(node) || visited.contains(node)){
            return;
        }

        if (node instanceof Instruction instruction && !method.getInstructions().contains(instruction)) {
            return;
        }

        visited.add(node);

        for(Node successor: node.getSuccessors()){
            dsfOrder(successor, visited);
        }

        nodeOrder.add(node);
    }

    private void getDefAndUse(Node node, Node parent){
        if (node == null) return;

        Node useDef = parent == null ? node : parent;

        if (node.getNodeType().equals(NodeType.BEGIN) || node.getNodeType().equals(NodeType.END)){
            return;
        }

        if (node instanceof AssignInstruction instruction){
            addToUseDefSet(useDef, instruction.getDest(), use);
            getDefAndUse(instruction.getRhs(), node);
        }
        else if( node instanceof UnaryOpInstruction instruction){
            addToUseDefSet(useDef, instruction.getOperand(), use);
        }
        else if (node instanceof BinaryOpInstruction instruction){
            addToUseDefSet(useDef, instruction.getLeftOperand(), use);
            addToUseDefSet(useDef, instruction.getRightOperand(), use);
        }
        else if (node instanceof ReturnInstruction instruction){
            addToUseDefSet(useDef, instruction.getOperand(), use);
        } else if (node instanceof CallInstruction instruction) {
            addToUseDefSet(useDef, instruction.getFirstArg(), use);
            if (instruction.getListOfOperands() != null){
                for( Element element: instruction.getListOfOperands()){
                    addToUseDefSet(useDef, element, use);
                }
            }
        }
        else if (node instanceof GetFieldInstruction instruction) {
            addToUseDefSet(useDef, instruction.getFirstOperand(), use);
        } else if (node instanceof PutFieldInstruction instruction) {
            addToUseDefSet(useDef, instruction.getFirstOperand(), use);
            addToUseDefSet(useDef, instruction.getThirdOperand(), use);
        } else if (node instanceof SingleOpInstruction instruction) {
            addToUseDefSet(useDef, instruction.getSingleOperand(), use);
        } else if (node instanceof OpCondInstruction instruction) {
            for (Element operand: instruction.getOperands()) {
                addToUseDefSet(useDef, operand, use);
            }
        } else if (node instanceof SingleOpCondInstruction instruction) {
            for (Element operand: instruction.getOperands()) {
                addToUseDefSet(useDef, operand, use);
            }
        }
    }

    private void addToUseDefSet(Node node, Element element, ArrayList<Set<String>> array){
        int i = nodeOrder.indexOf(node);

        if (element instanceof ArrayOperand arrayOperand){
            for(Element elem: arrayOperand.getIndexOperands()){
                addToUseDefSet(node, elem, use);
            }
            array.get(i).add(arrayOperand.getName());
        }

        if (element instanceof Operand operand && !operand.getType().getTypeOfElement().equals(ElementType.THIS)){
            array.get(i).add(operand.getName());
        }
    }

    public void getInAndOut(){
        orderNodes();
        this.def = new ArrayList<>();
        this.use = new ArrayList<>();
        this.in = new ArrayList<>();
        this.out = new ArrayList<>();

        for(Node node: nodeOrder){
            in.add(new HashSet<>());
            out.add(new HashSet<>());
            def.add(new HashSet<>());
            use.add(new HashSet<>());
            getDefAndUse(node, null);
        }

        boolean changed;

        do{
            changed = false;
            for(int i = 0; i < nodeOrder.size(); i++){

                Set<String> oldIn = new HashSet<>(in.get(i));
                Set<String> oldOut = new HashSet<>(out.get(i));

                out.get(i).clear();

                for(Node successor: nodeOrder.get(i).getSuccessors()){
                    int index = nodeOrder.indexOf(successor);
                    if (index != -1) {
                        Set<String> inSuccessor = new HashSet<>(in.get(index));
                        out.get(i).addAll(inSuccessor);
                    }
                }

                in.get(i).clear();

                Set<String> outDifference = new HashSet<>(out.get(i));
                outDifference.removeAll(def.get(i));

                outDifference.addAll(use.get(i));
                in.get(i).addAll(outDifference);

                changed = changed || !oldIn.equals(in.get(i)) || !oldOut.equals(out.get(i));

            }
        } while( changed );

    }

    private List<String> getParamNames(){
        List<String> paramNames = new ArrayList<>();
        for (Element param: method.getParams()){
            String name = param instanceof Operand operand ? operand.getName() : null;
            paramNames.add(name);
        }
        return paramNames;
    }

    public void getInterferenceGraph(){
        Set<String> vars = new HashSet<>();
        Set<String> parameters = new HashSet<>();

        for (String var: method.getVarTable().keySet()){
            if(getParamNames().contains(var)){
                parameters.add(var);
            } else if(!var.equals("this")){
                vars.add(var);
            }
        }

        interferenceGraph = new InterferenceGraph(vars, parameters);

        for (RegisterNode i: interferenceGraph.getLocalVariables()){
            for( RegisterNode j: interferenceGraph.getLocalVariables()){
                if(!i.equals(j)){
                    for(int idx = 0; idx < nodeOrder.size(); idx++){
                        if (def.get(idx).contains(i.getName()) && out.get(idx).contains(j.getName())){
                            interferenceGraph.addEdge(i, j);
                        }
                    }
                }
            }
        }
    }

    public void colorGraph(int maxRegisters){
        Stack<RegisterNode> stack = new Stack<>();
        int numRegisters = 0;

        while (interferenceGraph.getNumVisibleNodes() > 0){
            for (RegisterNode node: interferenceGraph.getLocalVariables()){
                if (node.isVisible()){
                    int deg = node.getNumVisibleNeighbors();
                    if (deg < numRegisters){
                        node.setInvisible();
                        stack.push(node);
                    }
                    else{
                        numRegisters++;
                    }
                }
            }
        }

        if (maxRegisters > 0 && numRegisters > maxRegisters){
            ollirResult.getReports().add(
                    new Report(ReportType.ERROR,
                            Stage.OPTIMIZATION,
                            -1,
                            "Too many registers needed")
            );
            throw new RuntimeException("Not enough registers.");
        }

        int firstReg = 1 + interferenceGraph.getParameters().size();
        while(!stack.empty()){
            RegisterNode node = stack.pop();
            for (int reg = firstReg; reg <= numRegisters + firstReg; reg++){
                if (node.edgeFreeRegister(reg)){
                    node.setRegister(reg);
                    node.setVisible();
                    break;
                }
            }

            if(!node.isVisible()){
                ollirResult.getReports().add(
                        new Report(ReportType.ERROR,
                                Stage.OPTIMIZATION,
                                -1,
                                "Unexpected error.")
                );
                throw new RuntimeException("Unexpected error.");
            }
        }
        int n = 1;
        for( RegisterNode node: interferenceGraph.getParameters()){
            node.setRegister(n++);
        }
    }

}
