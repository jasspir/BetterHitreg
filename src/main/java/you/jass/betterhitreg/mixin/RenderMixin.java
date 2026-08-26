package you.jass.betterhitreg.mixin;

import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import you.jass.betterhitreg.hitreg.Hitreg;
import you.jass.betterhitreg.settings.Toggle;
import you.jass.betterhitreg.utility.MultiVersion;

import static you.jass.betterhitreg.hitreg.Hitreg.*;

@Mixin(EntityRenderer.class)
public abstract class RenderMixin {
    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private void entity(Entity entity, Frustum frustum, double x, double y, double z, CallbackInfoReturnable<Boolean> cir) {
        if (client.player == null) {
            cir.setReturnValue(true);
            return;
        }

        if (Toggle.HIDE_OTHER_FIGHTS.toggled()) cir.setReturnValue(shouldRenderPlayer(entity));
    }

    @Unique
    private boolean shouldRenderPlayer(Entity entity) {
        if (!(entity instanceof Player || entity instanceof Display.TextDisplay) || entity.getId() == lastTarget || !Hitreg.withinFight || System.currentTimeMillis() - lastAttack > 5000) return true;
        Vec3 position = MultiVersion.getBasePosition(entity);
        return distanceFromPlayer(position) <= 5 || distanceFromTarget(position) <= 5 || distanceToTarget() > 10;
    }
}