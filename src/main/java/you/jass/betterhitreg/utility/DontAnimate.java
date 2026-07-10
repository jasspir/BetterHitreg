package you.jass.betterhitreg.utility;

import net.minecraft.world.damagesource.DamageSource;

public class DontAnimate extends DamageSource {
    public final DamageSource wrapped;

    public DontAnimate(DamageSource wrapped) {
        super(wrapped.typeHolder());
        this.wrapped = wrapped;
    }
}