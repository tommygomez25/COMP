package pt.up.fe.comp2023;

import org.specs.comp.ollir.ClassUnit;
import pt.up.fe.comp.jmm.jasmin.JasminBackend;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;

import java.util.Collections;

public class MyJasminBackend implements JasminBackend {

    @Override
    public JasminResult toJasmin(OllirResult ollirResult) {
        ClassUnit classUnit = ollirResult.getOllirClass();
        String jasminCode = new OllirToJasmin(classUnit).createJasmin();

        return new JasminResult(ollirResult, jasminCode, Collections.emptyList());
    }
}
