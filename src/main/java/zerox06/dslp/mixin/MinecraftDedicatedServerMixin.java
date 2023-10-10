package zerox06.dslp.mixin;

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
import zerox06.dslp.DSLPMod;
import zerox06.dslp.network.BetterLanPinger;

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
            DSLPMod.LOGGER.warn("**** FAILED TO CREATE LAN PINGER!");
            DSLPMod.LOGGER.warn("The exception was: {}", e.toString());
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
        if(playerManager == null) return getMotd() + "§7 | ?§8/§7?";
        return getMotd() + "§7 | " + playerManager.getPlayerList().size() + "§8/§7" + playerManager.getMaxPlayerCount();
    }
}
