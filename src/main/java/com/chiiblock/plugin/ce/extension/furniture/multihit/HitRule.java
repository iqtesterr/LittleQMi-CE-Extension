package com.chiiblock.plugin.ce.extension.furniture.multihit;

import java.util.List;

public class HitRule {
    private final int hits;
    private final int[] intervals;

    private HitRule(int hits, int[] intervals) {
        this.hits = hits;
        this.intervals = intervals;
    }

    public int hits() {
        return hits;
    }

    public int intervals(int hit) {
        if (hit >= intervals.length) return intervals[intervals.length - 1];
        return intervals[hit - 1];
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private int hits;
        private int[] intervals;

        public HitRule build() {
            return new HitRule(hits, intervals);
        }

        public Builder hits(int hits) {
            this.hits = hits;
            return this;
        }

        public Builder intervals(int[] intervals) {
            this.intervals = intervals;
            return this;
        }

        public Builder intervals(List<Integer> intervals) {
            this.intervals = new int[intervals.size()];
            for (int i = 0; i < intervals.size(); i++) {
                this.intervals[i] = intervals.get(i);
            }
            return this;
        }
    }
}
