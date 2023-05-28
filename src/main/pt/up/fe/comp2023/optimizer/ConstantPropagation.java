package pt.up.fe.comp2023.optimizer;

import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.JmmNodeImpl;

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
        addVisit("Method", this::methodBodyVisitor);
        addVisit("Assign", this::assignVisitor);
        addVisit("BinaryOp", this::binaryOpVisitor);
        addVisit("Id", this::idVisitor);
        addVisit("Not", this::notVisitor);

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
                if (!changedNode.get("var").equals(entry.getValue().get("var")))
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

    private String assignVisitor(JmmNode node, HashMap<String, JmmNode> map) {
        JmmNode identifier = node;
        JmmNode expression = node.getJmmChild(0);
        if (identifier.getKind().equals("Accessors")) {
            visit(identifier, map);
            return "";
        }
        if (expression.getKind().equals("IntLiteral") || expression.getKind().equals("BoolLiteral")) {
            map.put(identifier.get("varName"), expression);
        } else {
            visit(expression, map);
            map.remove(identifier.get("varName"));
        }
        return "";
    }

    private String binaryOpVisitor(JmmNode node, HashMap<String, JmmNode> map) {
        JmmNode left = node.getJmmChild(0);
        JmmNode right = node.getJmmChild(1);
        String leftVal = visit(left, map);
        String rightVal = visit(right, map);
        if(leftVal.equals(""))
            leftVal = rightVal;

        ConstantFold constantFold = new ConstantFold();

        if (left.getKind().equals("IntLiteral") && right.getKind().equals("IntLiteral")){
            constantFold.foldIntBinaryOp(left, right, node);
            changed = true;
            return "changed";
        }
        else if(left.getKind().equals("BoolLiteral") && right.getKind().equals("BoolLiteral")){
            constantFold.foldBoolBinaryOp(left, right, node);
            changed = true;
            return "changed";
        }
        return "";
    }

    private String idVisitor(JmmNode node, HashMap<String, JmmNode> map) {
        JmmNode constant = map.get(node.get("name"));
        if (constant != null) {
            JmmNode newNode = new JmmNodeImpl(constant.getKind());
            newNode.put("var", constant.get("var"));
            ConstantFold.replaceNode(node, newNode);
            changed = true;
            return "changed";
        }
        return "";
    }

    private String notVisitor(JmmNode node, HashMap<String, JmmNode> map){
        JmmNode child = node.getJmmChild(0);
        visit(child, map);
        ConstantFold constantFold = new ConstantFold();
        if(child.getKind().equals("BoolLiteral")){
            constantFold.foldNot(child, node);
            changed = true;
            return "changed";
        }

        if (child.getKind().equals("BinaryOp")){
            String op = child.get("op");
            switch (op){
                case "<" -> child.put("op", ">=");
                case "<=" -> child.put("op", ">");
                default -> {
                    return binaryOpVisitor(child, map);
                }
            }
            ConstantFold.replaceNode(node, child);
            changed = true;
            return "changed";
        }
        if (child.getKind().equals("Not")){
            ConstantFold.replaceNode(node, child.getJmmChild(0));
            changed = true;
            return "changed";
        }
        return "";
    }
}
