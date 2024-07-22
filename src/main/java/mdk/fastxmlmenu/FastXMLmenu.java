package mdk.fastxmlmenu;

import mdk.fastxmlmenu.command.FastUIcommand;
import mdk.fastxmlmenu.ui.UI;

import mdk.fastxmlmenu.xml.Loader;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class FastXMLmenu extends JavaPlugin {
    public static final FilenameFilter filter = new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
            return name.endsWith(".menu.xml");
        }
    };
    @Override
    public void onEnable() {
        Logger logger = getLogger();
        getServer().getPluginManager().registerEvents(new UI.EventHandler(), this);

        {
            FastUIcommand fastUIcommand = new FastUIcommand();
            PluginCommand command = getCommand("fastui");

            command.setTabCompleter(fastUIcommand);
            command.setExecutor(fastUIcommand);
        }

        Loader loader = Loader.getInstance();
        File data = getDataFolder();
        if (!data.exists()) data.mkdirs();

        File[] uis = data.listFiles(filter);

        assert uis != null;
        for (File file : uis) {
            try {
                UI ui = loader.load(Files.newInputStream(file.toPath()));
                ui.regster(this);
                logger.log(Level.INFO, String.format("Load ui %s", ui.getIdentifier()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
