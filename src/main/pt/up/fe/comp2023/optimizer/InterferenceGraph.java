package pt.up.fe.comp2023.optimizer;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class InterferenceGraph {

    private final Set<RegisterNode> locals;
    private final Set<RegisterNode> parameters;

    public InterferenceGraph(Set<String> nodes, Set<String> parameters){
        this.locals = new HashSet<>();
        this.parameters = new HashSet<>();
        for(String node: nodes){
            this.locals.add(new RegisterNode(node));
        }
        for(String parameter: parameters){
            this.parameters.add(new RegisterNode(parameter));
        }
    }

    public void addEdge(RegisterNode r1, RegisterNode r2){
        r1.addNeighbor(r2);
        r2.addNeighbor(r1);
    }

    public void removeEdge(RegisterNode r1, RegisterNode r2){
        r1.removeNeighbor(r2);
        r2.removeNeighbor(r1);
    }

    public Set<RegisterNode> getLocalVariables(){
        return locals;
    }

    public Set<RegisterNode> getParameters(){
        return parameters;
    }

    public int getNumVisibleNodes(){
        int n = 0;
        for(RegisterNode node: locals){
            if(node.isVisible()){
                n++;
            }
        }
        return n;
    }


}
