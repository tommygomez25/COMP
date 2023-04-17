package pt.up.fe.comp2023;

import org.specs.comp.ollir.*;
import pt.up.fe.specs.util.exceptions.NotImplementedException;

import java.util.Objects;

public class MethodInstructionBuilder {
    private final Method method;

    public MethodInstructionBuilder(Method method) {
        this.method = method;
    }

    public String buildInstruction(Instruction instruction) {
        StringBuilder jasminCode = new StringBuilder();
        switch (instruction.getInstType()) {
            case ASSIGN -> jasminCode.append(buildAssignInstruction((AssignInstruction) instruction));
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
                    Operand firstOp = (Operand) firstOperand;
                    Operand LHSop = (Operand) LHS;
                    if(Objects.equals(firstOp.getName(), LHSop.getName())){
                        String value = valueSign + ((LiteralElement) secondOperand).getLiteral();
                        return JasminInstruction.instIinc(register, value);
                    }
                }
                else if(firstOperand.isLiteral() && !secondOperand.isLiteral()) {
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

    private int getRegister(Element element) {
        Descriptor descriptor = method.getVarTable().get(((Operand) element).getName());
        return descriptor.getVirtualReg();
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
                    return getArray(element) + value + JasminInstruction.instAstore(register);
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

}
