package you.jass.betterhitreg.mixin;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.chunk.ChunkSectionsToRender;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.client.renderer.culling.Frustum;
import org.joml.Matrix4fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import you.jass.betterhitreg.hitreg.Hitreg;
import you.jass.betterhitreg.settings.Toggle;

@Mixin(LevelRenderer.class)
public class ChunkMixin {
    //version 1.20.1-
//    @Shadow
//    private ObjectArrayList renderChunksInFrustum;

    //version 1.20.2+
//    @Shadow
//    private ObjectArrayList<SectionRenderDispatcher.RenderSection> visibleSections;

    //version 1.21.11-
//    @Inject(method = "applyFrustum", at = @At("TAIL"))
//    private void onApplyFrustum(Frustum frustum, CallbackInfo ci) {
//        if (Toggle.VOID_WORLD.toggled() && Hitreg.client.player != null && Hitreg.client.level != null) {
//            //version 1.20.1-
//            //renderChunksInFrustum.clear();
//
//            //version 1.20.2+
//            visibleSections.clear();
//        }
//    }

    //version 26+
    @Inject(method = "prepareChunkRenders", at = @At("HEAD"))
    private void onPrepareChunkRenders(Matrix4fc modelViewMatrix, CallbackInfoReturnable<ChunkSectionsToRender> cir) {
        if (Toggle.VOID_WORLD.toggled() && Hitreg.client.player != null && Hitreg.client.level != null) visibleSections.clear();
    }
}