package com.jforex.programming.order.task;

import java.util.Collection;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.task.params.TaskParamsUtil;
import com.jforex.programming.order.task.params.position.MergePositionParams;

import io.reactivex.Observable;

public class CancelSLTPAndMergeTask {

    private final CancelSLTPTask cancelSLTPTask;
    private final BasicTaskObservable basicTask;
    private final TaskParamsUtil taskParamsUtil;

    public CancelSLTPAndMergeTask(final CancelSLTPTask cancelSLTPTask,
                                  final BasicTaskObservable basicTask,
                                  final TaskParamsUtil taskParamsUtil) {
        this.cancelSLTPTask = cancelSLTPTask;
        this.basicTask = basicTask;
        this.taskParamsUtil = taskParamsUtil;
    }

    public Observable<OrderEvent> observe(final Collection<IOrder> toMergeOrders,
                                          final MergePositionParams mergePositionParams) {
        final Observable<OrderEvent> cancelSLTP =
                taskParamsUtil.composeParams(cancelSLTPTask.observe(toMergeOrders, mergePositionParams),
                                             mergePositionParams.cancelSLTPComposeParams());

        final Observable<OrderEvent> merge =
                taskParamsUtil.composeParams(basicTask.mergeOrders(mergePositionParams.mergeOrderLabel(),
                                                                   toMergeOrders),
                                             mergePositionParams.mergeComposeParams());

        return cancelSLTP.concatWith(merge);
    }
}
