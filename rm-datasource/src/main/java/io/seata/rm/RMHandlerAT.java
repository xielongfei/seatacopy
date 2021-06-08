package io.seata.rm;

import io.seata.core.model.BranchType;
import io.seata.core.model.ResourceManager;
import io.seata.core.protocol.transaction.UndoLogDeleteRequest;
import io.seata.rm.datasource.DataSourceManager;
import io.seata.rm.datasource.DataSourceProxy;
import io.seata.rm.datasource.undo.UndoLogManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;

/**
 * @author: xielongfei
 * @date: 2021/06/07
 * @description:
 */
public class RMHandlerAT extends AbstractRMHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(RMHandlerAT.class);

    private static final int LIMIT_ROWS = 3000;

    @Override
    public void handle(UndoLogDeleteRequest request) {
        DataSourceManager dataSourceManager = (DataSourceManager)getResourceManager();
        DataSourceProxy dataSourceProxy = dataSourceManager.get(request.getResourceId());
        if (dataSourceProxy == null) {
            LOGGER.warn("Failed to get dataSourceProxy for delete undolog on {}", request.getResourceId());
            return;
        }
        Date logCreatedSave = getLogCreated(request.getSaveDays());
        Connection conn = null;
        try {
            conn = dataSourceProxy.getPlainConnection();
            int deleteRows = 0;
            do {
                try {
                    deleteRows = UndoLogManagerFactory.getUndoLogManager(dataSourceProxy.getDbType())
                            .deleteUndoLogByLogCreated(logCreatedSave, LIMIT_ROWS, conn);
                    if (deleteRows > 0 && !conn.getAutoCommit()) {
                        conn.commit();
                    }
                } catch (SQLException exx) {
                    if (deleteRows > 0 && !conn.getAutoCommit()) {
                        conn.rollback();
                    }
                    throw exx;
                }
            } while (deleteRows == LIMIT_ROWS);
        } catch (Exception e) {
            LOGGER.error("Failed to delete expired undo_log, error:{}", e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException closeEx) {
                    LOGGER.warn("Failed to close JDBC resource while deleting undo_log ", closeEx);
                }
            }
        }
    }

    private Date getLogCreated(int saveDays) {
        if (saveDays <= 0) {
            saveDays = UndoLogDeleteRequest.DEFAULT_SAVE_DAYS;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -saveDays);
        return calendar.getTime();
    }

    /**
     * get AT resource managerDataSourceManager.java
     *
     * @return
     */
    @Override
    protected ResourceManager getResourceManager() {
        return DefaultResourceManager.get().getResourceManager(BranchType.AT);
    }

    @Override
    public BranchType getBranchType() {
        return BranchType.AT;
    }
}
