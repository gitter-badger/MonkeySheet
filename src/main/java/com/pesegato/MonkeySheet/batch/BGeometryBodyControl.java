package com.pesegato.MonkeySheet.batch;


import org.dyn4j.dynamics.Body;

public abstract class BGeometryBodyControl extends BGeometryControl {

    public float offsetX;
    public float offsetY;
    public float offsetAngle;
    protected Body body;

    protected BGeometryBodyControl(Body body, BGeometry bgeo, float duration) {
        super(bgeo, duration);
        this.body = body;
    }

    @Override
    protected void controlUpdate(float tpf) {
        if (mustInit) {
            binit();
            mustInit = false;
        }
        bupdate(tpf);
        bgeo.getTransform().setPosition(
                (float) body.getTransform().getTranslationX() + offsetX,
                (float) body.getTransform().getTranslationY() + offsetY);
        bgeo.getTransform().setLocalRotation((float) body.getTransform().getRotation() + offsetAngle);
        bgeo.applyTransform();
        duration -= tpf;
        if (duration < 0) {
            setEnabled(false);
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (!enabled) {
            bgeo.removeFromParent();
            spatial.removeControl(this);
        }
    }
}
