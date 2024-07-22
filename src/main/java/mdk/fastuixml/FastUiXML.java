package mdk.fastuixml;

import mdk.fastuixml.ui.UI;

import mdk.fastuixml.xml.Loader;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Files;

public final class FastUiXML extends JavaPlugin {
    public static final FilenameFilter filter = new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
            return name.endsWith(".ui.xml");
        }
    };
    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new UI.EventHandler(), this);


        Loader loader = Loader.getInstance();
        File data = getDataFolder();
        if (!data.exists()) data.mkdirs();

        File[] uis = data.listFiles(filter);

        assert uis != null;
        for (File file : uis) {
            try {
                UI ui = loader.load(Files.newInputStream(file.toPath()));
                ui.regster(this);
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
