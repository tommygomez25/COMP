package pt.up.fe.comp2023.analyser;

import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.ArrayList;
import java.util.List;

public class ImportVisitor extends AJmmVisitor<Void, Void> {

        private List<String> imports = new ArrayList<String>();

        @Override
        protected void buildVisitor() {
            addVisit("Program", this::dealWithProgram);
            addVisit("Import", this::dealWithImport);
        }

        private Void dealWithProgram(JmmNode jmmNode, Void v) {
            for(JmmNode child: jmmNode.getChildren()){
                if(child.getKind().equals("Class")){
                    return null;
                }
                visit(child,null);
            }

            return null;
        }
        private Void dealWithImport(JmmNode jmmNode, Void v) {
            List<String> importValues = (List<String>) jmmNode.getObject("names");

            StringBuilder finalString = new StringBuilder();

            if(importValues != null) {
                finalString.append(importValues.get(0));
                for (int i = 1; i < importValues.size(); i++) {
                    finalString.append(".").append(importValues.get(i));
                }
            }
            imports.add(finalString.toString());
            return null;
        }

        public List<String> getImports() {
            return imports;
        }
}
