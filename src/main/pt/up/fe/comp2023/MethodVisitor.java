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

        parameters.put(methodName, new ArrayList<Symbol>());

        if (!methodName.equals("main")) {

            boolean retIsArray = jmmNode.getChildren().get(0).get("isArray").equals("true");
            String retType = jmmNode.getChildren().get(0).get("typeName");
            returnTypes.put(methodName, new Type(retType, retIsArray));

            List<String> methodParameters = (List<String>) jmmNode.getObject("parameters");

            for (int i = 1; i < jmmNode.getChildren().size(); i++) {

                if (!jmmNode.getChildren().get(i).getKind().equals("Type")) {
                    continue;
                }

                String type = jmmNode.getChildren().get(i).get("typeName");

                boolean isArray = jmmNode.getChildren().get(i).get("isArray").equals("true");

                Type typeObject = new Type(type, isArray);

                Symbol symbol = new Symbol(typeObject, methodParameters.get(i-1));

                parameters.get(methodName).add(symbol);
            }

        }
        else {
            String methodParameters = jmmNode.get("args");
            parameters.get(methodName).add(new Symbol(new Type("String", true), methodParameters));
            returnTypes.put(methodName, new Type("void", false));
        }


        // get the local variables
        return null;
    }

    public List<String> getMethods() {
        return methods;
    }

    Map<String, List<Symbol>> getMethodsParams() {
        return parameters;
    }

    Map<String, Type> getMethodsReturnTypes() {
        return returnTypes;
    }
}
