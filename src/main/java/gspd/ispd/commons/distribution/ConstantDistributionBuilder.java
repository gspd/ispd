package gspd.ispd.commons.distribution;

public class ConstantDistributionBuilder extends AbstractDistributionBuilder {
    private double constant;

    public ConstantDistributionBuilder(double constant) {
        this.constant = constant;
    }

    public ConstantDistributionBuilder() {
        this(0);
    }

    public double getConstant() {
        return constant;
    }

    public void setConstant(double constant) {
        this.constant = constant;
    }

    @Override
    public Distribution build() {
        return new Distribution() {
            @Override
            public double random() {
                return constant;
            }
        };
    }
}