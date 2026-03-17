package net.noxal.f3nproxyperm;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientChangeGameMode;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityStatus;
import com.velocitypowered.api.proxy.Player;

public class F3NPacketListener implements PacketListener {

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() != PacketType.Play.Client.CHANGE_GAME_MODE) {
            return;
        }
        if (!F3NProxyPerm.instance.canUse(event.getPlayer())) {
            return;
        }
        WrapperPlayClientChangeGameMode gameModePacket = new WrapperPlayClientChangeGameMode(event);
        event.setCancelled(true);
        Player player = event.getPlayer();
        F3NProxyPerm.proxy.getScheduler().buildTask(F3NProxyPerm.instance, () -> F3NProxyPerm.instance.changeGameMode(player, gameModePacket.getGameMode()));
    }

    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() != PacketType.Play.Server.ENTITY_STATUS
                || event.getPlayer() == null
                || !F3NProxyPerm.instance.canUse(event.getPlayer())
                || F3NProxyPerm.instance.shouldSendPacket(event.getPlayer())
        ) {
            return;
        }

        WrapperPlayServerEntityStatus entityStatusPacket = new WrapperPlayServerEntityStatus(event);
        if (entityStatusPacket.getEntityId() != PacketEvents.getAPI().getPlayerManager().getUser(event.getPlayer()).getEntityId()) {
            return;
        }
        if (entityStatusPacket.getStatus() <= 28 && entityStatusPacket.getStatus() >= 24) {
            entityStatusPacket.setStatus(28);
        }
    }
}