package com.jforex.programming.position.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Collection;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.google.common.collect.Sets;
import com.jforex.programming.order.OrderUtil;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.position.Position;
import com.jforex.programming.position.PositionFactory;
import com.jforex.programming.position.PositionUtil;
import com.jforex.programming.test.common.InstrumentUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;

@RunWith(HierarchicalContextRunner.class)
public class PositionUtilTest extends InstrumentUtilForTest {

    private PositionUtil positionUtil;

    @Mock
    private OrderUtil orderUtilMock;
    @Mock
    private PositionFactory positionFactoryMock;
    @Mock
    private Position positionMock;
    private final String mergeOrderLabel = "mergeOrderLabel";
    private TestObserver<OrderEvent> testSubscriber;

    @Before
    public void setUp() {
        setUpMocks();

        positionUtil = new PositionUtil(orderUtilMock, positionFactoryMock);
    }

    private void setUpMocks() {
        when(positionFactoryMock.forInstrument(instrumentEURUSD))
            .thenReturn(positionMock);
    }

    private void setUpOrderUtilObservables(final Collection<IOrder> toMergeOrders,
                                           final Observable<OrderEvent> observable) {
        when(orderUtilMock.mergeOrders(mergeOrderLabel, toMergeOrders))
            .thenReturn(observable);
    }

    private void expectFilledOrders(final Set<IOrder> filledOrders) {
        when(positionMock.filled()).thenReturn(filledOrders);
    }

    @Test
    public void positionOrdersIsCorrect() {
        assertThat(positionUtil.positionOrders(instrumentEURUSD), equalTo(positionMock));
    }

    public class MergePositionTests {

        private Observable<OrderEvent> mergeObservable;

        @Before
        public void setUp() {
            mergeObservable = positionUtil.merge(instrumentEURUSD, mergeOrderLabel);
        }

        @Test
        public void observableIsDeferredWithNoInteractionsToMocks() {
            verifyZeroInteractions(orderUtilMock);
            verifyZeroInteractions(positionFactoryMock);
        }

        public class OnSubscribe {

            public void prepareToMergeOrdersAndSubscribe(final Set<IOrder> toMergeOrders) {
                expectFilledOrders(toMergeOrders);
                setUpOrderUtilObservables(toMergeOrders, emptyObservable());

                testSubscriber = mergeObservable.test();
            }

            @Test
            public void completesImmediatelyWhenNoOrdersForMerge() {
                prepareToMergeOrdersAndSubscribe(Sets.newHashSet());

                testSubscriber.assertComplete();
                verifyZeroInteractions(orderUtilMock);
            }

            @Test
            public void completesImmediatelyWhenOnlyOneOrderForMerge() {
                prepareToMergeOrdersAndSubscribe(Sets.newHashSet(buyOrderEURUSD));

                testSubscriber.assertComplete();
                verifyZeroInteractions(orderUtilMock);
            }

            @Test
            public void callOnOrderUtilWhenEnoughOrdersForMerge() {
                final Set<IOrder> toMergeOrders = Sets.newHashSet(buyOrderEURUSD, sellOrderEURUSD);
                prepareToMergeOrdersAndSubscribe(toMergeOrders);

                testSubscriber.assertComplete();
                verify(orderUtilMock).mergeOrders(mergeOrderLabel, toMergeOrders);
            }
        }
    }

    public class ClosePositionTests {

        private Observable<OrderEvent> closeObservable;

        @Before
        public void setUp() {
            closeObservable = positionUtil.close(instrumentEURUSD, mergeOrderLabel);
        }

        private void expectFilledOrOpenedOrders(final Set<IOrder> filledOrOpenedOrders) {
            when(positionMock.filledOrOpened()).thenReturn(filledOrOpenedOrders);
        }

        @Test
        public void observableIsDeferredWithNoInteractionsToMocks() {
            verifyZeroInteractions(orderUtilMock);
            verifyZeroInteractions(positionFactoryMock);
        }

        @Test
        public void observableCompletesImmediatelyWhenNoOrdersToClose() {
            expectFilledOrders(Sets.newHashSet());
            expectFilledOrOpenedOrders(Sets.newHashSet());

            testSubscriber = closeObservable.test();

            testSubscriber.assertNoValues();
            testSubscriber.assertComplete();
        }

        public class PositionHasOneFilledOrder {

            @Before
            public void setUp() {
                expectFilledOrders(Sets.newHashSet(buyOrderEURUSD));
                expectFilledOrOpenedOrders(Sets.newHashSet(buyOrderEURUSD));

                when(orderUtilMock.close(buyOrderEURUSD)).thenReturn(emptyObservable());

                testSubscriber = closeObservable.test();
            }

            @Test
            public void noMergeCall() {
                verify(orderUtilMock, never()).mergeOrders(any(), any());
            }

            @Test
            public void oneCloseCall() {
                verify(orderUtilMock).close(buyOrderEURUSD);
            }

            @Test
            public void subscriberCompletes() {
                testSubscriber.assertComplete();
            }
        }

        public class PositionHasOneOpenedOrder {

            @Before
            public void setUp() {
                expectFilledOrders(Sets.newHashSet());
                expectFilledOrOpenedOrders(Sets.newHashSet(buyOrderEURUSD));

                when(orderUtilMock.close(buyOrderEURUSD)).thenReturn(emptyObservable());

                testSubscriber = closeObservable.test();
            }

            @Test
            public void noMergeCall() {
                verify(orderUtilMock, never()).mergeOrders(any(), any());
            }

            @Test
            public void oneCloseCall() {
                verify(orderUtilMock).close(buyOrderEURUSD);
            }

            @Test
            public void subscriberCompletes() {
                testSubscriber.assertComplete();
            }
        }

        public class PositionHasTwoFilledOrders {

            private final Set<IOrder> toMergeOrders = Sets.newHashSet(buyOrderEURUSD, sellOrderEURUSD);

            @Before
            public void setUp() {
                expectFilledOrders(toMergeOrders);
            }

            @Test
            public void mergeIsCalled() {
                when(orderUtilMock.mergeOrders(mergeOrderLabel, toMergeOrders))
                    .thenReturn(emptyObservable());

                testSubscriber = closeObservable.test();

                verify(orderUtilMock).mergeOrders(mergeOrderLabel, toMergeOrders);
            }

            public class MergeNeverCompletes {

                @Before
                public void setUp() {
                    when(orderUtilMock.mergeOrders(mergeOrderLabel, toMergeOrders))
                        .thenReturn(neverObservable());

                    testSubscriber = closeObservable.test();
                }

                @Test
                public void subscriberNotCompleted() {
                    testSubscriber.assertNotComplete();
                }
            }

            public class MergeEmitsError {

                @Before
                public void setUp() {
                    when(orderUtilMock.mergeOrders(mergeOrderLabel, toMergeOrders))
                        .thenReturn(errorObservable());

                    expectFilledOrOpenedOrders(toMergeOrders);

                    testSubscriber = closeObservable.test();
                }

                @Test
                public void noCloseCalls() {
                    verify(orderUtilMock, never()).close(any());
                }

                @Test
                public void subscriberErrors() {
                    testSubscriber.assertError(jfException);
                }
            }

            public class MergeCompletes {

                @Before
                public void setUp() {
                    when(orderUtilMock.mergeOrders(mergeOrderLabel, toMergeOrders))
                        .thenReturn(emptyObservable());
                }

                public class NoFilledOrOpenedOrders {

                    @Before
                    public void setUp() {
                        expectFilledOrOpenedOrders(Sets.newHashSet());

                        testSubscriber = closeObservable.test();
                    }

                    @Test
                    public void subscriberCompleted() {
                        testSubscriber.assertComplete();
                    }

                    @Test
                    public void noCloseCall() {
                        verify(orderUtilMock, never()).close(any());
                    }
                }

                public class TwoFilledOrOpenedOrders {

                    private final Set<IOrder> toCloseOrders = toMergeOrders;

                    @Before
                    public void setUp() {
                        expectFilledOrOpenedOrders(toCloseOrders);
                    }

                    public class CloseCallDoNotComplete {

                        @Before
                        public void setUp() {
                            when(orderUtilMock.close(buyOrderEURUSD)).thenReturn(neverObservable());
                            when(orderUtilMock.close(sellOrderEURUSD)).thenReturn(neverObservable());

                            testSubscriber = closeObservable.test();
                        }

                        @Test
                        public void subscriberNotCompleted() {
                            testSubscriber.assertNotComplete();
                        }

                        @Test
                        public void closeCallsAreNotConcatenated() {
                            verify(orderUtilMock).close(buyOrderEURUSD);
                            verify(orderUtilMock).close(sellOrderEURUSD);
                        }
                    }

                    public class CloseCallsSucceed {

                        @Before
                        public void setUp() {
                            when(orderUtilMock.close(any())).thenReturn(emptyObservable());

                            testSubscriber = closeObservable.test();
                        }

                        @Test
                        public void subscriberCompleted() {
                            testSubscriber.assertComplete();
                        }

                        @Test
                        public void twoCloseCalls() {
                            verify(orderUtilMock).close(buyOrderEURUSD);
                            verify(orderUtilMock).close(sellOrderEURUSD);
                        }
                    }
                }
            }
        }
    }
}
