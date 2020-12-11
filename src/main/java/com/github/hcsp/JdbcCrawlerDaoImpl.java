package com.github.hcsp;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JdbcCrawlerDaoImpl implements CrawlerDao {
    private static final String USER_NAME = "daimon";
    private static final String PASSPORT = "daimon";

    private final Connection connection;

    @SuppressFBWarnings("DMI_CONSTANT_DB_PASSWORD")
    public JdbcCrawlerDaoImpl() {
        try {
            this.connection = DriverManager.getConnection("jdbc:h2:file://G:\\h2SQL\\daimon\\news", USER_NAME, PASSPORT);
        } catch (SQLException e) {
            throw new RuntimeException();
        }
    }

    @Override
    public boolean isLinkProcessed(String link) throws SQLException {
        ResultSet resultSet = null;
        try (PreparedStatement statement = connection.prepareStatement("select LINK from LINKS_ALREADY_PROCESSED where LINK = ?")) {
            statement.setString(1, link);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                return true;
            }
        } finally {
            if (resultSet != null) {
                resultSet.close();
            }
        }
        return false;
    }

    @Override
    public String getNextLink(String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql); ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                return resultSet.getString(1);
            }
        }
        return null;
    }

    @Override
    public String getNextLinkAndDelete() throws SQLException {
        // 先从数据库拿出一个链接（拿出来并删掉），准备处理
        String link = getNextLink("select LINK from LINKS_TO_BE_PROCESSED limit 1");
        if (link != null) {
            updateDatabase(link, "delete from LINKS_TO_BE_PROCESSED where LINK = ?");
        }
        return link;
    }

    @Override
    public void updateDatabase(String link, String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, link);
            statement.executeUpdate();
        }
    }

    @Override
    public void insertNewsIntoDatabase(String link, String title, String content) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("insert into NEWS (URL, TITLE, CONTENT,CREATE_AT,MODIFIED_AT)values (?,?,?,now(),now())")) {
            statement.setString(1, link);
            statement.setString(2, title);
            statement.setString(3, content);
            statement.executeUpdate();
        }
    }
}
