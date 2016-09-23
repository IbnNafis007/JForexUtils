package com.jforex.programming.order.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.mockito.Mock;

import com.jforex.programming.order.MergeCommand;
import com.jforex.programming.order.MergePositionCommand;
import com.jforex.programming.test.common.InstrumentUtilForTest;

public class MergePositionCommandTest extends InstrumentUtilForTest {

    @Mock
    private MergeCommand mergeCommandMock;

    @Test
    public void accessorsAreCorrect() {
        final MergePositionCommand command = new MergePositionCommand(instrumentEURUSD, mergeCommandMock);

        assertThat(command.instrument(), equalTo(instrumentEURUSD));
        assertThat(command.mergeCommand(), equalTo(mergeCommandMock));
    }
}