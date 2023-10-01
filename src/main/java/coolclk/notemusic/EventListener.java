package coolclk.notemusic;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.concurrent.atomic.AtomicReference;

public class EventListener implements Listener {
    public final static EventListener INSTANCE = new EventListener();

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        AtomicReference<NoteMusicGui.Gui> actionGui = new AtomicReference<>();
        NoteMusicGui.guiList.forEach(gui -> {
            if (gui.getInventory() != null) {
                if (event.getInventory().equals(gui.getInventory())) {
                    actionGui.set(gui);
                    event.setCancelled(true);
                }
            }
        });
        if (actionGui.get() != null) actionGui.get().click(event.getSlot());
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        AtomicReference<NoteMusicGui.Gui> actionGui = new AtomicReference<>();
        NoteMusicGui.guiList.forEach(gui -> {
            if (event.getInventory().equals(gui.getInventory())) {
                actionGui.set(gui);
            }
        });
        if (actionGui.get() != null) NoteMusicGui.guiList.remove(actionGui.get());
    }
}
