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
    Map<String, Type> method_returnType = new HashMap<String, Type>();
    Map<String,List<Symbol>> method_localVariables = new HashMap<String, List<Symbol>>();
    Map<String,List<Symbol>> method_parameters = new HashMap<String, List<Symbol>>();

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
        return method_returnType.get(methodSignature) ;
    }

    @Override
    public List<Symbol> getParameters(String methodSignature) {
        return method_parameters.get(methodSignature);
    }

    @Override
    public List<Symbol> getLocalVariables(String methodSignature) {
        return method_localVariables.get(methodSignature);
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

}

