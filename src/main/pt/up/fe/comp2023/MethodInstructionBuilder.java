package pt.up.fe.comp2023;

import org.specs.comp.ollir.*;
import pt.up.fe.comp2023.analyser.BinaryOpCheck;
import pt.up.fe.specs.util.exceptions.NotImplementedException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import static java.lang.Integer.parseInt;

public class MethodInstructionBuilder {
    private final Method method;
    private final String superClass;
    private final LabelController labelController;

    public MethodInstructionBuilder(Method method, String superClass, LabelController labelController) {
        this.method = method;
        this.superClass = superClass;
        this.labelController = labelController;
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
            case BRANCH -> jasminCode.append(buildCondBranchInstruction((CondBranchInstruction) instruction));
            case GOTO -> jasminCode.append(buildGotoInstruction((GotoInstruction) instruction));
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
                    int val = JasminUtils.getValue(secondOperand, valueSign);
                    if(Objects.equals(firstOp.getName(), LHSop.getName()) && val <= 127 && val >= -128){
                        return JasminInstruction.instIinc(register, val);
                    }
                }
                else if(firstOperand.isLiteral() && !secondOperand.isLiteral()) {
                    // a = 1 + a
                    Operand secondOp = (Operand) secondOperand;
                    Operand LHSop = (Operand) LHS;
                    int val = JasminUtils.getValue(firstOperand, valueSign);
                    if (Objects.equals(secondOp.getName(), LHSop.getName()) && val <= 127 && val >= -128) {
                        return JasminInstruction.instIinc(register, val);
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

        if(instruction.getInvocationType() == CallType.NEW){
            ElementType type = instruction.getReturnType().getTypeOfElement();
            if (type == ElementType.ARRAYREF){
                jasminCode.append(loadElement(instruction.getListOfOperands().get(0)));
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
            jasminCode.append(JasminInstruction.instInvokestatic(className, methodName, retType, paramsTypes.toString(), params.size()));

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
                    jasminCode.append(JasminInstruction.instInvokespecial(className, methodName, retType, paramsTypes.toString(), params.size()));
                }
                else{
                    jasminCode.append(JasminInstruction.instInvokevirtual(className, methodName, retType, paramsTypes.toString(), params.size()));
                }
            }
        }
        else if(instruction.getInvocationType() == CallType.arraylength){
            return loadElement(instruction.getFirstArg()) + JasminInstruction.instArraylength();
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

    private String buildBinaryOpIf(Element leftOperand, Element rightOperand, OperationType type){
        StringBuilder jasminCode = new StringBuilder();

        jasminCode.append(loadElement(leftOperand));

        String label1;
        String label2;

        if(type == OperationType.LTH){
            label1 = "LTH_" + labelController.next();
            label2 = "LTH_" + labelController.next();

            if (rightOperand.isLiteral() && ((LiteralElement) rightOperand).getLiteral().equals("0")) {
                jasminCode.append(JasminInstruction.iflt(label1));
            } else {
                jasminCode.append(loadElement(rightOperand));
                jasminCode.append(JasminInstruction.if_icmplt(label1));
            }
        }
        else{
            label1 = "GTE_" + labelController.next();
            label2 = "GTE_" + labelController.next();

            if (rightOperand.isLiteral() && ((LiteralElement) rightOperand).getLiteral().equals("0")) {
                jasminCode.append(JasminInstruction.ifge(label1));
            } else {
                jasminCode.append(loadElement(rightOperand));
                jasminCode.append(JasminInstruction.if_icmpge(label1));
            }
        }

        jasminCode.append(JasminInstruction.instIconst(0))
                .append(JasminInstruction.instGoto(label2))
                .append(label1).append(":\n")
                .append(JasminInstruction.instIconst(1))
                .append(label2).append(":\n");


        return jasminCode.toString();
    }

    public String buildBinaryOpInstruction(BinaryOpInstruction instruction){
        StringBuilder jasminCode = new StringBuilder();
        Element leftOperand = instruction.getLeftOperand();
        Element rightOperand = instruction.getRightOperand();
        OperationType opType = instruction.getOperation().getOpType();

        jasminCode.append(loadElement(leftOperand));
        jasminCode.append(loadElement(rightOperand));
        if(opType == OperationType.LTH || opType == OperationType.GTE){
            jasminCode.append(buildBinaryOpIf(leftOperand, rightOperand, opType));
        }
        else{
            jasminCode.append(JasminInstruction.instArithOp(opType));
        }

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

    private String ifCond(Instruction cond, String label){
        return buildInstruction(cond) + JasminInstruction.ifne(label);
    }

    private String binaryIfCond(BinaryOpInstruction cond, String label){
        StringBuilder jasminCode = new StringBuilder();
        Element leftOperand = cond.getLeftOperand();
        Element rightOperand = cond.getRightOperand();
        OperationType opType = cond.getOperation().getOpType();

        if (opType == OperationType.LTH || opType == OperationType.GTE) {
            String comparison = "";
            jasminCode.append(loadElement(leftOperand));
            if (rightOperand.isLiteral() && ((LiteralElement) rightOperand).getLiteral().equals("0")) {
                if(opType == OperationType.LTH){
                    comparison = JasminInstruction.iflt(label);
                }
                else{
                    comparison = JasminInstruction.ifge(label);
                }
            } else {
                jasminCode.append(loadElement(rightOperand));
                if(opType == OperationType.LTH){
                    comparison = JasminInstruction.if_icmplt(label);
                }
                else{
                    comparison = JasminInstruction.if_icmpge(label);
                }
            }
            jasminCode.append(comparison);
        } else {
            jasminCode.append(ifCond(cond, label));
        }

        return jasminCode.toString();
    }

    public String buildCondBranchInstruction(CondBranchInstruction instruction){
        StringBuilder jasminCode = new StringBuilder();
        Instruction cond = instruction.getCondition();
        String label = instruction.getLabel();
        if(cond.getInstType() == InstructionType.BINARYOPER){
            jasminCode.append(binaryIfCond((BinaryOpInstruction) cond, label));
        }
        else{
            jasminCode.append(ifCond(cond, label));
        }
        return jasminCode.toString();
    }

    public String buildGotoInstruction(GotoInstruction instruction){
        return JasminInstruction.instGoto(instruction.getLabel());
    }

}
