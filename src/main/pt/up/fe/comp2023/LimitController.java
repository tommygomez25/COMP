package pt.up.fe.comp2023;

import java.util.HashSet;
import java.util.Set;

public class LimitController {
    private Set<Integer> registers;
    private int maxStackSize;
    private int currentStackSize;
    private int errorMargin;

    LimitController() {
        this.registers = new HashSet<>();
        this.maxStackSize = 0;
        this.currentStackSize = 0;
        this.errorMargin = 2;
    }

    public void updateRegistersUpTo(int numRegisters) {
        for (int i = 0; i < numRegisters; i++) {
            registers.add(i);
        }
    }

    public int getMaxStackSize() {
        return maxStackSize + errorMargin;
    }

    public int getLocalLimit() {
        return registers.size();
    }

    public void addRegister(int register) {
        registers.add(register);
    }

    public void updateStack(int diff) {
        currentStackSize += diff;
        if (currentStackSize > maxStackSize) {
            maxStackSize = currentStackSize;
        }
    }

    public void resetStack() {
        currentStackSize = 0;
        maxStackSize = 0;
        registers = new HashSet<>();
    }

}

