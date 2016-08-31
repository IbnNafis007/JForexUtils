package com.jforex.programming.order.process.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.util.function.Consumer;

import org.junit.Test;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.process.CloseProcess;
import com.jforex.programming.test.common.CommonUtilForTest;

public class CloseProcessTest extends CommonUtilForTest {

    @Test
    public void assertActionsAreValidWhenNotDefined() {
        final CloseProcess closeBuilder = CloseProcess
            .forOrder(buyOrderEURUSD)
            .build();

        assertNotNull(closeBuilder.errorAction());
    }

    @Test
    public void assertValuesAreCorrect() {
        final Consumer<Throwable> errorAction = t -> {};
        final Consumer<IOrder> closeRejectAction = o -> {};
        final Consumer<IOrder> closeOKAction = o -> {};
        final Consumer<IOrder> partialCloseAction = o -> {};

        final CloseProcess closeBuilder = CloseProcess
            .forOrder(buyOrderEURUSD)
            .onError(errorAction)
            .onCloseReject(closeRejectAction)
            .onClose(closeOKAction)
            .onPartialClose(partialCloseAction)
            .build();

        assertThat(closeBuilder.orderToClose(), equalTo(buyOrderEURUSD));
        assertThat(closeBuilder.errorAction(), equalTo(errorAction));
    }
}
