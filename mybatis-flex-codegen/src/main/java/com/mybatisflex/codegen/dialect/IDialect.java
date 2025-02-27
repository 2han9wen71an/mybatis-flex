/*
 *  Copyright (c) 2022-2023, Mybatis-Flex (fuhai999@gmail.com).
 *  <p>
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  <p>
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  <p>
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.mybatisflex.codegen.dialect;

import com.alibaba.druid.pool.DruidPooledConnection;
import com.mybatisflex.codegen.config.GlobalConfig;
import com.mybatisflex.codegen.entity.Table;
import com.mybatisflex.core.util.ClassUtil;
import com.mybatisflex.core.util.StringUtil;
import com.zaxxer.hikari.pool.HikariProxyConnection;
import oracle.jdbc.driver.OracleConnection;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 方言接口。
 */
public interface IDialect {

    /**
     * 默认方言。
     */
    IDialect DEFAULT = new JdbcDialect() {
        @Override
        String forBuildColumnsSql(String schema, String tableName) {
            return "SELECT * FROM " + (StringUtil.isNotBlank(schema) ? schema + "." : "") + tableName + " WHERE 1 = 2";
        }
    };

    /**
     * MySQL 方言。
     */
    IDialect MYSQL = new JdbcDialect() {
        @Override
        String forBuildColumnsSql(String schema, String tableName) {
            return "SELECT * FROM `" + (StringUtil.isNotBlank(schema) ? schema + "`.`" : "") + tableName + "` WHERE 1 = 2";
        }
    };

    /**
     * Oracle 方言。
     */
    IDialect ORACLE = new JdbcDialect() {
        @Override
        public String forBuildColumnsSql(String schema, String tableName) {
            return "SELECT * FROM \"" + (StringUtil.isNotBlank(schema) ? schema + "\".\"" : "") + tableName + "\" WHERE rownum < 1";
        }

        @Override
        public ResultSet getTablesResultSet(DatabaseMetaData dbMeta, Connection conn, String schema, String[] types) throws SQLException {
            return dbMeta.getTables(conn.getCatalog(), StringUtil.isNotBlank(schema) ? schema : dbMeta.getUserName(), null, types);
        }

        @Override
        protected ResultSet forRemarks(String schema, Table table, DatabaseMetaData dbMeta, Connection conn) throws SQLException {
            if (conn instanceof OracleConnection) {
                ((OracleConnection) conn).setRemarksReporting(true);
                return dbMeta.getColumns(conn.getCatalog(), StringUtil.isNotBlank(schema) ? schema : dbMeta.getUserName(), table.getName(), null);
            } else if ("com.zaxxer.hikari.pool.HikariProxyConnection".equals(conn.getClass().getName())) {
                return forRemarks(schema, table, dbMeta, getOriginalConn(HikariProxyConnection.class, "delegate", conn));
            } else if ("com.alibaba.druid.pool.DruidPooledConnection".equals(conn.getClass().getName())) {
                return forRemarks(schema, table, dbMeta, getOriginalConn(DruidPooledConnection.class, "conn", conn));
            }
            return null;
        }

        private Connection getOriginalConn(Class<?> clazz, String attr, Connection conn) {
            Field delegate = ClassUtil.getFirstField(clazz, field -> field.getName().equals(attr));
            try {
                delegate.setAccessible(true);
                return (Connection) delegate.get(conn);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    };

    /**
     * Sqlite 方言。
     */
    IDialect SQLITE = new SqliteDialect();

    /**
     * 构建表和列的信息。
     *
     * @param schemaName
     * @param table        存入的表对象
     * @param globalConfig 全局配置
     * @param dbMeta       数据库元数据
     * @param conn         连接
     * @throws SQLException 发生 SQL 异常时抛出
     */
    void buildTableColumns(String schemaName, Table table, GlobalConfig globalConfig, DatabaseMetaData dbMeta, Connection conn) throws SQLException;

    /**
     * 获取表的描述信息。
     *
     * @param dbMeta 数据库元数据
     * @param conn   连接
     * @param schema 模式
     * @param types  结果集类型
     * @return 结果集
     * @throws SQLException 发生 SQL 异常时抛出
     */
    ResultSet getTablesResultSet(DatabaseMetaData dbMeta, Connection conn, String schema, String[] types) throws SQLException;

}
