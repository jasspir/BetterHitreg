package you.jass.betterhitreg.mixin;

import com.mojang.blaze3d.audio.Channel;

import java.util.Map;
import java.util.WeakHashMap;

/** Keeps filter settings keyed by channel until its deferred playback callback runs. */
final class AudioFilterContext {
    record Settings(float muffle, float sharpen) {}

    private static final Map<Channel, Settings> PENDING = new WeakHashMap<>();

    private AudioFilterContext() {}

    static Settings create(float muffle, float sharpen) {
        float safeMuffle = clamp01(muffle);
        float safeSharpen = clamp01(sharpen);
        return safeMuffle > 0.0F || safeSharpen > 0.0F
                ? new Settings(safeMuffle, safeSharpen)
                : null;
    }

    static synchronized void bind(Channel channel, Settings settings) {
        if (settings == null) {
            PENDING.remove(channel);
        } else {
            PENDING.put(channel, settings);
        }
    }

    static synchronized Settings take(Channel channel) {
        return PENDING.remove(channel);
    }

    static synchronized void clear(Channel channel) {
        PENDING.remove(channel);
    }

    private static float clamp01(float value) {
        if (Float.isNaN(value)) return 0.0F;
        return Math.max(0.0F, Math.min(1.0F, value));
    }
}
