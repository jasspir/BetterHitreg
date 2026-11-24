package you.jass.betterhitreg.utility;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.Objects;

/** Identity-keyed packet timestamp cache with TTL expiration. */
final class PacketStampBuffer<T> {
    private record Entry<T>(T packet, long timestamp) {}

    private final long ttlMillis;
    private final Deque<Entry<T>> orderedEntries = new ArrayDeque<>();
    private final IdentityHashMap<T, Entry<T>> latestByPacket = new IdentityHashMap<>();

    PacketStampBuffer(long ttlMillis) {
        if (ttlMillis <= 0L) throw new IllegalArgumentException("ttlMillis must be positive");
        this.ttlMillis = ttlMillis;
    }

    synchronized void stamp(T packet, long now) {
        Objects.requireNonNull(packet, "packet");
        pruneExpired(now);
        Entry<T> entry = new Entry<>(packet, now);
        orderedEntries.addLast(entry);
        latestByPacket.put(packet, entry);
    }

    synchronized long receivedAt(T packet, long now, long fallback) {
        Objects.requireNonNull(packet, "packet");
        pruneExpired(now);
        Entry<T> entry = latestByPacket.remove(packet);
        return entry == null ? fallback : entry.timestamp();
    }

    synchronized void pruneExpired(long now) {
        while (!orderedEntries.isEmpty()
                && now - orderedEntries.peekFirst().timestamp() > ttlMillis) {
            Entry<T> expired = orderedEntries.removeFirst();
            // An older stamp must not remove a newer entry for the same packet.
            if (latestByPacket.get(expired.packet()) == expired) {
                latestByPacket.remove(expired.packet());
            }
        }
    }

}
