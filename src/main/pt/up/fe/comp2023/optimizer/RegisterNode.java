package pt.up.fe.comp2023.optimizer;

import java.util.ArrayList;
import java.util.Objects;

public class RegisterNode {

    private final String name;
    private int register;
    private boolean visible;
    private final ArrayList<RegisterNode> neighbors;

    public RegisterNode(String name){
        this.name = name;
        this.register = -1;
        this.visible = true;
        this.neighbors = new ArrayList<>();
    }

    public void addNeighbor(RegisterNode node){
        neighbors.add(node);
    }

    public void removeNeighbor(RegisterNode node){
        neighbors.remove(node);
    }

    public int getNumVisibleNeighbors(){
        int n = 0;
        for(RegisterNode node: neighbors){
            if(node.isVisible()){
                n++;
            }
        }
        return n;
    }

    public boolean isVisible(){
        return visible;
    }

    public String getName(){
        return name;
    }

    public int getRegister(){
        return register;
    }

    public ArrayList<RegisterNode> getNeighbors(){
        return neighbors;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RegisterNode that = (RegisterNode) o;
        return Objects.equals(name, that.name);
    }

    public void setInvisible() {
        visible = false;
    }

    public void setVisible() {
        visible = true;
    }

    public void setRegister(int reg) {
        register = reg;
    }

    public boolean edgeFreeRegister(int reg) {
        for (RegisterNode r: neighbors) {
            if (r.getRegister() != -1
                    && r.getRegister() == reg) return false;
        }
        return true;
    }

}
