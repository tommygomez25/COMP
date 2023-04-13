package pt.up.fe.comp2023;

import org.specs.comp.ollir.AccessModifiers;
import org.specs.comp.ollir.ClassUnit;
import org.specs.comp.ollir.Field;

public class OllirToJasmin {

    private final ClassUnit classUnit;

    public OllirToJasmin(ClassUnit classUnit) {
        this.classUnit = classUnit;
        this.classUnit.buildVarTables();
    }

    public String createJasmin(){
        StringBuilder jasminCode = new StringBuilder();

        // Class Name
        jasminCode.append(".class public ").append(classUnit.getClassName()).append("\n");

        // Super Class Name
        String superClass = classUnit.getSuperClass();
        superClass = superClass == null ? "java/lang/Object" : superClass;
        jasminCode.append(".super ").append(superClass).append("\n\n");

        // Fields
        for(Field field : classUnit.getFields()){
            jasminCode.append(buildField(field)).append("\n");
        }
        jasminCode.append("\n");

        // Init
        jasminCode.append(";\n");
        jasminCode.append("; standard initializer\n");
        jasminCode.append(";\n");
        jasminCode.append(".method public <init>()V\n");
        jasminCode.append("\taload_0\n");
        jasminCode.append("\tinvokenonvirtual ").append(superClass).append("/<init>()V\n");
        jasminCode.append("\treturn\n");
        jasminCode.append(".end method\n\n");

        // Methods
        return jasminCode.toString();
    }

    public String buildField(Field field){
        StringBuilder jasminCode = new StringBuilder();
        jasminCode.append(".field ");
        AccessModifiers accessModifier = field.getFieldAccessModifier();
        if(accessModifier != AccessModifiers.DEFAULT){
            jasminCode.append(accessModifier.name().toLowerCase()).append(" ");
        }
        if(field.isStaticField())
            jasminCode.append("static ");
        if(field.isFinalField())
            jasminCode.append("final ");

        jasminCode.append(field.getFieldName()).append(" ").append(JasminUtils.getJasminType(field.getFieldType())).append("\n");
        return jasminCode.toString();
    }
}
