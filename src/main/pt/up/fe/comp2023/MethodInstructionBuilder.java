package pt.up.fe.comp2023;

import org.specs.comp.ollir.*;
import pt.up.fe.specs.util.exceptions.NotImplementedException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static java.lang.Integer.parseInt;

public class MethodInstructionBuilder {
    private final Method method;
    private final String superClass;

    public MethodInstructionBuilder(Method method, String superClass) {
        this.method = method;
        this.superClass = superClass;
    }

    public String buildInstruction(Instruction instruction) {
        StringBuilder jasminCode = new StringBuilder();
        switch (instruction.getInstType()) {
            case ASSIGN -> jasminCode.append(buildAssignInstruction((AssignInstruction) instruction));
            case RETURN -> jasminCode.append(buildReturnInstruction((ReturnInstruction) instruction));
            case CALL -> jasminCode.append(buildCallInstruction((CallInstruction) instruction));
            case PUTFIELD -> jasminCode.append(buildPutFieldInstruction((PutFieldInstruction) instruction));
            case GETFIELD -> jasminCode.append(buildGetFieldInstruction((GetFieldInstruction) instruction));
            case NOPER -> jasminCode.append(buildSingleOpInstruction((SingleOpInstruction) instruction));
            case UNARYOPER -> jasminCode.append(buildUnaryOpInstruction((UnaryOpInstruction) instruction));
            case BINARYOPER -> jasminCode.append(buildBinaryOpInstruction((BinaryOpInstruction) instruction));
        }
        return jasminCode.toString();
    }

    public String buildAssignInstruction(AssignInstruction instruction) {
        StringBuilder jasminCode = new StringBuilder();
        Element LHS = instruction.getDest();
        Instruction RHS = instruction.getRhs();

        if (RHS.getInstType() == InstructionType.BINARYOPER){
            BinaryOpInstruction binaryOp = (BinaryOpInstruction) RHS;
            OperationType op = binaryOp.getOperation().getOpType();
            if(op == OperationType.ADD || op == OperationType.SUB || op == OperationType.MUL || op == OperationType.DIV){
                String valueSign = "";
                if(op == OperationType.SUB){
                    valueSign = "-";
                }
                int register = getRegister(LHS);
                Element firstOperand = binaryOp.getLeftOperand();
                Element secondOperand = binaryOp.getRightOperand();
                if(!firstOperand.isLiteral() && secondOperand.isLiteral()){
                    // a = a + 1
                    Operand firstOp = (Operand) firstOperand;
                    Operand LHSop = (Operand) LHS;
                    if(Objects.equals(firstOp.getName(), LHSop.getName())){
                        String value = valueSign + ((LiteralElement) secondOperand).getLiteral();
                        return JasminInstruction.instIinc(register, value);
                    }
                }
                else if(firstOperand.isLiteral() && !secondOperand.isLiteral()) {
                    // a = 1 + a
                    Operand secondOp = (Operand) secondOperand;
                    Operand LHSop = (Operand) LHS;
                    if (Objects.equals(secondOp.getName(), LHSop.getName())) {
                        String value = valueSign + ((LiteralElement) firstOperand).getLiteral();
                        return JasminInstruction.instIinc(register, value);
                    }
                }

            }
        }
        String RHSStr = buildInstruction(RHS);
        String result = storeElement(LHS, RHSStr);
        jasminCode.append(result);

        return jasminCode.toString();
    }

    public String buildReturnInstruction(ReturnInstruction instruction) {
        StringBuilder jasminCode = new StringBuilder();
        if(instruction.hasReturnValue()){
            Element element = instruction.getOperand();
            jasminCode.append(loadElement(element));
            ElementType type = element.getType().getTypeOfElement();
            switch (type) {
                case INT32, BOOLEAN -> jasminCode.append(JasminInstruction.instIreturn());
                case ARRAYREF, OBJECTREF, THIS, STRING -> jasminCode.append(JasminInstruction.instAreturn());
                default -> throw new NotImplementedException("Type not implemented: " + type);
            }
        }
        else{
            return "return\n";
        }
        return jasminCode.toString();
    }

    public String buildCallInstruction(CallInstruction instruction){
        StringBuilder jasminCode = new StringBuilder();



        if(instruction.getInvocationType() == CallType.arraylength){
            jasminCode.append(instruction.getFirstArg()).append(JasminInstruction.instArraylength());
        }
        else if(instruction.getInvocationType() == CallType.NEW){
            ElementType type = instruction.getReturnType().getTypeOfElement();
            if (type == ElementType.ARRAYREF){
                jasminCode.append(loadElement(instruction.getFirstArg()));
                jasminCode.append(JasminInstruction.instNewArray());
            }
            else{
                String returnType = ((ClassType) instruction.getReturnType()).getName();
                jasminCode.append(JasminInstruction.instNew(returnType)).append(JasminInstruction.instDup());
            }
        }
        else if(instruction.getInvocationType() == CallType.invokestatic){
            String retType = JasminUtils.getJasminType(instruction.getReturnType(), method.getOllirClass());
            ArrayList<Element> params = instruction.getListOfOperands();
            StringBuilder paramsTypes = new StringBuilder();
            LiteralElement litElem = (LiteralElement) instruction.getSecondArg();
            String methodName = litElem.getLiteral().replace("\"", "");
            Operand firstOp = (Operand) instruction.getFirstArg();
            for(Element param : params){
                jasminCode.append(loadElement(param));
                paramsTypes.append(JasminUtils.getJasminType(param.getType(), method.getOllirClass()));
            }
            String firstOpName = firstOp.getName();
            String className = JasminUtils.getQualifiedName(method.getOllirClass(), firstOpName);
            jasminCode.append(JasminInstruction.instInvokestatic(className, methodName, retType, paramsTypes.toString()));

        }
        else if(instruction.getInvocationType() == CallType.invokespecial || instruction.getInvocationType() == CallType.invokevirtual){
            if(method.isConstructMethod() && instruction.getFirstArg().getType().getTypeOfElement() == ElementType.THIS){
                jasminCode.append("\taload_0\n");
                jasminCode.append("\tinvokenonvirtual ").append(this.superClass).append("/<init>()V\n");
                jasminCode.append("\treturn\n");
            }
            else{
                String retType = JasminUtils.getJasminType(instruction.getReturnType(), method.getOllirClass());
                ArrayList<Element> params = instruction.getListOfOperands();
                StringBuilder paramsTypes = new StringBuilder();
                LiteralElement litElem = (LiteralElement) instruction.getSecondArg();
                String methodName = litElem.getLiteral().replace("\"", "");
                CallType callType = instruction.getInvocationType();
                jasminCode.append(loadElement(instruction.getFirstArg()));
                for(Element param : params){
                    jasminCode.append(loadElement(param));
                    paramsTypes.append(JasminUtils.getJasminType(param.getType(), method.getOllirClass()));
                }
                ClassType classType = (ClassType) instruction.getFirstArg().getType();
                String className = JasminUtils.getQualifiedName(method.getOllirClass(), classType.getName());
                if(callType == CallType.invokespecial){
                    jasminCode.append(JasminInstruction.instInvokespecial(className, methodName, retType, paramsTypes.toString()));
                }
                else{
                    jasminCode.append(JasminInstruction.instInvokevirtual(className, methodName, retType, paramsTypes.toString()));
                }
            }
        }
        else{
            throw new NotImplementedException("Call type not implemented: " + instruction.getInvocationType());
        }
        return jasminCode.toString();
    }

    public String buildPutFieldInstruction(PutFieldInstruction instruction){
        StringBuilder jasminCode = new StringBuilder();
        Element classElement = instruction.getFirstOperand();
        Element fieldElement = instruction.getSecondOperand();
        Element valueElement = instruction.getThirdOperand();
        jasminCode.append(loadElement(classElement));
        jasminCode.append(loadElement(valueElement));
        jasminCode.append(fieldOper(fieldElement, InstructionType.PUTFIELD, classElement));
        return jasminCode.toString();
    }

    public String buildGetFieldInstruction(GetFieldInstruction instruction){
        StringBuilder jasminCode = new StringBuilder();
        Element classElement = instruction.getFirstOperand();
        Element fieldElement = instruction.getSecondOperand();
        jasminCode.append(loadElement(classElement));
        jasminCode.append(fieldOper(fieldElement, InstructionType.GETFIELD, classElement));
        return jasminCode.toString();
    }

    public String buildSingleOpInstruction(SingleOpInstruction instruction){
        Element op = instruction.getSingleOperand();
        return loadElement(op);
    }

    public String buildUnaryOpInstruction(UnaryOpInstruction instruction){
        StringBuilder jasminCode = new StringBuilder();
        Element op = instruction.getOperand();
        OperationType opType = instruction.getOperation().getOpType();
        if(opType == OperationType.NOTB){
            jasminCode.append(JasminInstruction.instIconst(1));
            jasminCode.append(loadElement(op));
            jasminCode.append(JasminInstruction.instArithOp(OperationType.SUB));
        }
        return jasminCode.toString();
    }

    public String buildBinaryOpInstruction(BinaryOpInstruction instruction){
        StringBuilder jasminCode = new StringBuilder();
        Element leftOperand = instruction.getLeftOperand();
        Element rightOperand = instruction.getRightOperand();
        OperationType opType = instruction.getOperation().getOpType();

        jasminCode.append(loadElement(leftOperand));
        jasminCode.append(loadElement(rightOperand));
        jasminCode.append(JasminInstruction.instArithOp(opType));

        return jasminCode.toString();
    }

    public String fieldOper(Element fieldElement, InstructionType type, Element classElement){
        StringBuilder jasminCode = new StringBuilder();
        Operand fieldOp = (Operand) fieldElement;
        String fieldName = fieldOp.getName();
        String className = JasminUtils.getQualifiedName(method.getOllirClass(), ((ClassType) classElement.getType()).getName());
        String fieldType = JasminUtils.getJasminType(fieldElement.getType(), method.getOllirClass());
        if(type == InstructionType.GETFIELD){
            jasminCode.append(JasminInstruction.instGetfield(className, fieldName, fieldType));
        }
        else{
            jasminCode.append(JasminInstruction.instPutfield(className, fieldName, fieldType));
        }
        return jasminCode.toString();
    }

    private int getRegister(Element element) {
        HashMap<String, Descriptor> varTable = method.getVarTable();
        return varTable.get(((Operand) element).getName()).getVirtualReg();
    }

    private String getArray(Element element) {
        int elementRegister = getRegister(element);
        Element firstElement = ((ArrayOperand) element).getIndexOperands().get(0);
        int firstRegister = getRegister(firstElement);
        return JasminInstruction.instAload(elementRegister) + JasminInstruction.instIload(firstRegister);
    }

    private String storeElement(Element element, String value) {
        ElementType type = element.getType().getTypeOfElement();
        switch (type) {
            case INT32, BOOLEAN, STRING -> {
                int register = getRegister(element);
                Descriptor descriptor = method.getVarTable().get(((Operand) element).getName());
                ElementType varType = descriptor.getVarType().getTypeOfElement();
                if (varType == ElementType.ARRAYREF) {
                    return getArray(element) + value + JasminInstruction.instIastore();
                }
                return value + JasminInstruction.instIstore(register);
            }
            case ARRAYREF, OBJECTREF, THIS -> {
                int elementRegister = getRegister(element);
                return value + JasminInstruction.instAstore(elementRegister);
            }
            default -> throw new NotImplementedException("Type not implemented: " + type);
        }
    }

    private String loadElement(Element element){
        ElementType type = element.getType().getTypeOfElement();
        if(element.isLiteral()){
            return JasminInstruction.instIconst(parseInt(((LiteralElement) element).getLiteral()));
        }
        switch (type) {
            case INT32, BOOLEAN -> {
                int register = getRegister(element);
                Descriptor descriptor = method.getVarTable().get(((Operand) element).getName());
                ElementType varType = descriptor.getVarType().getTypeOfElement();
                if (varType == ElementType.ARRAYREF) {
                    return getArray(element) + JasminInstruction.instIaload();
                }
                return JasminInstruction.instIload(register);
            }
            case ARRAYREF, OBJECTREF, THIS, STRING -> {
                int elementRegister = getRegister(element);
                return JasminInstruction.instAload(elementRegister);
            }
            default -> throw new NotImplementedException("Type not implemented: " + type);
        }
    }

}
