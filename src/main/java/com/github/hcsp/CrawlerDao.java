package com.github.hcsp;

import java.sql.SQLException;

public interface CrawlerDao {
    String getNextLinkAndDelete() throws SQLException;

    void insertNewsIntoDatabase(String link, String title, String content) throws SQLException;

    boolean isLinkProcessed(String link) throws SQLException;

    void insertProcessedLink(String link) throws SQLException;

    void insertToBeProcessedLink(String href) throws SQLException;
}
