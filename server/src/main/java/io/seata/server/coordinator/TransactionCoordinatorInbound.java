package io.seata.server.coordinator;

import io.seata.core.model.ResourceManagerOutbound;
import io.seata.core.model.TransactionManager;



public interface TransactionCoordinatorInbound extends ResourceManagerOutbound, TransactionManager {

}

