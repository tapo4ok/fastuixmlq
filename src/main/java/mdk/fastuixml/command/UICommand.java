package mdk.fastuixml.command;

import mdk.fastuixml.ui.UI;
import mdk.mutils.Identifier;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;

public class UICommand extends Command {
    public final Permission permission;
    public final Identifier identifier;
    public final Identifier ui;
    public UICommand(Identifier identifier, Permission permission, Identifier ui) {
        super(identifier.getPath());
        setPermission(permission.getName());
        this.identifier = identifier;
        this.permission = permission;
        this.ui = ui;
    }

    public void register(JavaPlugin plugin) {
        try {
            plugin.getServer().getPluginManager().addPermission(permission);
        } catch (Throwable ignored) {}
        try {
            Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            CommandMap map = (CommandMap)commandMapField.get(Bukkit.getServer());
            map.register(identifier.getNamespace(), this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        for (UI ui : UI.REGISTRY) {
            if (ui.getIdentifier().equals(this.ui)) {
                ui.open((Player) sender);
            }
        }
        return false;
    }
}
