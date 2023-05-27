package pt.up.fe.comp2023;

public class LabelController {
    private int labelCounter;

    public LabelController() {
        this.labelCounter = 0;
    }

    public String getLabel() {
        return "label" + labelCounter++;
    }

    public String getLabel(int label) {
        return "label" + label;
    }

    public void reset() {
        this.labelCounter = 0;
    }

    public int next() {
        return labelCounter++;
    }
}
