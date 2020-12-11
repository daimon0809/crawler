package com.github.hcsp;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class Main {

    private static final String USER_NAME = "daimon";
    private static final String PASSPORT = "daimon";

    private static String getNextLink(Connection connection, String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql); ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                return resultSet.getString(1);
            }
        }
        return null;
    }

    private static String getNextLinkAndDelete(Connection connection) throws SQLException {
        // 先从数据库拿出一个链接（拿出来并删掉），准备处理
        String link = getNextLink(connection, "select LINK from LINKS_TO_BE_PROCESSED limit 1");
        if (link != null) {
            updateDatabase(connection, link, "delete from LINKS_TO_BE_PROCESSED where LINK = ?");
        }
        return link;
    }

    @SuppressFBWarnings("DMI_CONSTANT_DB_PASSWORD")
    public static void main(String[] args) throws IOException, SQLException {
        Connection connection = DriverManager.getConnection("jdbc:h2:file://G:\\h2SQL\\daimon\\news", USER_NAME, PASSPORT);
        String link;
        while ((link = getNextLinkAndDelete(connection)) != null) {
            if (isLinkProcessed(connection, link)) {
                continue;
            }

            // 只关注news，sina的，排除登录页面
            if (isNeedLink(link)) {
                System.out.println(link);
                Document doc = httpGetAndParseHtml(link);

                parseUrlsFromPageAndStoreIntoDatabase(connection, doc);

                storeIntoDataBaseIfItIsNewsPage(connection, doc, link);

                updateDatabase(connection, link, "insert into LINKS_ALREADY_PROCESSED (LINK) values (?)");
            }
        }
    }

    private static void parseUrlsFromPageAndStoreIntoDatabase(Connection connection, Document doc) throws SQLException {
        for (Element aTag : doc.select("a")) {
            String href = aTag.attr("href");

            if (href.startsWith("//")) {
                href = "https:" + href;
            }

            if (!(href.toLowerCase().startsWith("javascript") || href.startsWith("#"))) {
                updateDatabase(connection, href, "insert into LINKS_TO_BE_PROCESSED (LINK) values (?)");
            }
        }
    }

    private static boolean isLinkProcessed(Connection connection, String link) throws SQLException {
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

    private static void updateDatabase(Connection connection, String link, String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, link);
            statement.executeUpdate();
        }
    }

    private static void storeIntoDataBaseIfItIsNewsPage(Connection connection, Document doc, String link) throws SQLException {
        List<Element> articleTags = doc.select("article");
        if (!articleTags.isEmpty()) {
            for (Element articleTag : articleTags) {
                String title = articleTag.child(0).text();
                // 入库
                List<Element> paragraphs = articleTag.select("p");
                String content = paragraphs.stream().map(Element::text).collect(Collectors.joining("\n"));
                try (PreparedStatement statement = connection.prepareStatement("insert into NEWS (URL, TITLE, CONTENT,CREATE_AT,MODIFIED_AT)values (?,?,?,now(),now())")) {
                    statement.setString(1, link);
                    statement.setString(2, title);
                    statement.setString(3, content);
                    statement.executeUpdate();
                }
            }
        }
    }

    private static Document httpGetAndParseHtml(String link) throws IOException {
        // 我们之处理新浪站内的链接
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(link);
        httpGet.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.198 Safari/537.36");

        try (CloseableHttpResponse response1 = httpclient.execute(httpGet)) {
            HttpEntity entity = response1.getEntity();
            String html = EntityUtils.toString(entity);
            return Jsoup.parse(html);
        }
    }

    private static boolean isNeedLink(String link) {
        return (isNewsPage(link) || isIndexPage(link))
                && isNotLoginPage(link);
    }

    private static boolean isIndexPage(String link) {
        return "https://sina.cn".equals(link);
    }

    private static boolean isNewsPage(String link) {
        return link.contains("news.sina.cn");
    }

    private static boolean isNotLoginPage(String link) {
        return !link.contains("passport.sina.cn");
    }
}
