package pt.up.fe.comp2023;

import org.specs.comp.ollir.ClassUnit;
import org.specs.comp.ollir.OllirErrorException;
import pt.up.fe.comp.jmm.jasmin.JasminBackend;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.Collections;

public class MyJasminBackend implements JasminBackend {

    @Override
    public JasminResult toJasmin(OllirResult ollirResult) {
        ClassUnit classUnit = ollirResult.getOllirClass();
        try{
            classUnit.buildVarTables();
            classUnit.buildCFGs();
            classUnit.checkMethodLabels();
            OllirToJasmin converter = new OllirToJasmin(classUnit);
            String jasminCode = converter.createJasmin();
            if(ollirResult.getConfig().getOrDefault("debug", "false").equals("true")){
                System.out.println(jasminCode);
            }
            System.out.println(jasminCode);
            return new JasminResult(ollirResult, jasminCode, Collections.emptyList());

        } catch (OllirErrorException e) {
            return new JasminResult(classUnit.getClassName(), null,
                    Collections.singletonList(Report.newError(Stage.GENERATION, -1, -1, e.getMessage(), e)));
        }


    }
}
