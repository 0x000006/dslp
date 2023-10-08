package zerox06.dedicatedlanpinger.mixin;

import net.minecraft.server.dedicated.DedicatedPlayerManager;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import zerox06.dedicatedlanpinger.DedicatedLanPingerMod;
import zerox06.dedicatedlanpinger.network.BetterLanPinger;

import java.io.IOException;

@Mixin(MinecraftDedicatedServer.class)
public abstract class MinecraftDedicatedServerMixin {
    @Shadow public abstract String getMotd();
    @Shadow public abstract int getPort();
    @Shadow public abstract DedicatedPlayerManager getPlayerManager();

    @Unique
    @Nullable
    public BetterLanPinger lanPinger;

    @Inject(method = "setupServer()Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/dedicated/MinecraftDedicatedServer;isOnlineMode()Z", ordinal = 0))
    void beforeLevelLoad(CallbackInfoReturnable<Boolean> cir) {
        try {
            lanPinger = new BetterLanPinger(this::createLanMotd, String.valueOf(getPort()));
            lanPinger.start();
        } catch (IOException e) {
            DedicatedLanPingerMod.LOGGER.warn("**** FAILED TO CREATE LAN PINGER!");
            DedicatedLanPingerMod.LOGGER.warn("The exception was: {}", e.toString());
        }
    }

    @Inject(method = "shutdown", at = @At("TAIL"))
    void onShutdown(CallbackInfo ci) {
        if (lanPinger != null) {
            lanPinger.interrupt();
            lanPinger = null;
        }
    }

    @Unique
    public String createLanMotd() {
        DedicatedPlayerManager playerManager = getPlayerManager();
        if(playerManager == null) return "§f" + getMotd().replace("§r", "§f") + "§7 | ?§8/§7?";
        return "§f" + getMotd().replace("§r", "§f") + "§7 | " + playerManager.getPlayerList().size() + "§8/§7" + playerManager.getMaxPlayerCount();
    }
}