package io.netty.example.study.client.codec.dispatcher;

import io.netty.example.study.common.OperationResult;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Auther: daiyu
 * @Date: 16/2/20 15:39
 * @Description:
 */
public class RequestPendingCentor {

    private Map<Long, OperationResultFuture> map = new ConcurrentHashMap<>();

    public void add(Long streamId, OperationResultFuture future) {
        this.map.put(streamId, future);
    }


    public void set(Long streamId, OperationResult operationResult) {
        OperationResultFuture operationResultFuture =  this.map.get(streamId);
        if (operationResultFuture != null) {
            operationResultFuture.setSuccess(operationResult);
            this.map.remove(streamId);
        }
    }

    public OperationResultFuture take(Long streamId) {
        if (this.map.containsKey(streamId)) {
            OperationResultFuture operationResultFuture =  this.map.get(streamId);
            this.map.remove(streamId);
            return operationResultFuture;
        }
        return null;
    }
}
