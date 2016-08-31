package com.jforex.programming.order.process;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEventType;

public class SetGTTProcess extends OrderProcess {

    private final IOrder order;
    private final long newGTT;

    private SetGTTProcess(final Builder builder) {
        super(builder);
        order = builder.order;
        newGTT = builder.newGTT;
    }

    public final IOrder order() {
        return order;
    }

    public final long newGTT() {
        return newGTT;
    }

    public static final ChangeOption<SetGTTProcess> forParams(final IOrder order,
                                                              final long newGTT) {
        return new Builder(checkNotNull(order), checkNotNull(newGTT));
    }

    private static class Builder extends CommonBuilder<Builder> implements ChangeOption<SetGTTProcess> {

        private final IOrder order;
        private final long newGTT;

        private Builder(final IOrder order,
                        final long newGTT) {
            this.order = order;
            this.newGTT = newGTT;
        }

        @Override
        public ChangeOption<SetGTTProcess> onReject(final Consumer<IOrder> rejectAction) {
            eventHandlerForType.put(OrderEventType.CHANGE_GTT_REJECTED, checkNotNull(rejectAction));
            return this;
        }

        @Override
        public ChangeOption<SetGTTProcess> onDone(final Consumer<IOrder> okAction) {
            eventHandlerForType.put(OrderEventType.CHANGED_GTT, checkNotNull(okAction));
            return this;
        }

        @Override
        public SetGTTProcess build() {
            return new SetGTTProcess(this);
        }
    }
}