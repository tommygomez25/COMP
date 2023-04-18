package pt.up.fe.comp2023.analyser;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class MySymbolTable implements SymbolTable {
    private String className = null;
    private String superName = null;
    private List<String> imports = new ArrayList<>();
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

    public void addImport(String imp) {
        imports.add(imp);
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
    public void addClass(String className, String superClassName) {
        this.className = className;
        this.superName = superClassName;
    }

    public void addMethod(String name, Type type, List<Symbol> parameters, List<Symbol> localVariables) {
        methods.add(name);
        methodReturnType.put(name, type);
        methodParameters.put(name, parameters);
        methodLocalVariables.put(name, localVariables);
    }

    public void addField(Symbol symbol) {
        fields.add(symbol);
    }

    public boolean containsFieldInClass(String name, String className) {
        for (Symbol field : fields) {
            if (field.getName().equals(name)) {
                if (field.getType().getName().equals(className)) {
                    return true;
                }
            }
        }
        return false;
    }


    public boolean containsMethod(String methodName,Type returnType, List<Symbol> parameters) {
        for (String method : methods) {
            if (method.equals(methodName)) {
                if (methodReturnType.get(method).equals(returnType)) {
                    if (methodParameters.get(method).equals(parameters)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean isVarDeclared(String varName) {
        for (Symbol field : fields) {
            if (field.getName().equals(varName)) {
                return true;
            }
        }
        for (String method : methods) {
            for (Symbol parameter : methodParameters.get(method)) {
                if (parameter.getName().equals(varName)) {
                    return true;
                }
            }
            for (Symbol localVariable : methodLocalVariables.get(method)) {
                if (localVariable.getName().equals(varName)) {
                    return true;
                }
            }
        }

        // check if varName is in the imports
        for (String imp : imports) {
            if (imp.endsWith(varName)) {
                return true;
            }
        }
        return false;
    }

    public boolean isMathExpression(String kind) {
        return kind.equals("BinaryOp");
    }

    public boolean isBooleanExpression(String kind) {
        return kind.equals("BooleanOp");
    }

    public Symbol findField(String name){
        for(Symbol field : fields){
            if(field.getName().equals(name)){
                return field;
            }
        }

        // iterate method local variables
        for(String method : methods){
            for(Symbol localVariable : methodLocalVariables.get(method)){
                if(localVariable.getName().equals(name)){
                    return localVariable;
                }
            }
        }

        // iterate method parameters
        for(String method : methods){
            for(Symbol parameter : methodParameters.get(method)){
                if(parameter.getName().equals(name)){
                    return parameter;
                }
            }
        }

        // check if it is in imports
        for (String imp : imports) {
            if (imp.endsWith(name)) {
                return new Symbol(new Type(name,false),name);
            }
        }

        return new Symbol(new Type("unknown",false),name);
    }

    public Type getVarType(String varName) {
        // check if it is in imports
        for (String imp : imports) {
            if (imp.endsWith(varName)) {
                return new Type(varName,false);
            }
        }

        for (Symbol field : fields) {
            if (field.getName().equals(varName)) {
                return field.getType();
            }
        }
        for (String method : methods) {
            for (Symbol parameter : methodParameters.get(method)) {
                if (parameter.getName().equals(varName)) {
                    return parameter.getType();
                }
            }
            for (Symbol localVariable : methodLocalVariables.get(method)) {
                if (localVariable.getName().equals(varName)) {
                    return localVariable.getType();
                }
            }
        }

        return new Type("unknown",false);
    }

    public boolean isClassImported(String className){
        for(String imp : imports){
            if(imp.endsWith(className)){
                return true;
            }
        }
        return false;
    }

    public boolean isVarClass(String varName) {
        // check if varName is in the imports
        for (String imp : imports) {
            if (imp.endsWith(varName)) {
                return true;
            }
        }

        // check if varName is declared in the class
        for (Symbol field : fields) {
            if (field.getName().equals(varName)) {
                if (Character.isUpperCase(field.getType().getName().charAt(0)))
                    return true;
            }
        }

        // check if varName is a method parameter
        for (String method : methods) {
            for (Symbol parameter : methodParameters.get(method)) {
                if (parameter.getName().equals(varName)) {
                    if (Character.isUpperCase(parameter.getType().getName().charAt(0)))
                        return true;
                }
            }

            for (Symbol localVariable : methodLocalVariables.get(method)) {
                if (localVariable.getName().equals(varName)) {
                    if (Character.isUpperCase(localVariable.getType().getName().charAt(0)))
                        return true;
                }
            }
        }

        return false;
    }

}

