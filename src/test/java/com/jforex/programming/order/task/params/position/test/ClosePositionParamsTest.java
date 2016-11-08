package com.jforex.programming.order.task.params.position.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventTransformer;
import com.jforex.programming.order.event.OrderToEventTransformer;
import com.jforex.programming.order.task.BatchMode;
import com.jforex.programming.order.task.CloseExecutionMode;
import com.jforex.programming.order.task.params.basic.CloseParams;
import com.jforex.programming.order.task.params.position.SimpleClosePositionParams;
import com.jforex.programming.order.task.params.position.MergePositionParams;
import com.jforex.programming.test.common.InstrumentUtilForTest;

import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;
import io.reactivex.functions.Function;
import io.reactivex.observers.TestObserver;

public class ClosePositionParamsTest extends InstrumentUtilForTest {

    private SimpleClosePositionParams positionParams;

    @Mock
    private MergePositionParams mergeParamsMock;
    @Mock
    private Function<IOrder, CloseParams> closeParamsPriovderMock;
    private final OrderEvent testEvent = closeEvent;
    private final OrderEvent composerEvent = changedLabelEvent;
    private final OrderEventTransformer testComposer =
            upstream -> upstream.flatMap(orderEvent -> Observable.just(composerEvent));
    private final OrderToEventTransformer testOrderComposer =
            order -> upstream -> upstream
                .flatMap(orderEvent -> Observable.just(composerEvent));

    private void assertComposerIsNeutral(final ObservableTransformer<OrderEvent,
                                                                     OrderEvent> composer) {
        final TestObserver<OrderEvent> testObserver = Observable
            .just(testEvent)
            .compose(composer)
            .test();

        testObserver.assertComplete();
        testObserver.assertValue(testEvent);
    }

    private void assertComposerEmitsComposerEvent(final ObservableTransformer<OrderEvent,
                                                                              OrderEvent> composer) {
        final TestObserver<OrderEvent> testObserver = Observable
            .just(testEvent)
            .compose(composer)
            .test();

        testObserver.assertComplete();
        testObserver.assertValue(composerEvent);
    }

    @Test
    public void defaultParamsValuesAreCorrect() {
        positionParams = SimpleClosePositionParams
            .newBuilder(instrumentEURUSD, closeParamsPriovderMock)
            .closeOpenedComposer(testComposer, BatchMode.MERGE)
            .build();

        assertThat(positionParams.instrument(), equalTo(instrumentEURUSD));
        assertThat(positionParams.closeParamsProvider(), equalTo(closeParamsPriovderMock));
        assertFalse(positionParams.maybeMergeParams().isPresent());
        assertThat(positionParams.closeBatchMode(), equalTo(BatchMode.MERGE));
        assertComposerIsNeutral(positionParams.singleCloseComposer(buyOrderEURUSD));
        assertComposerIsNeutral(positionParams.singleCloseComposer(buyOrderEURUSD));
        assertComposerIsNeutral(positionParams.closeAllComposer());
        assertComposerIsNeutral(positionParams.closeFilledComposer());
    }

    @Test
    public void definedValuesForCloseFilledAreCorrect() {
        positionParams = SimpleClosePositionParams
            .newBuilder(instrumentEURUSD, closeParamsPriovderMock)
            .singleCloseComposer(testOrderComposer)
            .closeFilledComposer(testComposer, BatchMode.CONCAT)
            .withMergeParams(mergeParamsMock)
            .build();

        assertThat(positionParams.instrument(), equalTo(instrumentEURUSD));
        assertThat(positionParams.closeParamsProvider(), equalTo(closeParamsPriovderMock));
        assertTrue(positionParams.maybeMergeParams().isPresent());
        assertThat(positionParams.closeBatchMode(), equalTo(BatchMode.CONCAT));
        assertThat(positionParams.executionMode(), equalTo(CloseExecutionMode.CloseFilled));
        assertComposerIsNeutral(positionParams.closeAllComposer());
        assertComposerIsNeutral(positionParams.closeOpenedComposer());
        assertComposerEmitsComposerEvent(positionParams.singleCloseComposer(buyOrderEURUSD));
        assertComposerEmitsComposerEvent(positionParams.closeFilledComposer());
    }

    @Test
    public void definedValuesForCloseFilledOrOpenedAreCorrect() {
        positionParams = SimpleClosePositionParams
            .newBuilder(instrumentEURUSD, closeParamsPriovderMock)
            .singleCloseComposer(testOrderComposer)
            .closeAllComposer(testComposer, BatchMode.MERGE)
            .withMergeParams(mergeParamsMock)
            .build();

        assertThat(positionParams.instrument(), equalTo(instrumentEURUSD));
        assertThat(positionParams.closeParamsProvider(), equalTo(closeParamsPriovderMock));
        assertTrue(positionParams.maybeMergeParams().isPresent());
        assertThat(positionParams.executionMode(), equalTo(CloseExecutionMode.CloseAll));
        assertComposerIsNeutral(positionParams.closeFilledComposer());
        assertComposerIsNeutral(positionParams.closeOpenedComposer());
        assertComposerEmitsComposerEvent(positionParams.closeAllComposer());
    }

    @Test
    public void definedValuesForCloseOpenedAreCorrect() {
        positionParams = SimpleClosePositionParams
            .newBuilder(instrumentEURUSD, closeParamsPriovderMock)
            .singleCloseComposer(testOrderComposer)
            .closeOpenedComposer(testComposer, BatchMode.MERGE)
            .build();

        assertThat(positionParams.instrument(), equalTo(instrumentEURUSD));
        assertThat(positionParams.closeParamsProvider(), equalTo(closeParamsPriovderMock));
        assertFalse(positionParams.maybeMergeParams().isPresent());
        assertThat(positionParams.executionMode(), equalTo(CloseExecutionMode.CloseOpened));
        assertComposerIsNeutral(positionParams.closeFilledComposer());
        assertComposerIsNeutral(positionParams.closeAllComposer());
        assertComposerEmitsComposerEvent(positionParams.closeOpenedComposer());
    }
}