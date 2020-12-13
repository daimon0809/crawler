package com.github.hcsp;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

public class MyBatisCrawlerDaoImpl implements CrawlerDao {
    private SqlSessionFactory sqlSessionFactory;

    public MyBatisCrawlerDaoImpl() {
        String resource = "db/mybatis/mybatis-config.xml";
        InputStream inputStream;
        try {
            inputStream = Resources.getResourceAsStream(resource);
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    @Override
    public synchronized String getNextLinkAndDelete() throws SQLException {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            String url = session.selectOne("com.github.hcsp.MyMapper.selectNextAvailable");
            if (url != null) {
                session.delete("com.github.hcsp.MyMapper.deleteLink", url);
            }
            return url;
        }
    }

    @Override
    public void insertNewsIntoDatabase(String link, String title, String content) throws SQLException {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            session.insert("com.github.hcsp.MyMapper.insertNews",
                    new News(title, content, link));
        }
    }

    @Override
    public boolean isLinkProcessed(String link) throws SQLException {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            int processedLinkCount = session.selectOne("com.github.hcsp.MyMapper.countLink", link);
            return processedLinkCount != 0;
        }
    }

    @Override
    public void insertProcessedLink(String link) {
        Map<String, Object> params = new HashMap<>();
        params.put("tableName", "LINKS_ALREADY_PROCESSED");
        params.put("link", link);
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            session.insert("com.github.hcsp.MyMapper.insertLink",
                    params);
        }
    }

    @Override
    public void insertToBeProcessedLink(String link) {
        Map<String, Object> params = new HashMap<>();
        params.put("tableName", "LINKS_TO_BE_PROCESSED");
        params.put("link", link);
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            session.insert("com.github.hcsp.MyMapper.insertLink",
                    params);
        }
    }
}
