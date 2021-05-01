package io.seata.core.exception;

import io.seata.config.Configuration;
import io.seata.config.ConfigurationFactory;
import io.seata.core.protocol.ResultCode;
import io.seata.core.protocol.transaction.AbstractTransactionRequest;
import io.seata.core.protocol.transaction.AbstractTransactionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author: xielongfei
 * @date: 2021/04/30 16:41
 * @description:
 */
public abstract class AbstractExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractExceptionHandler.class);

    /**
     * The constant CONFIG.
     */
    protected static final Configuration CONFIG = ConfigurationFactory.getInstance();

    public interface Callback<T extends AbstractTransactionRequest, S extends AbstractTransactionResponse> {
        /**
         * Execute.
         *
         * @param request  the request
         * @param response the response
         * @throws TransactionException the transaction exception
         */
        void execute(T request, S response) throws TransactionException;

        /**
         * on Success
         *
         * @param request
         * @param response
         */
        void onSuccess(T request, S response);

        /**
         * onTransactionException
         *
         * @param request
         * @param response
         * @param exception
         */
        void onTransactionException(T request, S response, TransactionException exception);

        /**
         * on other exception
         *
         * @param request
         * @param response
         * @param exception
         */
        void onException(T request, S response, Exception exception);
    }

    public abstract static class AbstractCallback<T extends AbstractTransactionRequest, S extends AbstractTransactionResponse>
            implements Callback<T, S> {

        @Override
        public void onSuccess(T request, S response) {
            response.setResultCode(ResultCode.Success);
        }

        @Override
        public void onTransactionException(T request, S response,
                                           TransactionException tex) {
            response.setTransactionExceptionCode(tex.getCode());
            response.setResultCode(ResultCode.Failed);
            response.setMsg("TransactionException[" + tex.getMessage() + "]");
        }

        @Override
        public void onException(T request, S response, Exception rex) {
            response.setResultCode(ResultCode.Failed);
            response.setMsg("RuntimeException[" + rex.getMessage() + "]");
        }
    }

    /**
     * Exception handle template.
     *
     * @param callback the callback
     * @param request  the request
     * @param response the response
     */
    public <T extends AbstractTransactionRequest, S extends AbstractTransactionResponse> void exceptionHandleTemplate(Callback<T, S> callback, T request, S response) {
        try {
            callback.execute(request, response);
            callback.onSuccess(request, response);
        } catch (TransactionException tex) {
            LOGGER.error("Catch TransactionException while do RPC, request: {}", request, tex);
            callback.onTransactionException(request, response, tex);
        } catch (RuntimeException rex) {
            LOGGER.error("Catch RuntimeException while do RPC, request: {}", request, rex);
            callback.onException(request, response, rex);
        }
    }
}
