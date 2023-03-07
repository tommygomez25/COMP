package pt.up.fe.comp2023;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsLogs;
import pt.up.fe.specs.util.SpecsSystem;

public class Launcher {

    public static void main(String[] args) {
        // Setups console logging and other things
        SpecsSystem.programStandardInit();

        // Parse arguments as a map with predefined options
        var config = parseArgs(args);

        // Get input file
        File inputFile = new File(config.get("inputFile"));

        // Check if file exists
        if (!inputFile.isFile()) {
            throw new RuntimeException("Expected a path to an existing input file, got '" + inputFile + "'.");
        }

        // Read contents of input file
        String code = SpecsIo.read(inputFile);

        // Instantiate JmmParser
        SimpleParser parser = new SimpleParser();

        // Parse stage
        JmmParserResult parserResult = parser.parse(code, config);

        if(parserResult.getReports().size() > 0){
            System.out.println("Parsing errors:");
            for (var report : parserResult.getReports()) {
                System.out.println(report);
            }
            return;
        }

        System.out.println(parserResult.getRootNode().toTree());

        // Check if there are parsing errors
        TestUtils.noErrors(parserResult.getReports());

        // instantiate mySymbolTable
        MySymbolTable mySymbolTable = new MySymbolTable();
        ClassVisitor classVisitor = new ClassVisitor();
        classVisitor.visit(parserResult.getRootNode());
        mySymbolTable.setClassName(classVisitor.getClassName());
        mySymbolTable.setSuperName(classVisitor.getSuperClassName());
        mySymbolTable.setFields(classVisitor.getFields());
        System.out.println("Classes: ");
        System.out.println(mySymbolTable.getClassName());
        System.out.println(mySymbolTable.getSuper());
        System.out.println("Fields: ");
        for (Symbol field : mySymbolTable.getFields()) {
            System.out.println(field.getType() + " Name " + field.getName());
        }

        ImportVisitor importVisitor = new ImportVisitor();
        importVisitor.visit(parserResult.getRootNode());
        mySymbolTable.setImports(importVisitor.getImports());
        System.out.println("Imports: ");
        System.out.println(mySymbolTable.getImports());

        MethodVisitor methodVisitor = new MethodVisitor();
        methodVisitor.visit(parserResult.getRootNode());
        mySymbolTable.setMethods(methodVisitor.getMethods());
        System.out.println("Methods: ");
        System.out.println(mySymbolTable.getMethods());


    }

    private static Map<String, String> parseArgs(String[] args) {
        SpecsLogs.info("Executing with args: " + Arrays.toString(args));

        // Check if there is at least one argument
        if (args.length != 1) {
            throw new RuntimeException("Expected a single argument, a path to an existing input file.");
        }

        // Create config
        Map<String, String> config = new HashMap<>();
        config.put("inputFile", args[0]);
        config.put("optimize", "false");
        config.put("registerAllocation", "-1");
        config.put("debug", "false");

        return config;
    }

}
