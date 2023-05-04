package net.william278.huskhomes.importer;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.hook.Hook;
import net.william278.huskhomes.user.CommandUser;
import net.william278.huskhomes.user.OnlineUser;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.logging.Level;

public abstract class Importer extends Hook {

    protected final HuskHomes plugin;
    private final String name;
    private final List<ImportData> supportedImportData;

    protected Importer(@NotNull String name, @NotNull List<ImportData> supportedData, @NotNull HuskHomes plugin) {
        super(plugin, name + " Importer");
        this.name = name;
        this.supportedImportData = supportedData;
        this.plugin = plugin;
    }

    @Override
    public void initialize() {
        // No initialization required
    }

    public final void start(@NotNull CommandUser user) {
        final LocalDateTime startTime = LocalDateTime.now();
        log(user, Level.INFO, "⌚ Starting " + name + " data import...");

        for (ImportData data : supportedImportData) {
            try {
                log(user, Level.INFO, "│ ⌚ Importing " + data.getName() + "...");
                final int entries = importData(data);
                log(user, Level.INFO, "│ └ ✔ Imported " + data.getName() + " (" + entries + " entries)");
            } catch (Throwable e) {
                log(user, Level.WARNING, "│ └ ❌ Failed to import " + data.getName() + ": " + e.getMessage(), e);
                return;
            }
        }

        final long timeTaken = startTime.until(LocalDateTime.now(), ChronoUnit.SECONDS);
        log(user, Level.INFO, "✔ Completed import from " + name + " (took " + timeTaken + "s)");
    }

    protected abstract int importData(@NotNull ImportData importData) throws Throwable;

    protected final void log(@NotNull CommandUser user, @NotNull Level level, @NotNull String message, @NotNull Throwable... e) {
        message = "[Importer] " + message;
        if (user instanceof OnlineUser online) {
            final TextColor color = level == Level.SEVERE || level == Level.WARNING
                    ? TextColor.color(0xff3300)
                    : TextColor.color(0xfff021);
            online.sendMessage(Component.text(message, color));
        }
        plugin.log(level, message, e);
    }

    @NotNull
    public String getImporterName() {
        return name;
    }

    @NotNull
    public List<ImportData> getSupportedImportData() {
        return supportedImportData;
    }

    /**
     * Represents types of data that can be imported
     */
    public enum ImportData {
        USERS("User Data"),
        HOMES("Homes"),
        WARPS("Warps");

        private final String name;

        ImportData(@NotNull String name) {
            this.name = name;
        }

        @NotNull
        public String getName() {
            return name;
        }
    }

}
