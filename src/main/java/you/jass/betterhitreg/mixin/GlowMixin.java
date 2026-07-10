package you.jass.betterhitreg.mixin;

import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import you.jass.betterhitreg.hitreg.Hitreg;
import you.jass.betterhitreg.settings.Toggle;
import you.jass.betterhitreg.utility.Render;

@Mixin(Entity.class)
public abstract class GlowMixin {
    @Shadow private int id;
    @Inject(method = "getTeamColor", at = @At("RETURN"), cancellable = true)
    public void glow(CallbackInfoReturnable<Integer> cir) {
        if (!Hitreg.withinFight || id != Hitreg.target.getId() || Toggle.RENDER_HITBOX.toggled() || Toggle.RENDER_SERVER_HITBOX.toggled()) return;

        //this uses bgr instead of rgb for some reason so we convert it first
        if (Toggle.PERFECT_HIT_COLOR.toggled() && System.currentTimeMillis() - Hitreg.lastPerfectHit <= 500) cir.setReturnValue((Render.PERFECT_HIT_GLOW & 0xFF00FF00) | ((Render.PERFECT_HIT_GLOW & 0xFF) << 16) | ((Render.PERFECT_HIT_GLOW >> 16) & 0xFF));
        else if (Toggle.JUMP_RESET_COLOR.toggled() && System.currentTimeMillis() - Hitreg.lastJumpReset <= 500) cir.setReturnValue((Render.JUMP_RESET_GLOW & 0xFF00FF00) | ((Render.JUMP_RESET_GLOW & 0xFF) << 16) | ((Render.JUMP_RESET_GLOW >> 16) & 0xFF));
    }

    @Inject(method = "isCurrentlyGlowing", at = @At("RETURN"), cancellable = true)
    public void isGlow(CallbackInfoReturnable<Boolean> cir) {
        if (!Hitreg.withinFight || id != Hitreg.target.getId() || Toggle.RENDER_HITBOX.toggled() || Toggle.RENDER_SERVER_HITBOX.toggled()) return;
        if (Toggle.PERFECT_HIT_COLOR.toggled() && System.currentTimeMillis() - Hitreg.lastPerfectHit <= 500) cir.setReturnValue(true);
        else if (Toggle.JUMP_RESET_COLOR.toggled() && System.currentTimeMillis() - Hitreg.lastJumpReset <= 500) cir.setReturnValue(true);
    }
}