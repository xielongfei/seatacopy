package io.seata.rm;

import io.seata.common.exception.FrameworkException;
import io.seata.common.loader.EnhancedServiceLoader;
import io.seata.common.util.CollectionUtils;
import io.seata.core.exception.TransactionException;
import io.seata.core.model.BranchStatus;
import io.seata.core.model.BranchType;
import io.seata.core.model.Resource;
import io.seata.core.model.ResourceManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: xielongfei
 * @date: 2021/06/07
 * @description:
 */
public class DefaultResourceManager implements ResourceManager {

    /**
     * all resource managers
     */
    protected static Map<BranchType, ResourceManager> resourceManagers
            = new ConcurrentHashMap<>();

    private DefaultResourceManager() {
        initResourceManagers();
    }

    /**
     * Get resource manager.
     *
     * @return the resource manager
     */
    public static DefaultResourceManager get() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * only for mock
     *
     * @param branchType
     * @param rm
     */
    public static void mockResourceManager(BranchType branchType, ResourceManager rm) {
        resourceManagers.put(branchType, rm);
    }

    protected void initResourceManagers() {
        //init all resource managers
        List<ResourceManager> allResourceManagers = EnhancedServiceLoader.loadAll(ResourceManager.class);
        if (CollectionUtils.isNotEmpty(allResourceManagers)) {
            for (ResourceManager rm : allResourceManagers) {
                resourceManagers.put(rm.getBranchType(), rm);
            }
        }
    }

    @Override
    public BranchStatus branchCommit(BranchType branchType, String xid, long branchId,
                                     String resourceId, String applicationData)
            throws TransactionException {
        return getResourceManager(branchType).branchCommit(branchType, xid, branchId, resourceId, applicationData);
    }

    @Override
    public BranchStatus branchRollback(BranchType branchType, String xid, long branchId,
                                       String resourceId, String applicationData)
            throws TransactionException {
        return getResourceManager(branchType).branchRollback(branchType, xid, branchId, resourceId, applicationData);
    }

    @Override
    public Long branchRegister(BranchType branchType, String resourceId,
                               String clientId, String xid, String applicationData, String lockKeys)
            throws TransactionException {
        return getResourceManager(branchType).branchRegister(branchType, resourceId, clientId, xid, applicationData,
                lockKeys);
    }

    @Override
    public void branchReport(BranchType branchType, String xid, long branchId, BranchStatus status,
                             String applicationData) throws TransactionException {
        getResourceManager(branchType).branchReport(branchType, xid, branchId, status, applicationData);
    }

    @Override
    public boolean lockQuery(BranchType branchType, String resourceId,
                             String xid, String lockKeys) throws TransactionException {
        return getResourceManager(branchType).lockQuery(branchType, resourceId, xid, lockKeys);
    }

    @Override
    public void registerResource(Resource resource) {
        getResourceManager(resource.getBranchType()).registerResource(resource);
    }

    @Override
    public void unregisterResource(Resource resource) {
        getResourceManager(resource.getBranchType()).unregisterResource(resource);
    }

    @Override
    public Map<String, Resource> getManagedResources() {
        Map<String, Resource> allResource = new HashMap<>();
        for (ResourceManager rm : resourceManagers.values()) {
            Map<String, Resource> tempResources = rm.getManagedResources();
            if (tempResources != null) {
                allResource.putAll(tempResources);
            }
        }
        return allResource;
    }

    /**
     * get ResourceManager by Resource Type
     *
     * @param branchType
     * @return
     */
    public ResourceManager getResourceManager(BranchType branchType) {
        ResourceManager rm = resourceManagers.get(branchType);
        if (rm == null) {
            throw new FrameworkException("No ResourceManager for BranchType:" + branchType.name());
        }
        return rm;
    }

    @Override
    public BranchType getBranchType() {
        throw new FrameworkException("DefaultResourceManager isn't a real ResourceManager");
    }

    private static class SingletonHolder {
        private static DefaultResourceManager INSTANCE = new DefaultResourceManager();
    }
}
