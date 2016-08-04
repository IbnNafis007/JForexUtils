package com.jforex.programming.math;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.Function;

import com.dukascopy.api.ICurrency;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.OfferSide;

public class ConversionBuilder {

    private Builder builder;
    protected final Function<ConversionBuilder, Double> consumer;

    public interface FromSource {
        public ToInstrument fromInstrument(Instrument instrument);

        public ToCurrency fromCurrency(ICurrency currency);
    }

    public interface ToInstrument {
        public ForOfferSide toInstrument(Instrument instrument);
    }

    public interface ForOfferSide {
        public double forOfferSide(OfferSide offerSide);
    }

    public interface ToCurrency {
        public ForOfferSide toCurrency(ICurrency currency);
    }

    public ConversionBuilder(final Function<ConversionBuilder, Double> consumer) {
        this.consumer = consumer;
    }

    public final FromSource convertAmount(final double amount) {
        builder = new Builder(amount);
        return builder;
    }

    public final double amount() {
        return this.builder.amount;
    }

    public final ICurrency sourceCurrency() {
        return this.builder.sourceCurrency;
    }

    public final ICurrency targetCurrency() {
        return this.builder.targetCurrency;
    }

    public final OfferSide offerSide() {
        return this.builder.offerSide;
    }

    private final class Builder implements
                                FromSource,
                                ToInstrument,
                                ToCurrency,
                                ForOfferSide {

        private final double amount;
        private ICurrency sourceCurrency;
        private ICurrency targetCurrency;
        private OfferSide offerSide;

        private Builder(final double amount) {
            this.amount = amount;
        }

        @Override
        public final ToInstrument fromInstrument(final Instrument sourceInstrument) {
            this.sourceCurrency = checkNotNull(sourceInstrument).getPrimaryJFCurrency();
            return this;
        }

        @Override
        public final ToCurrency fromCurrency(final ICurrency currency) {
            this.sourceCurrency = checkNotNull(currency);
            return this;
        }

        @Override
        public final ForOfferSide toInstrument(final Instrument targetInstrument) {
            this.targetCurrency = checkNotNull(targetInstrument).getPrimaryJFCurrency();
            return this;
        }

        @Override
        public final ForOfferSide toCurrency(final ICurrency currency) {
            this.targetCurrency = checkNotNull(currency);
            return this;
        }

        @Override
        public final double forOfferSide(final OfferSide offerSide) {
            this.offerSide = checkNotNull(offerSide);
            return consumer.apply(ConversionBuilder.this);
        }
    }
}
