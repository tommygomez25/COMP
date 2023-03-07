package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MethodVisitor extends AJmmVisitor<Void, Void> {
    List<String> methods = new ArrayList<String>();
    Map<String, Type> returnTypes = new HashMap<String, Type>();
    Map<String,List<Symbol>> localVariables = new HashMap<String, List<Symbol>>();
    Map<String,List<Symbol>> parameters = new HashMap<String, List<Symbol>>();

    @Override
    protected void buildVisitor(){
        addVisit("Program", this::dealWithProgram);
        addVisit("Class", this::dealWithClass);
        addVisit("Method", this::dealWithMethod);
    }

    private Void dealWithProgram(JmmNode jmmNode, Void v){
        for(JmmNode child: jmmNode.getChildren()){
            if(child.getKind().equals("Class")){
                visit(child, null);
                return null;
            }
        }
        return null;
    }

    private Void dealWithClass(JmmNode jmmNode, Void v){
        for(JmmNode child: jmmNode.getChildren()){
            if(child.getKind().equals("Method")){
                visit(child, null);
            }
        }
        return null;
    }

    private Void dealWithMethod(JmmNode jmmNode, Void v){
        String methodName = jmmNode.get("methodName");
        methods.add(methodName);

        //String returnType = jmmNode.get("returnType");
        return null;
    }

    public List<String> getMethods() {
        return methods;
    }

}
