package com.jforex.programming.quote.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.OfferSide;
import com.google.common.collect.Sets;
import com.jforex.programming.quote.TickQuote;
import com.jforex.programming.quote.TickQuoteConsumer;
import com.jforex.programming.quote.TickQuoteProvider;
import com.jforex.programming.test.common.CurrencyUtilForTest;
import com.jforex.programming.test.fakes.ITickForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

@RunWith(HierarchicalContextRunner.class)
public class TickQuoteProviderTest extends CurrencyUtilForTest {

    private TickQuoteProvider tickQuoteProvider;

    @Mock
    private TickQuoteConsumer tickQuoteConsumerEURUSDMock;
    @Mock
    private TickQuoteConsumer tickQuoteConsumerAUDUSDMock;
    private Set<Instrument> subscribedInstruments;
    private Subject<TickQuote, TickQuote> tickObservable;
    private final ITick tickEURUSDOfHistory = new ITickForTest(1.23413, 1.23488);
    private final ITick firstTickEURUSD = new ITickForTest(1.23456, 1.23451);
    private final ITick secondTickEURUSD = new ITickForTest(1.32123, 1.32143);
    private final TickQuote firstEURUSDTickQuote = new TickQuote(instrumentEURUSD, firstTickEURUSD);
    private final TickQuote secondEURUSDTickQuote = new TickQuote(instrumentEURUSD, secondTickEURUSD);

    private final ITick firstTickAUDUSD = new ITickForTest(1.10345, 1.10348);
    private final TickQuote firstAUDUSDTickQuote = new TickQuote(instrumentAUDUSD, firstTickAUDUSD);

    @Before
    public void setUp() {
        initCommonTestFramework();
        setupMocks();
        tickObservable = PublishSubject.create();
        subscribedInstruments = createSet(instrumentEURUSD, instrumentAUDUSD);

        tickQuoteProvider = new TickQuoteProvider(tickObservable,
                                                  subscribedInstruments,
                                                  historyMock);
        tickQuoteProvider.subscribe(Sets.newHashSet(instrumentEURUSD), tickQuoteConsumerEURUSDMock::onTickQuote);
    }

    private void setupMocks() {
        try {
            when(historyMock.getLastTick(instrumentEURUSD)).thenReturn(tickEURUSDOfHistory);
            when(historyMock.getLastTick(instrumentAUDUSD)).thenReturn(firstTickAUDUSD);
        } catch (final JFException e) {}
    }

    private void verifyTickValues(final ITick tick) {
        assertThat(tickQuoteProvider.tick(instrumentEURUSD),
                   equalTo(tick));

        assertThat(tickQuoteProvider.ask(instrumentEURUSD),
                   equalTo(tick.getAsk()));

        assertThat(tickQuoteProvider.bid(instrumentEURUSD),
                   equalTo(tick.getBid()));

        assertThat(tickQuoteProvider.forOfferSide(instrumentEURUSD, OfferSide.ASK),
                   equalTo(tick.getAsk()));

        assertThat(tickQuoteProvider.forOfferSide(instrumentEURUSD, OfferSide.BID),
                   equalTo(tick.getBid()));
    }

    @Test
    public void testObservableIsCorrectReturned() {
        assertThat(tickQuoteProvider.observable(), equalTo(tickObservable));
    }

    public class HistoryThrowsAtInitialization {

        @Before
        public void setUp() throws JFException {
            when(historyMock.getLastTick(instrumentEURUSD)).thenThrow(jfException);
        }

        @Test(expected = Exception.class)
        public void testTickThrows() {
            tickQuoteProvider = new TickQuoteProvider(tickObservable, subscribedInstruments, historyMock);

            rxTestUtil.advanceTimeBy(5000L, TimeUnit.MILLISECONDS);
        }
    }

    public class HistoryReturnsTickBeforeFirstTickQuoteIsReceived {

        @Before
        public void setUp() throws JFException {
            when(historyMock.getLastTick(instrumentEURUSD)).thenReturn(tickEURUSDOfHistory);
        }

        @Test
        public void testTickReturnsHistoryTick() {
            verifyTickValues(tickEURUSDOfHistory);
        }
    }

    public class AfterFirstTickQuoteIsConsumed {

        @Before
        public void setUp() throws JFException {
            when(historyMock.getLastTick(instrumentEURUSD)).thenReturn(tickEURUSDOfHistory);
            tickObservable.onNext(firstEURUSDTickQuote);
        }

        @Test
        public void testTickReturnsFirstTick() {
            verifyTickValues(firstTickEURUSD);
        }

        @Test
        public void testTickQuoteConsumerReceivesFirstTickQuote() {
            verify(tickQuoteConsumerEURUSDMock).onTickQuote(firstEURUSDTickQuote);
        }

        @Test
        public void testNoInteractionWithAUDUSDConsumer() {
            verifyZeroInteractions(tickQuoteConsumerAUDUSDMock);
        }

        public class AfterSecondTickQuoteIsConsumed {

            @Before
            public void setUp() {
                tickObservable.onNext(secondEURUSDTickQuote);
            }

            @Test
            public void testTickReturnsSecondTick() {
                verifyTickValues(secondTickEURUSD);
            }

            @Test
            public void testTickQuoteConsumerReceivesSecondTickQuote() {
                verify(tickQuoteConsumerEURUSDMock).onTickQuote(secondEURUSDTickQuote);
            }

            @Test
            public void testTickQuoteConsumerForAUDUSDReceivesTickQuote() {
                tickQuoteProvider.subscribe(Sets.newHashSet(instrumentAUDUSD),
                                            tickQuoteConsumerAUDUSDMock::onTickQuote);

                tickObservable.onNext(firstAUDUSDTickQuote);

                verify(tickQuoteConsumerAUDUSDMock).onTickQuote(firstAUDUSDTickQuote);
            }
        }
    }
}
