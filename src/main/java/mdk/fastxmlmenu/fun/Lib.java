package mdk.fastxmlmenu.fun;

import mdk.mutils.Identifier;

import java.util.ArrayList;

public class Lib extends ArrayList<Function> {
    public Identifier identifier;
    public Lib(Identifier identifier) {
        this.identifier = identifier;
    }

    public Identifier getIdentifier() {
        return identifier;
    }
}
