package you.jass.betterhitreg.mixin;

import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LocalPlayer.class)
public interface LocalPlayerAccessor {
    //the last sprint state the client sent to the server
    @Accessor("wasSprinting")
    boolean getWasSprinting();
}
