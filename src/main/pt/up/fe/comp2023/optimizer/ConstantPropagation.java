package pt.up.fe.comp2023.optimizer;

import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.JmmNodeImpl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class ConstantPropagation extends AJmmVisitor<HashMap<String, JmmNode>, String> {

    private boolean changed = false;
    private final boolean simpleWhile;
    public ConstantPropagation() {
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
        addVisit("IfElse", this::ifElseVisitor);
        addVisit("While", this::whileVisitor);

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
        JmmNode expression = node.getJmmChild(0);
        if (node.getKind().equals("Accessors")) {
            visit(node, map);
            return "";
        }
        if (expression.getKind().equals("IntLiteral") || expression.getKind().equals("BoolLiteral")) {
            map.put(node.get("varName"), expression);
        } else {
            visit(expression, map);
            map.remove(node.get("varName"));
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
        return leftVal;
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

    private String ifElseVisitor(JmmNode node, HashMap<String, JmmNode> map){
        JmmNode cond = node.getJmmChild(0);
        JmmNode thenStat = node.getJmmChild(1);
        JmmNode elseStat = node.getJmmChild(2);
        visit(cond, map);

        if(cond.getKind().equals("BoolLiteral")){
            JmmNode nextExpression = cond.get("var").equals("1") ? thenStat : elseStat;
            visit(nextExpression, map);
            // fold if
            ConstantFold.replaceNode(node, nextExpression);
            
            changed = true;
            return "";
        }

        HashMap<String, JmmNode> thenMap = new HashMap<>(map);
        HashMap<String, JmmNode> elseMap = new HashMap<>(map);

        visit(thenStat, thenMap);
        visit(elseStat, elseMap);

        updateMap(map, thenMap);
        updateMap(map, elseMap);

        return "";
    }

    private String whileVisitor(JmmNode jmmNode, HashMap<String, JmmNode> map) {
        JmmNode whileCond = jmmNode.getJmmChild(0);
        JmmNode whileBody = jmmNode.getJmmChild(1);
        HashMap<String, JmmNode> newMap = new HashMap<>();
        HashMap<String, JmmNode> copyMap = new HashMap<>(map);

        if (simpleWhile){
            JmmNode simpleCond = getSimpleCond(jmmNode, copyMap);

            ConstantFold constantFold = new ConstantFold();

            if(simpleCond.getKind().equals("BoolLiteral")){
                if(simpleCond.get("var").equals("0")) {
                    constantFold.foldConstantWhile(jmmNode);
                }
                else{
                    jmmNode.put("dowhile","1");
                }
            }

            for(JmmNode child : whileCond.getChildren()) {
                child.setParent(whileCond);
            }
        }
        else{
            visit(whileCond, newMap);
        }

        getConstMap(whileBody, copyMap);
        visit(whileBody, copyMap);
        updateMap(map, copyMap);

        return "";
    }

    private JmmNode copy(JmmNode jmmNode, JmmNode parent) {
        JmmNode copy = new JmmNodeImpl(jmmNode.getKind());

        for (String attr: jmmNode.getAttributes()) {
            copy.put(attr, jmmNode.get(attr));
        }

        for (int i=0; i < jmmNode.getChildren().size(); i++) {
            JmmNode child = jmmNode.getJmmChild(i);
            JmmNode copyChild = copy(child, copy);
            copy.add(copyChild, i);
            child.setParent(copy);
        }
        copy.setParent(parent);

        return copy;
    }

    private JmmNode getSimpleCond(JmmNode node, HashMap<String, JmmNode> map){
        JmmNode whileCopy = copy(node, node.getJmmParent());
        JmmNode whileCond = whileCopy.getJmmChild(0);
        String changed;
        do{
            changed = visit(whileCond, map);
            whileCopy = copy(whileCopy, node.getJmmParent());
            whileCond = whileCopy.getJmmChild(0);
        }while(changed.equals("changed") && !whileCond.getKind().equals("BoolLiteral"));
        return whileCond;
    }

    private void getConstMap(JmmNode node, HashMap<String, JmmNode> map){
        if(node.getKind().equals("Assignment") && node.getJmmChild(0).getAttributes().contains("name")){
            String name = node.getJmmChild(0).get("name");
            map.remove(name);
        }

        for(JmmNode child : node.getChildren()){
            getConstMap(child, map);
        }
    }
}
