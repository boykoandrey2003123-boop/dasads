package ru.buildbattle.util;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import ru.buildbattle.BuildBattlePlugin;

public class ItemFactory {

    private final BuildBattlePlugin plugin;
    public final NamespacedKey tagKey;

    public ItemFactory(BuildBattlePlugin plugin) {
        this.plugin = plugin;
        this.tagKey = new NamespacedKey(plugin, "bb_item");
    }

    public ItemStack createMenuCompass() {
        return new ItemBuilder(Material.COMPASS)
                .name("&6&lBuildBattle Меню")
                .lore("&7ПКМ, чтобы открыть меню", "&7и выбрать режим игры")
                .tag(tagKey, "menu")
                .flags()
                .build();
    }

    public ItemStack createBuilderTools() {
        return new ItemBuilder(Material.NETHERITE_SHOVEL)
                .name("&b&lИнструменты строителя")
                .lore("&7ПКМ, чтобы открыть настройки:", "&7заливка блоком, очистка,", "&7границы площадки и другое")
                .tag(tagKey, "tools")
                .flags()
                .glow()
                .build();
    }

    public ItemStack createRatingItem() {
        return new ItemBuilder(Material.GOLDEN_APPLE)
                .name("&e&lОценить постройку")
                .lore("&7ПКМ, чтобы выставить оценку")
                .tag(tagKey, "rating")
                .flags()
                .glow()
                .build();
    }

    public ItemStack createWand() {
        return new ItemBuilder(Material.BLAZE_ROD)
                .name("&d&lИнструмент выделения арены")
                .lore("&7ЛКМ по блоку - точка 1", "&7ПКМ по блоку - точка 2")
                .tag(tagKey, "wand")
                .flags()
                .glow()
                .build();
    }

    public boolean isTagged(ItemStack item, String value) {
        if (item == null || !item.hasItemMeta()) return false;
        String v = item.getItemMeta().getPersistentDataContainer().get(tagKey, PersistentDataType.STRING);
        return value.equals(v);
    }
}
