package de.maxhenkel.voicechat.permission;

import com.dhyun.portal.permission.Permission;
import com.dhyun.portal.permission.PermissionManager;
import com.dhyun.portal.permission.PermissionType;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.server.permission.events.PermissionGatherEvent;
import net.minecraftforge.server.permission.nodes.PermissionNode;
import net.minecraftforge.server.permission.nodes.PermissionTypes;

import java.util.stream.Collectors;

public class ForgePermissionManager extends PermissionManager {

    @Override
    public Permission createPermissionInternal(String modId, String node, PermissionType type) {
        return new ForgePermission(new PermissionNode<>(modId, node, PermissionTypes.BOOLEAN, (player, playerUUID, context) -> type.hasPermission(player)), type);
    }

    @SubscribeEvent
    public void registerPermissions(PermissionGatherEvent.Nodes event) {
        event.addNodes(getPermissions().stream().map(ForgePermission.class::cast).map(ForgePermission::getNode).collect(Collectors.toList()));
    }

}
