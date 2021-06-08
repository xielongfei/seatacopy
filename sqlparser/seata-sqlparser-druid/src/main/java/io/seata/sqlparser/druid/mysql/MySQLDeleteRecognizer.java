package io.seata.sqlparser.druid.mysql;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlDeleteStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlTableIndex;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlOutputVisitor;
import io.seata.sqlparser.ParametersHolder;
import io.seata.sqlparser.SQLDeleteRecognizer;
import io.seata.sqlparser.SQLType;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: xielongfei
 * @date: 2021/06/07
 * @description:
 */
public class MySQLDeleteRecognizer extends BaseMySQLRecognizer implements SQLDeleteRecognizer {

    private final MySqlDeleteStatement ast;

    /**
     * Instantiates a new My sql delete recognizer.
     *
     * @param originalSQL the original sql
     * @param ast         the ast
     */
    public MySQLDeleteRecognizer(String originalSQL, SQLStatement ast) {
        super(originalSQL);
        this.ast = (MySqlDeleteStatement)ast;
    }

    @Override
    public SQLType getSQLType() {
        return SQLType.DELETE;
    }

    @Override
    public String getTableAlias() {
        if (ast.getFrom() == null) {
            return ast.getTableSource().getAlias();
        }
        return ast.getFrom().getAlias();
    }

    @Override
    public String getTableName() {
        StringBuilder sb = new StringBuilder();
        MySqlOutputVisitor visitor = new MySqlOutputVisitor(sb) {

            @Override
            public boolean visit(SQLExprTableSource x) {
                printTableSourceExpr(x.getExpr());
                return false;
            }
        };
        if (ast.getFrom() == null) {
            visitor.visit((SQLExprTableSource) ast.getTableSource());
        } else {
            visitor.visit((SQLExprTableSource) ast.getFrom());
        }
        return sb.toString();
    }

    @Override
    public String getWhereCondition(final ParametersHolder parametersHolder,
                                    final ArrayList<List<Object>> paramAppenderList) {
        SQLExpr where = ast.getWhere();
        return super.getWhereCondition(where, parametersHolder, paramAppenderList);
    }

    @Override
    public String getWhereCondition() {
        SQLExpr where = ast.getWhere();
        return super.getWhereCondition(where);
    }

    @Override
    public String getLimit(ParametersHolder parametersHolder, ArrayList<List<Object>> paramAppenderList) {
        return super.getLimit(ast, getSQLType(), parametersHolder, paramAppenderList);
    }

    @Override
    public String getOrderBy() {
        return super.getOrderBy(ast, getSQLType());
    }

}
