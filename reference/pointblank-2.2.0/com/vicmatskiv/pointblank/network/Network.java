/*
 * Decompiled with CFR 0.152.
 */
package com.vicmatskiv.pointblank.network;

import com.vicmatskiv.pointblank.NetworkService;
import com.vicmatskiv.pointblank.Platform;
import com.vicmatskiv.pointblank.network.AimingChangeRequestPacket;
import com.vicmatskiv.pointblank.network.ClientBoundAddSpawnEntityDataPacket;
import com.vicmatskiv.pointblank.network.ClientBoundPlayerDataSyncPacket;
import com.vicmatskiv.pointblank.network.CraftingRequestPacket;
import com.vicmatskiv.pointblank.network.CraftingResponsePacket;
import com.vicmatskiv.pointblank.network.CustomClientBoundExplosionPacket;
import com.vicmatskiv.pointblank.network.EffectBroadcastPacket;
import com.vicmatskiv.pointblank.network.EffectRequestPacket;
import com.vicmatskiv.pointblank.network.FireModeRequestPacket;
import com.vicmatskiv.pointblank.network.FireModeResponsePacket;
import com.vicmatskiv.pointblank.network.GunStateRequestPacket;
import com.vicmatskiv.pointblank.network.GunStateResponsePacket;
import com.vicmatskiv.pointblank.network.HitScanFireRequestPacket;
import com.vicmatskiv.pointblank.network.HitScanFireResponsePacket;
import com.vicmatskiv.pointblank.network.MainHeldSimplifiedStateBroadcastPacket;
import com.vicmatskiv.pointblank.network.MainHeldSimplifiedStateSyncRequest;
import com.vicmatskiv.pointblank.network.ProjectileFireRequestPacket;
import com.vicmatskiv.pointblank.network.ReloadRequestPacket;
import com.vicmatskiv.pointblank.network.ReloadResponsePacket;
import com.vicmatskiv.pointblank.network.ServerBoundNextAttachmentPacket;
import com.vicmatskiv.pointblank.network.ServerBoundOpenScreenPacket;
import com.vicmatskiv.pointblank.network.SpawnParticlePacket;
import com.vicmatskiv.pointblank.network.ThrowProjectileRequestPacket;

public class Network {
    private static NetworkService networkService = Platform.getInstance().getNetworkService();

    public static void setupNetworkChannel() {
        networkService.registerPacket(ClientBoundAddSpawnEntityDataPacket.class, NetworkService.Direction.SERVER_TO_CLIENT, ClientBoundAddSpawnEntityDataPacket::encode, ClientBoundAddSpawnEntityDataPacket::decode, ClientBoundAddSpawnEntityDataPacket::handle);
        networkService.registerPacket(MainHeldSimplifiedStateSyncRequest.class, NetworkService.Direction.CLIENT_TO_SERVER, GunStateRequestPacket::encode, MainHeldSimplifiedStateSyncRequest::decode, GunStateRequestPacket::handle);
        networkService.registerPacket(MainHeldSimplifiedStateBroadcastPacket.class, NetworkService.Direction.SERVER_TO_CLIENT, MainHeldSimplifiedStateBroadcastPacket::encode, MainHeldSimplifiedStateBroadcastPacket::decode, MainHeldSimplifiedStateBroadcastPacket::handle);
        networkService.registerPacket(HitScanFireRequestPacket.class, NetworkService.Direction.CLIENT_TO_SERVER, GunStateRequestPacket::encode, HitScanFireRequestPacket::decode, GunStateRequestPacket::handle);
        networkService.registerPacket(HitScanFireResponsePacket.class, NetworkService.Direction.SERVER_TO_CLIENT, GunStateResponsePacket::encode, HitScanFireResponsePacket::decode, HitScanFireResponsePacket::handle);
        networkService.registerPacket(ProjectileFireRequestPacket.class, NetworkService.Direction.CLIENT_TO_SERVER, GunStateRequestPacket::encode, ProjectileFireRequestPacket::decode, GunStateRequestPacket::handle);
        networkService.registerPacket(ReloadRequestPacket.class, NetworkService.Direction.CLIENT_TO_SERVER, GunStateRequestPacket::encode, ReloadRequestPacket::decode, GunStateRequestPacket::handle);
        networkService.registerPacket(ReloadResponsePacket.class, NetworkService.Direction.SERVER_TO_CLIENT, GunStateResponsePacket::encode, ReloadResponsePacket::decode, GunStateResponsePacket::handle);
        networkService.registerPacket(FireModeRequestPacket.class, NetworkService.Direction.CLIENT_TO_SERVER, GunStateRequestPacket::encode, FireModeRequestPacket::decode, GunStateRequestPacket::handle);
        networkService.registerPacket(FireModeResponsePacket.class, NetworkService.Direction.SERVER_TO_CLIENT, GunStateResponsePacket::encode, FireModeResponsePacket::decode, GunStateResponsePacket::handle);
        networkService.registerPacket(SpawnParticlePacket.class, NetworkService.Direction.SERVER_TO_CLIENT, SpawnParticlePacket::encode, SpawnParticlePacket::decode, SpawnParticlePacket::handle);
        networkService.registerPacket(CustomClientBoundExplosionPacket.class, NetworkService.Direction.SERVER_TO_CLIENT, CustomClientBoundExplosionPacket::encode, CustomClientBoundExplosionPacket::decode, CustomClientBoundExplosionPacket::handle);
        networkService.registerPacket(EffectRequestPacket.class, NetworkService.Direction.CLIENT_TO_SERVER, EffectRequestPacket::encode, EffectRequestPacket::decode, EffectRequestPacket::handle);
        networkService.registerPacket(EffectBroadcastPacket.class, NetworkService.Direction.SERVER_TO_CLIENT, EffectBroadcastPacket::encode, EffectBroadcastPacket::decode, EffectBroadcastPacket::handle);
        networkService.registerPacket(AimingChangeRequestPacket.class, NetworkService.Direction.CLIENT_TO_SERVER, GunStateRequestPacket::encode, AimingChangeRequestPacket::decode, GunStateRequestPacket::handle);
        networkService.registerPacket(CraftingRequestPacket.class, NetworkService.Direction.CLIENT_TO_SERVER, CraftingRequestPacket::encode, CraftingRequestPacket::decode, CraftingRequestPacket::handle);
        networkService.registerPacket(CraftingResponsePacket.class, NetworkService.Direction.SERVER_TO_CLIENT, CraftingResponsePacket::encode, CraftingResponsePacket::decode, CraftingResponsePacket::handle);
        networkService.registerPacket(ServerBoundOpenScreenPacket.class, NetworkService.Direction.CLIENT_TO_SERVER, ServerBoundOpenScreenPacket::encode, ServerBoundOpenScreenPacket::decode, ServerBoundOpenScreenPacket::handle);
        networkService.registerPacket(ServerBoundNextAttachmentPacket.class, NetworkService.Direction.CLIENT_TO_SERVER, ServerBoundNextAttachmentPacket::encode, ServerBoundNextAttachmentPacket::decode, ServerBoundNextAttachmentPacket::handle);
        networkService.registerPacket(ClientBoundPlayerDataSyncPacket.class, NetworkService.Direction.SERVER_TO_CLIENT, ClientBoundPlayerDataSyncPacket::encode, ClientBoundPlayerDataSyncPacket::decode, ClientBoundPlayerDataSyncPacket::handle);
        networkService.registerPacket(ThrowProjectileRequestPacket.class, NetworkService.Direction.CLIENT_TO_SERVER, ThrowProjectileRequestPacket::encode, ThrowProjectileRequestPacket::decode, ThrowProjectileRequestPacket::handle);
        networkService.onRegisteringPacketsCompleted();
    }
}

