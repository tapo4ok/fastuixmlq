package mdk.fastxmlmenu;

import mdk.fastxmlmenu.command.FastMenucommand;
import mdk.fastxmlmenu.menu.Menu;

import mdk.fastxmlmenu.xml.Loader;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class FastXMLmenu extends JavaPlugin {
    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new Menu.EventHandler(), this);

        {
            FastMenucommand fastUIcommand = new FastMenucommand();
            PluginCommand command = getCommand("fastmenu");

            command.setTabCompleter(fastUIcommand);
            command.setExecutor(fastUIcommand);
        }

        Loader loader = Loader.getInstance();

        loader.load(this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
