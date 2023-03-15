package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class MySymbolTable implements SymbolTable {
    private String className;
    private String superName;
    private List<String> imports;
    private List<Symbol> fields = new ArrayList<>();

    List<String> methods = new ArrayList<String>();
    Map<String, Type> methodReturnType = new HashMap<String, Type>();
    Map<String,List<Symbol>> methodLocalVariables = new HashMap<String, List<Symbol>>();
    Map<String,List<Symbol>> methodParameters = new HashMap<String, List<Symbol>>();

    @Override
    public String getClassName() {
        return className;
    }

    @Override
    public String getSuper() {
        return superName;
    }

    @Override
    public List<String> getImports() {
        return imports;
    }

    @Override
    public List<Symbol> getFields() {
        return fields;
    }

    @Override
    public List<String> getMethods() {
        return methods;
    }

    @Override
    public Type getReturnType(String methodSignature) {
        return methodReturnType.get(methodSignature) ;
    }

    @Override
    public List<Symbol> getParameters(String methodSignature) {
        return methodParameters.get(methodSignature);
    }

    @Override
    public List<Symbol> getLocalVariables(String methodSignature) {
        return methodLocalVariables.get(methodSignature);
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public void setSuperName(String superName) {
        this.superName = superName;
    }

    public void setImports(List<String> imports) {
        this.imports = imports;
    }

    public void setMethods(List<String> methods) {
        this.methods = methods;
    }

    public void setFields(List<Symbol> fields) {
        this.fields = fields;
    }

    public String print() {
        StringBuilder builder = new StringBuilder();
        builder.append("Imports: \n");
        for (String imp : imports) {
            builder.append("\t");
            builder.append(imp);
            builder.append("\n");
        }
        builder.append("Class: ");
        builder.append(className);
        if (superName != null) {
            builder.append(" extends ");
            builder.append(superName);
        }
        builder.append("\n");
        builder.append("Fields: \n");
        for (Symbol field : fields) {
            builder.append("\t");
            builder.append(field.getType().getName());
            if(field.getType().isArray()){
                builder.append("[]");
            }
            builder.append(" ");
            builder.append(field.getName());
            builder.append("\n");
        }
        builder.append("Methods: \n");
        for(String method : methods){
            builder.append("\t");
            builder.append("Method Name: ");
            builder.append(method);
            builder.append("\n");
            builder.append("\t\t");
            builder.append("Return Type: ");
            builder.append(methodReturnType.get(method).getName());
            if(methodReturnType.get(method).isArray()){
                builder.append("[]");
            }
            builder.append("\n");
            builder.append("\t\t");
            builder.append("Parameters: \n");
            for(Symbol parameter : methodParameters.get(method)){
                builder.append("\t\t\t");
                builder.append(parameter.getType().getName());
                if(parameter.getType().isArray()){ builder.append("[]"); }
                builder.append(" ");
                builder.append(parameter.getName());
                builder.append("\n");
            }
            builder.append("\t\t");
            builder.append("Local Variables: \n");
            for(Symbol localVariable : methodLocalVariables.get(method)){
                builder.append("\t\t\t");
                builder.append(localVariable.getType().getName());
                if(localVariable.getType().isArray()){
                    builder.append("[]");
                }
                builder.append(" ");
                builder.append(localVariable.getName());
                builder.append("\n");
            }

            builder.append("\n");
        }

        return builder.toString();
    }

    public void setMethodsReturnTypes(Map<String, Type> methodsReturnTypes) {
        this.methodReturnType = methodsReturnTypes;
    }

    public void setMethodsParameters(Map<String, List<Symbol>> methodParameters) {
        this.methodParameters = methodParameters;
    }

    public void setMethodsLocalVariables(Map<String, List<Symbol>> methodLocalVariables) {
        this.methodLocalVariables = methodLocalVariables;
    }


}

