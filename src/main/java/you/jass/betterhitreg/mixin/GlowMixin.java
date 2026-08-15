package you.jass.betterhitreg.mixin;

import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import you.jass.betterhitreg.hitreg.Hitreg;
import you.jass.betterhitreg.settings.Style;
import you.jass.betterhitreg.settings.Toggle;
import you.jass.betterhitreg.utility.Render;

@Mixin(Entity.class)
public abstract class GlowMixin {
    @Shadow private int id;
    @Inject(method = "getTeamColor", at = @At("RETURN"), cancellable = true)
    public void glow(CallbackInfoReturnable<Integer> cir) {
        if (!Hitreg.withinFight || id != Hitreg.target.getId() || Toggle.RENDER_HITBOX.toggled() || Toggle.RENDER_SERVER_HITBOX.toggled()) return;
        if (Toggle.PERFECT_HIT_COLOR.toggled() && System.currentTimeMillis() - Hitreg.lastPerfectHit <= 500) cir.setReturnValue(Style.PERFECT_HIT.argb());
        else if (Toggle.JUMP_RESET_COLOR.toggled() && System.currentTimeMillis() - Hitreg.lastJumpReset <= 500) cir.setReturnValue(Style.JUMP_RESET.argb());
    }

    @Inject(method = "isCurrentlyGlowing", at = @At("RETURN"), cancellable = true)
    public void isGlow(CallbackInfoReturnable<Boolean> cir) {
        if (!Hitreg.withinFight || id != Hitreg.target.getId() || Toggle.RENDER_HITBOX.toggled() || Toggle.RENDER_SERVER_HITBOX.toggled()) return;
        if (Toggle.PERFECT_HIT_COLOR.toggled() && System.currentTimeMillis() - Hitreg.lastPerfectHit <= 500) cir.setReturnValue(true);
        else if (Toggle.JUMP_RESET_COLOR.toggled() && System.currentTimeMillis() - Hitreg.lastJumpReset <= 500) cir.setReturnValue(true);
    }
}