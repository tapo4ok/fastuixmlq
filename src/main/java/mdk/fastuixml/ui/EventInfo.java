package mdk.fastuixml.ui;

public class EventInfo {
    private boolean cancel;

    public EventInfo() {
        this.cancel = false;
    }

    public boolean isCancel() {
        return cancel;
    }

    public void cancel() {
        this.cancel = true;
    }

    public void setCancel(boolean cancel) {
        this.cancel = cancel;
    }
}
