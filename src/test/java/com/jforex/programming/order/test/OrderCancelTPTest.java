package com.jforex.programming.order.test;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.google.common.collect.Sets;
import com.jforex.programming.order.MergeCommand;
import com.jforex.programming.order.OrderCancelTP;
import com.jforex.programming.order.OrderChangeBatch;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.test.common.InstrumentUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Observable;
import io.reactivex.functions.Function;
import io.reactivex.observers.TestObserver;

@RunWith(HierarchicalContextRunner.class)
public class OrderCancelTPTest extends InstrumentUtilForTest {

    private OrderCancelTP orderCancelTP;

    private final Set<IOrder> toCancelTPOrders = Sets.newHashSet(buyOrderEURUSD, sellOrderEURUSD);

    @Mock
    private OrderChangeBatch orderChangeBatchMock;
    @Mock
    private MergeCommand mergeCommandMock;
    @Mock
    private Function<Observable<OrderEvent>, Observable<OrderEvent>> orderCancelTPComposerMock;

    @Before
    public void setUp() {
        when(mergeCommandMock.orderCancelTPComposer(any())).thenReturn(orderCancelTPComposerMock);

        orderCancelTP = new OrderCancelTP(orderChangeBatchMock);
    }

    @Test
    public void observeIsDeferred() {
        orderCancelTP.observe(toCancelTPOrders, mergeCommandMock);

        verifyZeroInteractions(orderChangeBatchMock);
    }

    public class WhenSubscribedTests {

        private TestObserver<OrderEvent> testObserver;
        private final OrderEvent event = new OrderEvent(buyOrderEURUSD,
                                                        OrderEventType.SUBMIT_CONDITIONAL_OK,
                                                        true);

        @Before
        public void setUp() {
            when(orderChangeBatchMock.cancelTP(eq(toCancelTPOrders), any()))
                .thenReturn(eventObservable(event));

            testObserver = orderCancelTP
                .observe(toCancelTPOrders, mergeCommandMock)
                .test();
        }

        @Test
        public void subscribeCallsChangeBatchWithCorrectParams() {
            verify(orderChangeBatchMock)
                .cancelTP(eq(toCancelTPOrders),
                          argThat(c -> {
                              try {
                                  return orderCancelTPComposerMock.equals(c.apply(buyOrderEURUSD));
                              } catch (final Exception e) {
                                  return false;
                              }
                          }));
        }

        @Test
        public void observableFromChangeBatchIsReturned() {
            testObserver.assertValue(event);
            testObserver.assertComplete();
        }
    }
}