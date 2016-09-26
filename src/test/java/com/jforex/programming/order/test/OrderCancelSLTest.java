package com.jforex.programming.order.test;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.google.common.collect.Sets;
import com.jforex.programming.order.BatchMode;
import com.jforex.programming.order.OrderCancelSL;
import com.jforex.programming.order.OrderChangeBatch;
import com.jforex.programming.order.command.MergeCommand;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.test.common.InstrumentUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Observable;
import io.reactivex.functions.Function;
import io.reactivex.observers.TestObserver;

@RunWith(HierarchicalContextRunner.class)
public class OrderCancelSLTest extends InstrumentUtilForTest {

    private OrderCancelSL orderCancelSL;

    private final Set<IOrder> toCancelSLOrders = Sets.newHashSet(buyOrderEURUSD, sellOrderEURUSD);

    @Mock
    private OrderChangeBatch orderChangeBatchMock;
    @Mock
    private MergeCommand mergeCommandMock;
    @Mock
    private Function<Observable<OrderEvent>, Observable<OrderEvent>> orderCancelSLComposerMock;

    @Before
    public void setUp() {
        when(mergeCommandMock.orderCancelSLComposer(any())).thenReturn(orderCancelSLComposerMock);
        when(mergeCommandMock.orderCancelSLMode()).thenReturn(BatchMode.MERGE);

        orderCancelSL = new OrderCancelSL(orderChangeBatchMock);
    }

    @Test
    public void observeIsDeferred() {
        orderCancelSL.observe(toCancelSLOrders, mergeCommandMock);

        verifyZeroInteractions(orderChangeBatchMock);
    }

    public class WhenSubscribedTests {

        private TestObserver<OrderEvent> testObserver;
        private final OrderEvent event = submitEvent;

        @Before
        public void setUp() {
            when(orderChangeBatchMock.cancelSL(eq(toCancelSLOrders),
                                               eq(BatchMode.MERGE),
                                               any()))
                                                   .thenReturn(eventObservable(event));

            testObserver = orderCancelSL
                .observe(toCancelSLOrders, mergeCommandMock)
                .test();
        }

        @Test
        public void subscribeCallsChangeBatchWithCorrecSLarams() {
            verify(orderChangeBatchMock)
                .cancelSL(eq(toCancelSLOrders),
                          eq(BatchMode.MERGE),
                          argThat(c -> {
                              Observable
                                  .fromCallable(() -> c.apply(buyOrderEURUSD))
                                  .test()
                                  .assertValue(orderCancelSLComposerMock);
                              return true;
                          }));
        }

        @Test
        public void observableFromChangeBatchIsReturned() {
            testObserver.assertValue(event);
            testObserver.assertComplete();
        }
    }
}
