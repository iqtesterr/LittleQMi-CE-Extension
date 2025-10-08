package com.chiiblock.plugin.ce.extension.block.countdown;

import com.chiiblock.plugin.ce.extension.block.entity.CountdownBlockEntity;

import java.util.HashMap;

public class TickWheel {
    private final CountdownModule module;
    private final Wheel[] wheels = {new Wheel(0), new Wheel(6), new Wheel(12)};
    private final HashMap<CountdownBlockEntity, Handle> map = new HashMap<>();

    public TickWheel(CountdownModule module) {
        this.module = module;
    }

    public void tick() {
        Wheel w0 = wheels[0];
        int idx = (int) (module.currentTick() & w0.mask);
        Bucket b = w0.buckets[idx];

        Handle h = b.head;
        b.head = null;

        while (h != null) {
            Handle nx = h.next;
            h.unbucket();

            if (h.target <= module.currentTick()) {
                //h.be.execute(module.currentTick());
                map.remove(h.be); // 触发后移除
            } else {
                place(h, module.currentTick());
            }
            h = nx;
        }

        if ((module.currentTick() & wheels[1].mask) == 0)
            cascade(1, module.currentTick());
        if ((module.currentTick() & wheels[2].mask) == 0)
            cascade(2, module.currentTick());
    }

    public void registerOnce(CountdownBlockEntity be, long delayTicks) {
        Handle h = map.get(be);
        if (h == null) {
            h = new Handle(be);
            map.put(be, h);
        } else if (h.bucket != null) {
            h.detach();
        }
        h.target = Math.max(0L, delayTicks) + module.currentTick();
        place(h, module.currentTick());
    }

    public void unregister(CountdownBlockEntity be) {
        Handle h = map.remove(be);
        if (h != null) h.detach();
    }

    public void clear() {
        map.clear();
        for (Wheel w : wheels) for (Bucket b : w.buckets) b.head = null;
    }

    private void place(Handle h, long nowTick) {
        if (h.bucket != null) h.detach();

        long dt = Math.max(0, h.target - nowTick);
        int tier = (dt < 64) ? 0 : (dt < 4096 ? 1 : 2);

        Wheel w = wheels[tier];
        int idx = (int) ((h.target >>> w.shift) & w.mask);
        Bucket b = w.buckets[idx];

        h.bucket = b;
        h.next = b.head;
        if (h.next != null) h.next.prev = h;
        b.head = h;
    }

    private void cascade(int lvl, long nowTick) {
        Wheel w = wheels[lvl];
        int idx = (int) ((nowTick >>> w.shift) & w.mask);
        Bucket b = w.buckets[idx];

        Handle h = b.head;
        b.head = null;

        while (h != null) {
            Handle nx = h.next;
            h.unbucket();
            place(h, nowTick);
            h = nx;
        }
    }

    private static final class Handle {
        final CountdownBlockEntity be;
        long target;
        Bucket bucket; Handle prev, next;

        Handle(CountdownBlockEntity be) {
            this.be = be;
        }

        void unbucket() {
            prev = next = null; bucket = null;
        }

        void detach() {
            if (bucket != null) {
                if (prev != null) prev.next = next;
                if (next != null) next.prev = prev;
                if (bucket.head == this) bucket.head = next;
                unbucket();
            }
        }
    }

    private static final class Wheel {
        final int shift, size=64, mask=size-1;
        final Bucket[] buckets = new Bucket[size];
        Wheel(int s) {
            shift=s;
            for (int i=0;i<size;i++) {
                buckets[i] = new Bucket();
            };
        }
    }
    private static final class Bucket {
        Handle head;
    }
}