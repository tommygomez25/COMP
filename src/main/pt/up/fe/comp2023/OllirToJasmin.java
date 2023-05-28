package pt.up.fe.comp2023;

import org.specs.comp.ollir.*;

import java.util.ArrayList;

public class OllirToJasmin {

    private final ClassUnit classUnit;
    public String superClass;

    private final LabelController labelController = new LabelController();

    public OllirToJasmin(ClassUnit classUnit) {
        this.classUnit = classUnit;
        this.classUnit.buildVarTables();
    }

    public String createJasmin(){
        StringBuilder jasminCode = new StringBuilder();

        // Class Name
        jasminCode.append(".class public ").append(classUnit.getClassName()).append("\n");

        // Super Class Name
        this.superClass = classUnit.getSuperClass();
        this.superClass = this.superClass == null ? "java/lang/Object" : this.superClass;
        jasminCode.append(".super ").append(this.superClass).append("\n\n");

        // Fields
        for(Field field : classUnit.getFields()){
            jasminCode.append(buildField(field)).append("\n");
        }
        jasminCode.append("\n");

        // Methods
        for(Method method : classUnit.getMethods()){
            jasminCode.append(buildMethod(method)).append("\n");
        }

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

        jasminCode.append(field.getFieldName()).append(" ").append(JasminUtils.getJasminType(field.getFieldType(), this.classUnit)).append("\n");

        return jasminCode.toString();
    }

    public String buildMethod(Method method){
        StringBuilder jasminCode = new StringBuilder();

        jasminCode.append(".method ");
        AccessModifiers accessModifier = method.getMethodAccessModifier();
        if(accessModifier != AccessModifiers.DEFAULT){
            jasminCode.append(accessModifier.name().toLowerCase()).append(" ");
            if(method.isStaticMethod())
                jasminCode.append("static ");
            if(method.isFinalMethod())
                jasminCode.append("final ");
            jasminCode.append(method.getMethodName()).append("(");
        }
        else{
            jasminCode.append("public <init>(");
        }


        String params = method.getParams().stream()
                .map(param -> JasminUtils.getJasminType(param.getType(), this.classUnit))
                .reduce("", (a, b) -> a + b);

        jasminCode.append(params).append(")");

        jasminCode.append(JasminUtils.getJasminType(method.getReturnType(), this.classUnit)).append("\n");

        MethodInstructionBuilder methodInstructionBuilder = new MethodInstructionBuilder(method, this.superClass, this.labelController);

        JasminInstruction.limitController.resetStack();
        JasminInstruction.limitController.updateRegistersUpTo(method.getParams().size() + 1);

        ArrayList<Instruction> instructions = method.getInstructions();

        StringBuilder methodCode = new StringBuilder();

        for(Instruction instruction : instructions){

            for(String label: method.getLabels().keySet()){
                if(method.getLabels().get(label).equals(instruction)){
                    methodCode.append(label).append(":\n");
                }
            }

            methodCode.append(methodInstructionBuilder.buildInstruction(instruction));
            if(instruction.getInstType() == InstructionType.CALL){
                ElementType retType = ((CallInstruction) instruction).getReturnType().getTypeOfElement();
                CallType callType = ((CallInstruction) instruction).getInvocationType();
                if(!method.isConstructMethod() && (retType != ElementType.VOID || callType == CallType.invokespecial)){
                    methodCode.append(JasminInstruction.instPop());
                }
            }
        }

        // method body
        if(accessModifier != AccessModifiers.DEFAULT) {
            jasminCode.append("\t.limit stack ").append(JasminInstruction.limitController.getMaxStackSize()).append("\n");
            jasminCode.append("\t.limit locals ").append(JasminInstruction.limitController.getLocalLimit()).append("\n");
        }

        jasminCode.append(methodCode.toString());

        jasminCode.append(".end method\n");

        return jasminCode.toString();
    }
}
