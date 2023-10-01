package coolclk.notemusic;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class NoteMusicGui {
    static class Gui {
        static class GuiItem {
            interface ClickAction {
                boolean action();
            }

            private final int slot;
            private ClickAction click = () -> true;

            public GuiItem(int slot) {
                this.slot = slot;
            }

            public void setClickAction(ClickAction click) {
                this.click = click;
            }

            public ClickAction getClickAction() {
                return this.click;
            }
        }

        private final Player owner;
        private Inventory inventory;
        private final List<GuiItem> items = new ArrayList<>();

        public Gui(Player owner, int rows, String title) {
            this.owner = owner;
            this.recreateInventory(rows, title);
            this.getOwner().openInventory(this.getInventory());
        }

        public Inventory getInventory() {
            return this.inventory;
        }

        public List<GuiItem> getItems() {
            return this.items;
        }

        public void click(int slot) {
            new ArrayList<>(this.getItems()).forEach(item -> {
                if (slot == item.slot) {
                    if (!item.getClickAction().action()) this.getOwner().closeInventory();
                }
            });
        }

        public Player getOwner() {
            return this.owner;
        }

        public void removeItems() {
            for (int i = 0; i < inventory.getSize(); i++) {
                inventory.setItem(0, null);
            }
        }

        public GuiItem addItem(int x, int y, ItemStack item) {
            return this.addItem(Helper.getInventoryIndex(x, y), item);
        }

        public GuiItem addItem(int slot, ItemStack item) {
            GuiItem guiItem = new GuiItem(slot);
            this.getInventory().setItem(slot, item);
            this.getItems().add(guiItem);
            return guiItem;
        }

        public GuiItem addItem(int slot, Material type, int amount, short damage, String displayName, List<String> lore) {
            ItemStack item = new ItemStack(type, amount, damage);
            ItemMeta itemMeta = item.getItemMeta();
            itemMeta.setDisplayName(displayName);
            itemMeta.setLore(lore);
            item.setItemMeta(itemMeta);
            return this.addItem(slot, item);
        }

        public GuiItem addItem(int x, int y, Material type, int amount, short damage, String displayName, List<String> lore) {
            ItemStack item = new ItemStack(type, amount, damage);
            ItemMeta itemMeta = item.getItemMeta();
            itemMeta.setDisplayName(displayName);
            itemMeta.setLore(lore);
            item.setItemMeta(itemMeta);
            return this.addItem(x, y, item);
        }

        public void recreateInventory(int rows, String title) {
            this.getItems().clear();
            this.inventory = Bukkit.createInventory(this.getOwner(), Helper.getInventorySize(rows), title);
            this.getOwner().closeInventory();
            this.getOwner().openInventory(this.getInventory());
        }
    }

    static class Helper {
        static int getInventorySize(int row) {
            return row * 9;
        }

        static int getInventoryIndex(int x, int y) {
            return y * 9 + x;
        }
    }

    static List<Gui> guiList = new ArrayList<>();

    public static void open(Player player) {
        Gui gui = new Gui(player, 1, "NoteMusic");
        changeGuiMainMenu(gui);
        guiList.add(gui);
    }

    private static void changeGuiMainMenu(Gui gui) {
        gui.removeItems();
        gui.recreateInventory(4, Main.message.getString("inventory-main-title"));
        gui.addItem(1, 1,
                Material.NOTE_BLOCK, 1, (short) 0,
                Main.message.getString("inventory-main-playmusic-name"),
                gui.getOwner().hasPermission("notemusic.command.playmusic") ? Main.message.getStringList("inventory-main-clickDo-lore") : Main.message.getStringList("inventory-main-noPermission-lore"))
                .setClickAction(() -> {
                    changeGuiPlayMusic(gui, 0);
                    return true;
                });
        gui.addItem(3, 1,
                        Material.REDSTONE_BLOCK, 1, (short) 0,
                        Main.message.getString("inventory-main-stopmusic-name"),
                        gui.getOwner().hasPermission("notemusic.command.stopmusic") ? Main.message.getStringList("inventory-main-clickDo-lore") : Main.message.getStringList("inventory-main-noPermission-lore"))
                .setClickAction(() -> {
                    changeGuiStopMusic(gui, 0);
                    return true;
                });
        gui.addItem(5, 1,
                        Material.PAPER, 1, (short) 0,
                        Main.message.getString("inventory-main-importmusic-name"),
                        gui.getOwner().hasPermission("notemusic.command.importmusic") ? Main.message.getStringList("inventory-main-clickDo-lore") : Main.message.getStringList("inventory-main-noPermission-lore"))
                .setClickAction(() -> {
                    changeGuiImportMusic(gui, 0);
                    return true;
                });
        gui.addItem(7, 1,
                        Material.ARROW, 1, (short) 0,
                        Main.message.getString("inventory-main-removemusic-name"),
                        gui.getOwner().hasPermission("notemusic.command.removemusic") ? Main.message.getStringList("inventory-main-clickDo-lore") : Main.message.getStringList("inventory-main-noPermission-lore"))
                .setClickAction(() -> {
                    changeGuiRemoveMusic(gui, 0);
                    return true;
                });
        gui.addItem(4, 3,
                        Material.ARROW, 1, (short) 0,
                        Main.message.getString("inventory-main-reload-name"),
                        gui.getOwner().hasPermission("notemusic.command.reload") ? Main.message.getStringList("inventory-main-clickDo-lore") : Main.message.getStringList("inventory-main-noPermission-lore"))
                .setClickAction(() -> {
                    gui.getOwner().performCommand("notemusic:notemusic reload");
                    return false;
                });
    }

    private static void changeGuiPlayMusic(Gui gui, int page) {
        if (!gui.getOwner().hasPermission("notemusic.command.playmusic")) return;
        gui.recreateInventory(6, Main.message.getString("inventory-main-title"));
        for (int i = 0; i <= 8; i++) gui.addItem(i, 4, Material.STAINED_GLASS_PANE, 1, (short) 0, "§r", Collections.emptyList());
        int pageLength = (int) (Math.floor(Main.music.getKeys(false).size() / 36D) + 1);
        if (page - 1 >= 0) gui.addItem(2, 5, Material.ARROW, 1, (short) 0, Main.message.getString("inventory-playmusic-previousPage-name"), Main.message.getStringList("inventory-main-clickDo-lore")).setClickAction(() -> {
                changeGuiPlayMusic(gui, page - 1);
                return true;
            });
        gui.addItem(4, 5, Material.PAPER, 1, (short) 0, String.format(Main.message.getString("inventory-playmusic-page-name"), page + 1, pageLength), Collections.emptyList());
        if (page + 1 < pageLength) gui.addItem(6, 5, Material.ARROW, 1, (short) 0, Main.message.getString("inventory-playmusic-nextPage-name"), Main.message.getStringList("inventory-main-clickDo-lore")).setClickAction(() -> {
                changeGuiPlayMusic(gui, page + 1);
                return true;
            });
        int slot = 0;
        for (int i = page * 36; i < (page + 1) * 36 && i < Main.music.getKeys(false).size(); i++) {
            String musicName = Main.music.getKeys(false).toArray(new String[0])[i];
            gui.addItem(slot,
                            Material.NOTE_BLOCK, 1, (short) 0,
                            String.format(Main.message.getString("inventory-playmusic-item-name"), musicName),
                            Arrays.asList(
                                    String.format(Main.message.getString("inventory-playmusic-item-lore-time"), Float.parseFloat(Main.music.getString(musicName + ".speed")) * 0.08f),
                                    Main.message.getString("inventory-playmusic-item-lore-click")
                            ))
                    .setClickAction(() -> {
                        gui.getOwner().performCommand("notemusic:notemusic playmusic " + musicName);
                        return false;
                    });
            slot++;
        }
    }

    private static void changeGuiStopMusic(Gui gui, int page) {
        if (!gui.getOwner().hasPermission("notemusic.command.stopmusic")) return;
        gui.recreateInventory(6, Main.message.getString("inventory-main-title"));
        for (int i = 0; i <= 8; i++) gui.addItem(i, 4, Material.STAINED_GLASS_PANE, 1, (short) 0, "§r", Collections.emptyList());
        int pageLength = (int) (Math.floor(Main.playingMusic.size() / 36D) + 1);
        if (page - 1 >= 0) gui.addItem(2, 5, Material.ARROW, 1, (short) 0, Main.message.getString("inventory-stopmusic-previousPage-name"), Main.message.getStringList("inventory-main-clickDo-lore")).setClickAction(() -> {
            changeGuiStopMusic(gui, page - 1);
            return true;
        });
        gui.addItem(4, 5, Material.PAPER, 1, (short) 0, String.format(Main.message.getString("inventory-stopmusic-page-name"), page + 1, pageLength), Collections.emptyList());
        if (page + 1 < pageLength) gui.addItem(6, 5, Material.ARROW, 1, (short) 0, Main.message.getString("inventory-stopmusic-nextPage-name"), Main.message.getStringList("inventory-main-clickDo-lore")).setClickAction(() -> {
            changeGuiStopMusic(gui, page + 1);
            return true;
        });
        int slot = 0;
        for (int i = page * 36; i < (page + 1) * 36 && i < Main.playingMusic.size(); i++) {
            MusicRunnable musicRunnable = Main.playingMusic.toArray(new MusicRunnable[0])[i];
            gui.addItem(slot,
                            Material.NOTE_BLOCK, 1, (short) 0,
                            String.format(Main.message.getString("inventory-stopmusic-item-name"), musicRunnable.musicName, musicRunnable.musicPlayId),
                            Arrays.asList(
                                    String.format(Main.message.getString("inventory-stopmusic-item-lore-time"), Float.parseFloat(Main.music.getString(musicRunnable.musicName + ".speed")) * 0.08f),
                                    Main.message.getString("inventory-stopmusic-item-lore-click")
                            ))
                    .setClickAction(() -> {
                        gui.getOwner().performCommand("notemusic:notemusic stopmusic " + musicRunnable.musicPlayId);
                        return false;
                    });
            slot++;
        }
    }

    private static void changeGuiImportMusic(Gui gui, int page) {
        if (!gui.getOwner().hasPermission("notemusic.command.importmusic")) return;
        gui.recreateInventory(6, Main.message.getString("inventory-main-title"));
        for (int i = 0; i <= 8; i++) gui.addItem(i, 4, Material.STAINED_GLASS_PANE, 1, (short) 0, "§r", Collections.emptyList());
        int pageLength = (int) (Math.floor(Main.music.getKeys(false).size() / 36D) + 1);
        if (page - 1 >= 0) gui.addItem(2, 5, Material.ARROW, 1, (short) 0, Main.message.getString("inventory-importmusic-previousPage-name"), Main.message.getStringList("inventory-main-clickDo-lore")).setClickAction(() -> {
            changeGuiImportMusic(gui, page - 1);
            return true;
        });
        gui.addItem(4, 5, Material.PAPER, 1, (short) 0, String.format(Main.message.getString("inventory-importmusic-page-name"), page + 1, pageLength), Collections.emptyList());
        if (page + 1 < pageLength) gui.addItem(6, 5, Material.ARROW, 1, (short) 0, Main.message.getString("inventory-importmusic-nextPage-name"), Main.message.getStringList("inventory-main-clickDo-lore")).setClickAction(() -> {
            changeGuiImportMusic(gui, page + 1);
            return true;
        });
        int slot = 0;
        for (int i = page * 36; i < (page + 1) * 36 && i < Main.getUnreportedMusic().size(); i++) {
            String file = Main.getUnreportedMusic().get(i);
            gui.addItem(slot,
                            Material.NOTE_BLOCK, 1, (short) 0,
                            String.format(Main.message.getString("inventory-importmusic-item-name"), file),
                            Main.message.getStringList("inventory-importmusic-item-lore"))
                    .setClickAction(() -> {
                        gui.getOwner().performCommand("notemusic:notemusic importmusic " + file);
                        return false;
                    });
            slot++;
        }
    }

    private static void changeGuiRemoveMusic(Gui gui, int page) {
        if (!gui.getOwner().hasPermission("notemusic.command.removemusic")) return;
        gui.recreateInventory(6, Main.message.getString("inventory-main-title"));
        for (int i = 0; i <= 8; i++) gui.addItem(i, 4, Material.STAINED_GLASS_PANE, 1, (short) 0, "§r", Collections.emptyList());
        int pageLength = (int) (Math.floor(Main.music.getKeys(false).size() / 36D) + 1);
        if (page - 1 >= 0) gui.addItem(2, 5, Material.ARROW, 1, (short) 0, Main.message.getString("inventory-removemusic-previousPage-name"), Main.message.getStringList("inventory-main-clickDo-lore")).setClickAction(() -> {
            changeGuiRemoveMusic(gui, page - 1);
            return true;
        });
        gui.addItem(4, 5, Material.PAPER, 1, (short) 0, String.format(Main.message.getString("inventory-removemusic-page-name"), page + 1, pageLength), Collections.emptyList());
        if (page + 1 < pageLength) gui.addItem(6, 5, Material.ARROW, 1, (short) 0, Main.message.getString("inventory-removemusic-nextPage-name"), Main.message.getStringList("inventory-main-clickDo-lore")).setClickAction(() -> {
            changeGuiRemoveMusic(gui, page + 1);
            return true;
        });
        int slot = 0;
        for (int i = page * 36; i < (page + 1) * 36 && i < Main.music.getKeys(false).size(); i++) {
            String musicName = Main.music.getKeys(false).toArray(new String[0])[i];
            gui.addItem(slot,
                            Material.BOOK, 1, (short) 0,
                            String.format(Main.message.getString("inventory-removemusic-item-name"), musicName),
                            Main.message.getStringList("inventory-removemusic-item-lore"))
                    .setClickAction(() -> {
                        gui.getOwner().performCommand("notemusic:notemusic removemusic " + musicName);
                        return false;
                    });
            slot++;
        }
    }
}
