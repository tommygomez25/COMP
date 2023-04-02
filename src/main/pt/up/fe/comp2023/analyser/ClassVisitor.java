package pt.up.fe.comp2023.analyser;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.ArrayList;
import java.util.List;

public class ClassVisitor extends AJmmVisitor<Void, Void> {

    private String className;
    private String superClassName;
    private List<Symbol> fields = new ArrayList<>();
    protected void buildVisitor(){
        addVisit("Program", this::dealWithProgram);
        addVisit("Class", this::dealWithClass);
    }

    private Void dealWithProgram(JmmNode jmmNode,Void v1){
        for(JmmNode child: jmmNode.getChildren()){
            if(child.getKind().equals("Class")){
                visit(child, null);
                return null;
            }
        }
        return null;
    }

    private Void dealWithClass(JmmNode jmmNode, Void v){
        this.className = jmmNode.get("className");
        if(jmmNode.getAttributes().contains("extendName")){
            this.superClassName = jmmNode.get("extendName");
        }


        for(JmmNode child: jmmNode.getChildren()){
            if(child.getKind().equals("Field")){
                String fieldName = child.get("fieldName");
                boolean isArray = child.getChildren().get(0).get("isArray").equals("true");
                String t = child.getChildren().get(0).get("typeName");
                Type type = new Type(t, isArray);
                fields.add(new Symbol(type, fieldName));
            }
        }
        return null;
    }

    public String getClassName() {
        return className;
    }

    public String getSuperClassName() {
        return superClassName;
    }

    public List<Symbol> getFields() {
        return fields;
    }

}
