package mdk.fastxmlmenu.fun;

import mdk.fastxmlmenu.Sender;

import java.util.ArrayList;
import java.util.List;

public class Function {
    private final String name;
    private boolean cancelEvent;
    private final List<String> lines = new ArrayList<>();
    private final Sender sender;

    public Function(String name, Sender sender) {
        this.name = name;
        this.sender = sender;
    }

    public Sender getSender() {
        return sender;
    }

    public String getName() {
        return name;
    }

    public boolean isCancelEvent() {
        return cancelEvent;
    }

    public void setCancelEvent(boolean cancelEvent) {
        this.cancelEvent = cancelEvent;
    }

    public void addLine(String line) {
        lines.add(line);
    }

    public List<String> getLines() {
        return lines;
    }
}