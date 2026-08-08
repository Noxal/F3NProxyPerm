package net.noxal.f3nproxyperm;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientChatCommandUnsigned;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityStatus;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import io.github.retrooper.packetevents.velocity.factory.VelocityPacketEventsBuilder;
import jakarta.inject.Inject;
import org.slf4j.Logger;

import java.nio.file.Path;

@Plugin(
        id = "f3nproxyperm",
        name = "F3NProxyPerm",
        authors = "Willsr71",
        version = PluginInfo.VERSION,
        dependencies = {
                @Dependency(id = "packetevents"),
                @Dependency(id = "luckperms", optional = true)
        }
)
public class F3NProxyPerm {
    public static F3NProxyPerm instance;
    public static Logger logger;
    public static ProxyServer proxy;

    private LuckPermsHook luckpermsHook;

    @Inject
    public F3NProxyPerm(ProxyServer proxy, Logger logger, PluginContainer container, @DataDirectory Path dataDirectory) {
        F3NProxyPerm.instance = this;
        F3NProxyPerm.proxy = proxy;
        F3NProxyPerm.logger = logger;

        PacketEvents.setAPI(VelocityPacketEventsBuilder.build(proxy, container, logger, dataDirectory));
    }

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {
        try {
            PacketEvents.getAPI().init();
            PacketEvents.getAPI().getEventManager().registerListener(new F3NPacketListener(), PacketListenerPriority.HIGHEST);

            proxy.getCommandManager().register(F3NPermCommand.command());
            proxy.getEventManager().register(this, ProxyShutdownEvent.class, this::onProxyShutdown);
            proxy.getEventManager().register(this, ServerPostConnectEvent.class, this::onServerPostConnect);
            if (proxy.getPluginManager().getPlugin("luckperms").isPresent()) luckpermsHook = new LuckPermsHook();
        } catch (Throwable e) {
            logger.error("Failed to initialize PacketEvents", e);
        }
    }

    public void onProxyShutdown(ProxyShutdownEvent event) {
        PacketEvents.getAPI().terminate();
        if (luckpermsHook != null) luckpermsHook.unregister();
    }

    public void onServerPostConnect(ServerPostConnectEvent event) {
        updateOpLevel(event.getPlayer());
    }

    public void updateOpLevel(Player player) {
        try {
            if (!shouldSendPacket(player)) return;
            int status = canUse(player) ? 28 : 24;
            PacketEvents.getAPI().getPlayerManager().sendPacket(player, new WrapperPlayServerEntityStatus(PacketEvents.getAPI().getPlayerManager().getUser(player).getEntityId(), status));
        } catch (Exception e) {
            logger.error("Failed to update op level for player " + player.getUsername(), e);
        }
    }

    public void changeGameMode(Player player, GameMode gameMode) {
        String command = "/gamemode " + gameMode.toString().toLowerCase();
        PacketEvents.getAPI().getPlayerManager().receivePacket(player, new WrapperPlayClientChatCommandUnsigned(command));
    }

    public boolean canUse(Player player) {
        return player.hasPermission("f3nperm.use");
    }

    public boolean shouldSendPacket(Player player) {
        return player.getProtocolVersion().getProtocol() > ProtocolVersion.MINECRAFT_1_13.getProtocol();
    }
}
