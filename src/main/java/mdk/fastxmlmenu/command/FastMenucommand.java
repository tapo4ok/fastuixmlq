package mdk.fastxmlmenu.command;

import com.google.common.collect.ImmutableList;
import mdk.fastxmlmenu.FastXMLmenu;
import mdk.fastxmlmenu.menu.Menu;
import mdk.fastxmlmenu.xml.Loader;
import mdk.fastxmlmenu.xml.Placeholder;
import mdk.mutils.Identifier;
import org.apache.commons.lang.Validate;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.StringUtil;

import java.io.File;
import java.nio.file.Files;
import java.util.*;
import java.util.logging.Level;

public class FastMenucommand implements CommandExecutor, TabExecutor {
    private final String[] w1 = new String[] {
            "reload",
            "open",
            "help",
            "placeholder"
    };
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(args.length>0)) {
            sender.sendMessage(String.format("/%s (%s)", "fastmenu", String.join("/", w1)));
            return false;
        }

        switch (args[0]) {
            case "open": {
                Identifier identifier = new Identifier(args[1], args[2]);
                for (Menu ui : Menu.REGISTRY) {
                    if (ui.getIdentifier().equals(identifier)) {
                        ui.open((Player) sender);
                    }
                }
                break;
            }
            case "reload": {
                Loader loader = Loader.getInstance();

                loader.load(JavaPlugin.getPlugin(FastXMLmenu.class));
                break;
            }
            case "placeholder": {
                if ((args.length>1)) {
                    boolean i = false;
                    Placeholder holder = Placeholder.getInstance();
                    switch (args[1]) {
                        case "set":
                        {
                            if (!(args.length>3)) {
                                i = true;
                            }
                            else {
                                holder.map.put(args[2], args[3]);
                            }
                            break;
                        }
                        case "remove": {
                            if (!(args.length>2)) {
                                i = true;
                            }
                            else {
                                holder.map.remove(args[2]);
                            }
                            break;
                        }
                    }
                    if (i) {
                        break;
                    }
                }
            }
            case "help":
            default:
            {
                sender.sendMessage("/fastmenu reload");
                sender.sendMessage("/fastmenu help");
                sender.sendMessage("/fastmenu open <namespace> <path>");
                sender.sendMessage("/fastmenu placeholder set <key> <value>");
                sender.sendMessage("/fastmenu placeholder remove <key>");
                break;
            }
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        Validate.notNull(sender, "Sender cannot be null");
        Validate.notNull(args, "Arguments cannot be null");
        Validate.notNull(alias, "Alias cannot be null");

        if (args.length == 1) {
            return Arrays.asList(w1);
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("open")) {
                return Arrays.asList("<namespace>");
            } else if (args[0].equalsIgnoreCase("placeholder")) {
                return Arrays.asList("set", "remove");
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("open")) {
                return Arrays.asList("<path>");
            } else if (args[0].equalsIgnoreCase("placeholder") && args[1].equalsIgnoreCase("set")) {
                return Arrays.asList("<key>");
            } else if (args[0].equalsIgnoreCase("placeholder") && args[1].equalsIgnoreCase("remove")) {
                return Arrays.asList("<key>");
            }
        } else if (args.length == 4) {
            if (args[0].equalsIgnoreCase("placeholder") && args[1].equalsIgnoreCase("set")) {
                return Arrays.asList("<value>");
            }
        }
        return new ArrayList<>();
    }
}
