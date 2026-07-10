package you.jass.betterhitreg.mixin;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import you.jass.betterhitreg.hitreg.Hitreg;


@Mixin(LocalPlayer.class)
public abstract class PlayerMixin {
    @Inject(method = "crit", at = @At("HEAD"), cancellable = true)
    private void crit(Entity target, CallbackInfo ci) {
        if (Hitreg.isToggled()) ci.cancel();
    }

    @Inject(method = "magicCrit", at = @At("HEAD"), cancellable = true)
    private void magicCrit(Entity target, CallbackInfo ci) {
        if (Hitreg.isToggled()) ci.cancel();
    }
}