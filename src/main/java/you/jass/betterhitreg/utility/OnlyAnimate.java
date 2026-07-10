package you.jass.betterhitreg.utility;

import net.minecraft.world.damagesource.DamageSource;

public class OnlyAnimate extends DamageSource {
    public final DamageSource wrapped;

    public OnlyAnimate(DamageSource wrapped) {
        super(wrapped.typeHolder());
        this.wrapped = wrapped;
    }
}