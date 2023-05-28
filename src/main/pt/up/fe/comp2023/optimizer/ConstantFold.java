package pt.up.fe.comp2023.optimizer;

import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.JmmNodeImpl;

public class ConstantFold {

    public void foldIntBinaryOp(JmmNode left, JmmNode right, JmmNode node){
        int leftValue = Integer.parseInt(left.get("var"));
        int rightValue = Integer.parseInt(right.get("var"));
        int result = 0;
        switch (node.get("op")){
            case "+" -> result = leftValue + rightValue;
            case "-" -> result = leftValue - rightValue;
            case "*" -> result = leftValue * rightValue;
            case "/" -> result = leftValue / rightValue;
            case "<" -> result = leftValue < rightValue ? 1 : 0;
            case ">=" -> result = leftValue >= rightValue ? 1 : 0;
            default -> throw new IllegalStateException("Unexpected value: " + node.get("op"));
        }
        String kind = node.get("op").matches("<|>=") ? "BoolLiteral" : "IntLiteral";
        JmmNode newNode = new JmmNodeImpl(kind);
        newNode.put("var", String.valueOf(result));
        replaceNode(node, newNode);
    }

    public static void replaceNode (JmmNode oldNode, JmmNode newNode) {
        JmmNode parent = oldNode.getJmmParent();
        if (parent == null) {
            return;
        }
        int index = parent.getChildren().indexOf(oldNode);
        parent.removeJmmChild(oldNode);
        parent.add(newNode, index);
        newNode.setParent(parent);
    }

    public void foldBoolBinaryOp(JmmNode left, JmmNode right, JmmNode node){
        boolean leftValue = left.get("var").equals("1");
        boolean rightValue = right.get("var").equals("1");
        boolean result = false;
        switch (node.get("op")){
            case "&&" -> result = leftValue && rightValue;
            case "||" -> result = leftValue || rightValue;
            default -> throw new IllegalStateException("Unexpected value: " + node.get("op"));
        }

        JmmNode newNode = new JmmNodeImpl(left.getKind());
        newNode.put("var", result ? "1" : "0");
        replaceNode(node, newNode);
    }

    public void foldNot(JmmNode expr, JmmNode node){
        JmmNode newNode = new JmmNodeImpl(expr.getKind());
        newNode.put("var", expr.get("var").equals("1") ? "0" : "1");
        replaceNode(node, newNode);
    }


}
