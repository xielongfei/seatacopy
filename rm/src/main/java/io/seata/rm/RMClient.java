package io.seata.rm;

import io.seata.core.rpc.netty.RmNettyRemotingClient;

/**
 * @author: xielongfei
 * @date: 2021/06/07
 * @description:
 */
public class RMClient {

    /**
     * Init.
     *
     * @param applicationId           the application id
     * @param transactionServiceGroup the transaction service group
     */
    public static void init(String applicationId, String transactionServiceGroup) {
        RmNettyRemotingClient rmNettyRemotingClient = RmNettyRemotingClient.getInstance(applicationId, transactionServiceGroup);
        rmNettyRemotingClient.setResourceManager(DefaultResourceManager.get());
        rmNettyRemotingClient.setTransactionMessageHandler(DefaultRMHandler.get());
        rmNettyRemotingClient.init();
    }
}
