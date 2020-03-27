package gspd.ispd.fxgui.workload.dag.icons;

import gspd.ispd.fxgui.commons.Icon;
import gspd.ispd.commons.ISPDType;
import gspd.ispd.fxgui.commons.IconEditor;
import gspd.ispd.fxgui.commons.NodeIcon;
import gspd.ispd.fxgui.workload.dag.editor.SwitchEditor;
import gspd.ispd.fxgui.workload.dag.shapes.SwitchShape;
import javafx.scene.paint.Color;
import javafx.util.Builder;

public class SwitchIcon extends NodeIcon {

    public static final ISPDType SWITCH_TYPE = ISPDType.type(NODE_TYPE, "SWITCH_TYPE");

    ///////////////////////////////////////
    /////////// CONSTRUCTOR ///////////////
    ///////////////////////////////////////

    public SwitchIcon(boolean selected, double centerX, double centerY) {
        super(SwitchShape::new, selected, centerX, centerY);

        setType(SWITCH_TYPE);
    }

    public SwitchIcon(double centerX, double centerY) {
        this(false, centerX, centerY);
    }

    public SwitchIcon(boolean selected) {
        this(selected, 0.0, 0.0);
    }

    public SwitchIcon() {
        this(false, 0.0, 0.0);
    }

    /////////////////////////////////////////
    ///////////// OVERRIDE //////////////////
    /////////////////////////////////////////

    private static final Builder<SwitchIcon> SWITCH_BUILDER = SwitchIcon::new;
    @Override
    public Builder<? extends Icon> iconBuilder() {
        return SWITCH_BUILDER;
    }

    private static final SwitchEditor SWITCH_EDITOR = new SwitchEditor();
    @Override
    protected IconEditor editor() {
        SWITCH_EDITOR.setIcon(this);
        return SWITCH_EDITOR;
    }

    @Override
    protected void updateIcon() {
        SwitchShape shape = (SwitchShape) getContent();
        if (isSelected()) {
            shape.setFill(Color.MEDIUMBLUE);
            shape.setStroke(Color.DARKBLUE);
        } else {
            shape.setFill(Color.BLACK);
            shape.setStroke(Color.BLACK);
        }
    }
}
