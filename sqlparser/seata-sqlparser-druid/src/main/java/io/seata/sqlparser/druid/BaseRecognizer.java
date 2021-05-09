package io.seata.sqlparser.druid;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLBetweenExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLExistsExpr;
import com.alibaba.druid.sql.ast.expr.SQLInListExpr;
import com.alibaba.druid.sql.visitor.SQLASTVisitor;
import io.seata.sqlparser.SQLRecognizer;
import io.seata.sqlparser.SQLType;

/**
 * @author: xielongfei
 * @date: 2021/05/08 17:17
 * @description:
 */
public abstract class BaseRecognizer implements SQLRecognizer {

    /**
     * The type V marker.
     */
    public static class VMarker {
        @Override
        public String toString() {
            return "?";
        }
    }

    /**
     * The Original sql.
     */
    protected String originalSQL;

    /**
     * Instantiates a new Base recognizer.
     *
     * @param originalSQL the original sql
     */
    public BaseRecognizer(String originalSQL) {
        this.originalSQL = originalSQL;

    }

    public void executeVisit(SQLExpr where, SQLASTVisitor visitor) {
        if (where instanceof SQLBinaryOpExpr) {
            visitor.visit((SQLBinaryOpExpr) where);
        } else if (where instanceof SQLInListExpr) {
            visitor.visit((SQLInListExpr) where);
        } else if (where instanceof SQLBetweenExpr) {
            visitor.visit((SQLBetweenExpr) where);
        } else if (where instanceof SQLExistsExpr) {
            visitor.visit((SQLExistsExpr) where);
        } else {
            throw new IllegalArgumentException("unexpected WHERE expr: " + where.getClass().getSimpleName());
        }
    }

    @Override
    public String getOriginalSQL() {
        return originalSQL;
    }
}
