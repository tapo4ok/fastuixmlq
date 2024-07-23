package mdk.fastxmlmenu.xml;

import mdk.fastxmlmenu.Profiling;
import mdk.fastxmlmenu.command.MenuCommand;
import mdk.fastxmlmenu.fun.Function;
import mdk.fastxmlmenu.hadler.IHandler;
import mdk.fastxmlmenu.Sender;
import mdk.fastxmlmenu.menu.Menu;

import mdk.mutils.Identifier;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Loader {
    public static final FilenameFilter menu = (dir, name) -> name.endsWith(".menu.xml");
    public static final FilenameFilter meta = (dir, name) -> name.endsWith(".meta.xml");

    private final DocumentBuilderFactory factory;
    private Loader() {
        factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
    }

    private static final Loader Inc = new Loader();

    public static Loader getInstance() {
        return Inc;
    }
    public void load(JavaPlugin plugin) {
        Profiling.start("load");

        Menu.us.clear();
        Menu.u.clear();

        Placeholder placeholder = Placeholder.getInstance();
        Logger logger = plugin.getLogger();

        File data = plugin.getDataFolder();
        if (!data.exists()) data.mkdirs();

        {
            File[] uis = data.listFiles(meta);
            assert uis != null;
            for (File file : uis) {
                try {
                    load0(Files.newInputStream(file.toPath()), placeholder);
                    logger.log(Level.INFO, String.format("Load meta %s", file.getName()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        {
            File[] uis = data.listFiles(menu);
            assert uis != null;
            for (File file : uis) {
                try {
                    Menu ui = load(Files.newInputStream(file.toPath()), placeholder);
                    ui.regster(plugin);
                    logger.log(Level.INFO, String.format("Load ui %s", ui.getIdentifier()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        Profiling.clear();
        Profiling.end("load");

        Profiling.printProfilingResults();
        Profiling.clear();
    }

    public void load0(InputStream stream, Placeholder placeholder) {
        Profiling.start("parsing");
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();

            Document doc = builder.parse(stream);
            doc.getDocumentElement().normalize();

            NodeList nodeList = doc.getElementsByTagName("entry");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    placeholder.map.put(element.getAttribute("name"), element.getTextContent());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Profiling.end("parsing");
        Profiling.printProfilingResults();
    }

    public Menu load(InputStream stream, Placeholder placeholder) {
        Profiling.start("parsing");
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();

            Document doc = builder.parse(stream);
            doc.getDocumentElement().normalize();

            Element root = doc.getDocumentElement();

            // Identifier
            Element identifierElement = (Element) root.getElementsByTagName("identifier").item(0);
            String namespace = identifierElement.getAttribute("namespace");
            String path = identifierElement.getAttribute("path");
            Identifier identifier = new Identifier(namespace, path);

            // Handler
            Element handlerElement = (Element) root.getElementsByTagName("handler").item(0);
            String handlerClassName = placeholder.replacePlaceholders(handlerElement.getAttribute("class"));
            IHandler handler = (IHandler) Class.forName(handlerClassName).getDeclaredConstructor().newInstance();

            // Title
            String title = placeholder.replacePlaceholders(root.getElementsByTagName("title").item(0).getTextContent());

            Menu ui = new Menu(identifier, handler, title);

            NodeList commandNodes = root.getElementsByTagName("command");

            for (int i = 0; i < commandNodes.getLength(); i++) {
                Node commandNode = commandNodes.item(i);

                if (commandNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element commandElement = (Element) commandNode;

                    NodeList permisionNodes = commandElement.getElementsByTagName("permision");
                    if (permisionNodes.getLength() > 0) {
                        Element permisionElement = (Element) permisionNodes.item(0);
                        String permisionName = placeholder.replacePlaceholders(permisionElement.getAttribute("permision"));

                        Permission permission = new Permission(permisionName);

                        NodeList defultNodes = permisionElement.getElementsByTagName("defult");
                        if (defultNodes.getLength() > 0) {
                            Element defultElement = (Element) defultNodes.item(0);
                            permission.setDefault(PermissionDefault.valueOf(placeholder.replacePlaceholders(defultElement.getAttribute("defult"))));
                        }

                        NodeList identifierNodes = commandElement.getElementsByTagName("identifier");
                        if (identifierNodes.getLength() > 0) {
                            Element identifierElement2 = (Element) identifierNodes.item(0);
                            String namespace2 = identifierElement2.getAttribute("namespace");
                            String path2 = identifierElement2.getAttribute("path");

                            ui.setCommand(Optional.of(new MenuCommand(new Identifier(namespace2, path2), permission, identifier)));
                        }
                    }
                }
            }

            Element scriptElement = (Element) root.getElementsByTagName("mc").item(0);
            if (scriptElement != null) {
                NodeList functionNodes = scriptElement.getElementsByTagName("function");


                Map<String, Function> functions = new HashMap<>();

                for (int i = 0; i < functionNodes.getLength(); i++) {
                    Element functionElement = (Element) functionNodes.item(i);

                    String name = placeholder.replacePlaceholders(functionElement.getAttribute("name"));
                    Function function = new Function(name, Sender.valueOf(placeholder.replacePlaceholders(functionElement.getAttribute("sender"))));

                    Element metaElement = (Element) functionElement.getElementsByTagName("meta").item(0);
                    if (metaElement != null) {
                        boolean cancelEvent = Boolean.parseBoolean(metaElement.getAttribute("cancel_event"));
                        function.setCancelEvent(cancelEvent);
                    }

                    NodeList lineNodes = functionElement.getElementsByTagName("line");
                    for (int j = 0; j < lineNodes.getLength(); j++) {
                        Element lineElement = (Element) lineNodes.item(j);
                        function.addLine(lineElement.getTextContent());
                    }

                    functions.put(name, function);
                }

                ui.setFunctions(functions);
            }

            // Entries
            NodeList entryNodes = root.getElementsByTagName("entry");
            for (int i = 0; i < entryNodes.getLength(); i++) {
                Node entryNode = entryNodes.item(i);
                if (entryNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element entryElement = (Element) entryNode;

                    String method = placeholder.replacePlaceholders(entryElement.getAttribute("method"));
                    int slot = Integer.parseInt(placeholder.replacePlaceholders(entryElement.getAttribute("slot")));

                    Element itemStackElement = (Element) entryElement.getElementsByTagName("itemstack").item(0);
                    Material material = Material.valueOf(placeholder.replacePlaceholders(itemStackElement.getAttribute("material")));
                    int amount = Integer.parseInt(placeholder.replacePlaceholders(itemStackElement.getAttribute("amount")));
                    ItemStack stack = new ItemStack(material, amount);

                    // Meta data
                    if (itemStackElement.getElementsByTagName("meta").getLength() > 0) {
                        Element metaElement = (Element) itemStackElement.getElementsByTagName("meta").item(0);
                        String ownerClass = placeholder.replacePlaceholders(metaElement.getAttribute("owner"));

                        ItemMeta meta = stack.getItemMeta();

                        handleItemMeta(metaElement, meta, placeholder);

                        if ("org.bukkit.inventory.meta.FireworkMeta".equals(ownerClass) && meta instanceof FireworkMeta) {
                            meta = handleFireworkMeta(metaElement, (FireworkMeta) meta);
                        } else if ("org.bukkit.inventory.meta.PotionMeta".equals(ownerClass) && meta instanceof PotionMeta) {
                            meta = handlePotionMeta(metaElement, (PotionMeta) meta);
                        } else if ("org.bukkit.inventory.meta.SpawnEggMeta".equals(ownerClass) && meta instanceof SpawnEggMeta) {
                            meta = handleSpawnEggMeta(metaElement, (SpawnEggMeta) meta);
                        }

                        stack.setItemMeta(meta);
                    }

                    ui.add(stack, method, slot);
                }
            }

            Profiling.end("parsing");
            Profiling.printProfilingResults();
            return ui;

        } catch (Exception e) {
            e.printStackTrace();
            Profiling.end("parsing");
            Profiling.printProfilingResults();
            return null;
        }
    }


    private static ItemMeta handleItemMeta(Element metaElement, ItemMeta meta, Placeholder placeholder) {
        // Display Name
        if (metaElement.getElementsByTagName("displayName").getLength() > 0) {
            String displayName = metaElement.getElementsByTagName("displayName").item(0).getTextContent();
            meta.setDisplayName(displayName);
        }

        // Lore
        if (metaElement.getElementsByTagName("lore").getLength() > 0) {
            List<String> lore = new ArrayList<>();
            NodeList loreLines = metaElement.getElementsByTagName("line");
            for (int i = 0; i < loreLines.getLength(); i++) {
                lore.add(placeholder.replacePlaceholders(loreLines.item(i).getTextContent()));
            }
            meta.setLore(lore);
        }

        // Enchantments
        if (metaElement.getElementsByTagName("enchantments").getLength() > 0) {
            NodeList enchantmentNodes = metaElement.getElementsByTagName("enchantment");
            for (int i = 0; i < enchantmentNodes.getLength(); i++) {
                Element enchantmentElement = (Element) enchantmentNodes.item(i);
                Enchantment enchantment = Enchantment.getByName(placeholder.replacePlaceholders(enchantmentElement.getAttribute("type")));
                int level = Integer.parseInt(placeholder.replacePlaceholders(enchantmentElement.getAttribute("level")));
                meta.addEnchant(enchantment, level, true);
            }
        }

        if (metaElement.getElementsByTagName("unbreakable").getLength() > 0) {
            boolean unbreakable = Boolean.parseBoolean(metaElement.getElementsByTagName("unbreakable").item(0).getTextContent());
            meta.setUnbreakable(unbreakable);
        }

        if (metaElement.getElementsByTagName("itemFlags").getLength() > 0) {
            NodeList flagNodes = metaElement.getElementsByTagName("flag");
            for (int i = 0; i < flagNodes.getLength(); i++) {
                ItemFlag flag = ItemFlag.valueOf(placeholder.replacePlaceholders(flagNodes.item(i).getTextContent()));
                meta.addItemFlags(flag);
            }
        }

        return meta;
    }

    private static FireworkMeta handleFireworkMeta(Element metaElement, FireworkMeta meta) {
        // Firework Effects
        NodeList effectNodes = metaElement.getElementsByTagName("effect");
        for (int i = 0; i < effectNodes.getLength(); i++) {
            Element effectElement = (Element) effectNodes.item(i);

            FireworkEffect.Builder effectBuilder = FireworkEffect.builder();
            effectBuilder.with(FireworkEffect.Type.valueOf(effectElement.getAttribute("type")));

            // Colors
            String[] colorStrings = effectElement.getAttribute("colors").split(",");
            List<Color> colors = new ArrayList<>();
            for (String colorString : colorStrings) {
                colors.add(Color.fromRGB(Integer.parseInt(colorString)));
            }
            effectBuilder.withColor(colors);

            // Fade Colors
            if (effectElement.hasAttribute("fadeColors")) {
                String[] fadeColorStrings = effectElement.getAttribute("fadeColors").split(",");
                List<Color> fadeColors = new ArrayList<>();
                for (String fadeColorString : fadeColorStrings) {
                    fadeColors.add(Color.fromRGB(Integer.parseInt(fadeColorString)));
                }
                effectBuilder.withFade(fadeColors);
            }

            // Trail
            if (effectElement.hasAttribute("trail")) {
                effectBuilder.trail(Boolean.parseBoolean(effectElement.getAttribute("trail")));
            }

            // Flicker
            if (effectElement.hasAttribute("flicker")) {
                effectBuilder.flicker(Boolean.parseBoolean(effectElement.getAttribute("flicker")));
            }

            meta.addEffect(effectBuilder.build());
        }

        // Power
        if (metaElement.getElementsByTagName("power").getLength() > 0) {
            int power = Integer.parseInt(metaElement.getElementsByTagName("power").item(0).getTextContent());
            meta.setPower(power);
        }

        return meta;
    }

    private static PotionMeta handlePotionMeta(Element metaElement, PotionMeta meta) {
        // Potion Type
        if (metaElement.getElementsByTagName("potionType").getLength() > 0) {
            String potionType = metaElement.getElementsByTagName("potionType").item(0).getTextContent();
            PotionData potionData = new PotionData(PotionType.valueOf(potionType),
                    Boolean.parseBoolean(metaElement.getElementsByTagName("extended").item(0).getTextContent()),
                    Boolean.parseBoolean(metaElement.getElementsByTagName("upgraded").item(0).getTextContent()));
            meta.setBasePotionData(potionData);
        }

        return meta;
    }

    private static SpawnEggMeta handleSpawnEggMeta(Element metaElement, SpawnEggMeta meta) {
        // Entity Type
        if (metaElement.getElementsByTagName("entityType").getLength() > 0) {
            String entityType = metaElement.getElementsByTagName("entityType").item(0).getTextContent();
            meta.setSpawnedType(EntityType.valueOf(entityType));
        }

        return meta;
    }


    private Material getMaterial(String name) {
        try {
            return Material.valueOf(name);
        } catch (IllegalArgumentException e) {
            Bukkit.getLogger().warning("Invalid material: " + name + ". Defaulting to AIR.");
            return Material.AIR; // or any other default material
        }
    }
}
