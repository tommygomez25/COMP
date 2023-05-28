package pt.up.fe.comp2023.optimizer;

import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.function.BiFunction;

public class ConstantPropagation extends AJmmVisitor<HashMap<String, JmmNode>, String> {

    private boolean changed = false;
    private final boolean simpleWhile;
    public ConstantPropagation() {
        this.changed = false;
        this.simpleWhile = false;
    }

    public ConstantPropagation(boolean simpleWhile) {
        this.simpleWhile = simpleWhile;
        buildVisitor();
    }

    @Override
    protected void buildVisitor() {
        addVisit("MethodBody", this::methodBodyVisitor);
        /*addVisit("Identifier", this::idVisitor);
        addVisit("Assignment", this::assignVisitor);
        addVisit("BinaryOp", this::binaryOpVisitor);
        addVisit("NotExpression", this::notVisitor);
        addVisit("IfThenElseStatement", this::ifThenElseStatVisitor);
        addVisit("WhileStatement", this::whileStatVisitor);*/

        setDefaultVisit(this::defaultVisitor);
    }

    public boolean hasChanged() {
        return changed;
    }

    private String methodBodyVisitor(JmmNode node, HashMap<String, JmmNode> map) {
        HashMap<String, JmmNode> newConstantsMap = new HashMap<>(map);
        for(JmmNode child : node.getChildren()) {
            visit(child, newConstantsMap);
        }
        updateMap(map, newConstantsMap);
        return "";
    }

    private void updateMap(HashMap<String, JmmNode> oldMap, HashMap<String, JmmNode> newMap) {
        for (Map.Entry<String, JmmNode> entry : newMap.entrySet()) {
            JmmNode changedNode = oldMap.get(entry.getKey());
            if (changedNode != null) {
                if (!changedNode.get("val").equals(entry.getValue().get("val")))
                    oldMap.remove(entry.getKey());
            }
        }

        for (String key : new HashSet<>(oldMap.keySet())) {
            if (newMap.get(key) == null)
                oldMap.remove(key);
        }
    }

    private String defaultVisitor(JmmNode jmmNode, HashMap<String, JmmNode> map) {
        for(JmmNode child : jmmNode.getChildren()) {
            visit(child, map);
        }
        return "";
    }
}
