package com.jforex.programming.order.process;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;

public class MergePositionProcess extends OrderProcess {

    private final String mergeOrderLabel;
    private final Instrument instrument;

    public interface Option extends MergeOption<Option> {
        public MergePositionProcess build();
    }

    private MergePositionProcess(final Builder builder) {
        super(builder);
        mergeOrderLabel = builder.mergeOrderLabel;
        instrument = builder.instrument;
    }

    public final String mergeOrderLabel() {
        return mergeOrderLabel;
    }

    public final Instrument instrument() {
        return instrument;
    }

    public static final Option forParams(final String mergeOrderLabel,
                                         final Instrument instrument) {
        return new Builder(checkNotNull(mergeOrderLabel), checkNotNull(instrument));
    }

    private static class Builder extends CommonBuilder<Builder> implements Option {

        private final String mergeOrderLabel;
        private final Instrument instrument;

        private Builder(final String mergeOrderLabel,
                        final Instrument instrument) {
            this.mergeOrderLabel = mergeOrderLabel;
            this.instrument = instrument;
        }

        @Override
        public Option onRemoveSLReject(final Consumer<IOrder> setSLRejectAction) {
            putRemoveSLReject(setSLRejectAction);
            return this;
        }

        @Override
        public Option onRemoveTPReject(final Consumer<IOrder> setTPRejectAction) {
            putRemoveTPReject(setTPRejectAction);
            return this;
        }

        @Override
        public Option onRemoveSL(final Consumer<IOrder> changedSLAction) {
            putRemoveSL(changedSLAction);
            return this;
        }

        @Override
        public Option onRemoveTP(final Consumer<IOrder> changedTPAction) {
            putRemoveSL(changedTPAction);
            return this;
        }

        @Override
        public Option onMergeReject(final Consumer<IOrder> mergeRejectAction) {
            putMergeReject(mergeRejectAction);
            return this;
        }

        @Override
        public Option onMerge(final Consumer<IOrder> mergedAction) {
            putMerge(mergedAction);
            return this;
        }

        @Override
        public Option onMergeClose(final Consumer<IOrder> mergeClosedAction) {
            putMergeClose(mergeClosedAction);
            return this;
        }

        @Override
        public MergePositionProcess build() {
            return new MergePositionProcess(this);
        }
    }
}