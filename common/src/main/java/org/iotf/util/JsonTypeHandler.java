package org.iotf.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.postgresql.util.PGobject;

import java.sql.*;

public class JsonTypeHandler extends BaseTypeHandler<Object> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Object parameter, JdbcType jdbcType) throws SQLException {
        String jsonString;
        try {
            if(parameter instanceof String){
                jsonString = (String) parameter;
            }else {
                jsonString = objectMapper.writeValueAsString(parameter);
            }
        } catch (JsonProcessingException e) {
            throw new SQLException("JsonTypeHandler JSON serialization error", e);
        }
        // 获取数据库类型
        Connection connection = ps.getConnection();
        DatabaseMetaData metaData = connection.getMetaData();
        String databaseProductName = metaData.getDatabaseProductName().toLowerCase();

        if (databaseProductName.contains("postgresql")) {
            PGobject pgObject = new PGobject();
            pgObject.setType("json"); // 或者 "jsonb" 根据需求
            pgObject.setValue(jsonString);
            ps.setObject(i, pgObject);
        } else {
            //其他数据库处理逻辑
            ps.setString(i, jsonString);
        }
    }

    @Override
    public Object getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return parseJson(rs.getString(columnName));
    }

    @Override
    public Object getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return parseJson(rs.getString(columnIndex));
    }

    @Override
    public Object getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return parseJson(cs.getString(columnIndex));
    }

    private Object parseJson(String json) throws SQLException {
        if (json == null) {
            return null;
        }
        try {
            return objectMapper.readTree(json);
        } catch (Exception e) {
            throw new SQLException("JsonTypeHandler JSON deserialization error", e);
        }
    }
}