package you.jass.betterhitreg.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import you.jass.betterhitreg.settings.Toggle;
import you.jass.betterhitreg.utility.InputTracker;

@Mixin(value = Entity.class, priority = Integer.MAX_VALUE)
public class EntityMixin {
    @Unique private float checkYaw;
    @Unique private float checkPitch;
    @Unique private double checkTurnYaw;
    @Unique private double checkTurnPitch;

    @Inject(method = "turn", at = @At("HEAD"), order = Integer.MAX_VALUE)
    private void head(double yaw, double pitch, CallbackInfo ci) {
        Entity entity = (Entity) (Object) this;
        if (!Toggle.DEBUG_INPUTS.toggled() || Minecraft.getInstance().player != entity) return;
        checkYaw = entity.getYRot();
        checkPitch = entity.getXRot();
        checkTurnYaw = yaw;
        checkTurnPitch = pitch;
        InputTracker.checkTurn(yaw, pitch);
    }

    @Inject(method = "turn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;setXRot(F)V", ordinal = 0, shift = At.Shift.AFTER), order = Integer.MAX_VALUE)
    private void afterXRot(double yaw, double pitch, CallbackInfo ci) {
        Entity entity = (Entity) (Object) this;
        if (!Toggle.DEBUG_INPUTS.toggled() || Minecraft.getInstance().player != entity || !InputTracker.hasExpectedTurn()) return;
        float expected = checkPitch + (float) checkTurnPitch * 0.15F;
        if (Math.abs(entity.getXRot() - expected) > 0.0001F) InputTracker.flagMouseY();
    }

    @Inject(method = "turn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;setYRot(F)V", ordinal = 0, shift = At.Shift.AFTER), order = Integer.MAX_VALUE)
    private void afterYRot(double yaw, double pitch, CallbackInfo ci) {
        Entity entity = (Entity) (Object) this;
        if (!Toggle.DEBUG_INPUTS.toggled() || Minecraft.getInstance().player != entity || !InputTracker.hasExpectedTurn()) return;
        float expected = checkYaw + (float) checkTurnYaw * 0.15F;
        if (Math.abs(entity.getYRot() - expected) > 0.0001F) InputTracker.flagMouseX();
    }

    @Inject(method = "turn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;setXRot(F)V", ordinal = 1, shift = At.Shift.AFTER), order = Integer.MAX_VALUE)
    private void afterClamp(double yaw, double pitch, CallbackInfo ci) {
        Entity entity = (Entity) (Object) this;
        if (!Toggle.DEBUG_INPUTS.toggled() || Minecraft.getInstance().player != entity || !InputTracker.hasExpectedTurn()) return;
        float expected = Math.max(-90.0F, Math.min(90.0F, checkPitch + (float) checkTurnPitch * 0.15F));
        if (Math.abs(entity.getXRot() - expected) > 0.0001F) InputTracker.flagMouseY();
    }

    @Inject(method = "turn", at = @At("TAIL"), order = Integer.MAX_VALUE)
    private void tail(double yaw, double pitch, CallbackInfo ci) {
        Entity entity = (Entity) (Object) this;
        if (!Toggle.DEBUG_INPUTS.toggled() || Minecraft.getInstance().player != entity || !InputTracker.hasExpectedTurn()) return;
        float expectedYaw = checkYaw + (float) checkTurnYaw * 0.15F;
        float expectedPitch = Math.max(-90.0F, Math.min(90.0F, checkPitch + (float) checkTurnPitch * 0.15F));
        if (Math.abs(entity.getYRot() - expectedYaw) > 0.0001F) InputTracker.flagMouseX();
        if (Math.abs(entity.getXRot() - expectedPitch) > 0.0001F) InputTracker.flagMouseY();
        InputTracker.clearExpectedTurn();
    }
}