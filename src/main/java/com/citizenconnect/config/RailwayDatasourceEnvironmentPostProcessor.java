package com.citizenconnect.config;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.util.StringUtils;

/**
 * Railway (and similar) often expose {@code DATABASE_URL} or {@code MYSQL_URL} as
 * {@code mysql://user:pass@host:port/db} — Spring JDBC expects {@code jdbc:mysql://...}.
 * Also supports discrete {@code MYSQLHOST}, {@code MYSQLPORT}, {@code MYSQLDATABASE}, etc.
 * Skips when profile {@code local} is active (H2 from {@code application-local.properties}).
 */
@Order(Ordered.LOWEST_PRECEDENCE)
public class RailwayDatasourceEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final String SOURCE_NAME = "citizenconnectRailwayDatasource";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (isLocalProfile(environment)) {
            return;
        }
        MutablePropertySources sources = environment.getPropertySources();
        if (sources.contains(SOURCE_NAME)) {
            return;
        }

        String databaseUrl = firstNonBlank(
                environment.getProperty("DATABASE_URL"),
                System.getenv("DATABASE_URL"),
                environment.getProperty("MYSQL_URL"),
                System.getenv("MYSQL_URL"));

        Map<String, Object> map = new HashMap<>();

        if (StringUtils.hasText(databaseUrl)) {
            String trimmed = databaseUrl.trim();
            if (trimmed.startsWith("jdbc:mysql")) {
                map.put("spring.datasource.url", appendMysqlParamsIfMissing(trimmed, environment));
                putCredentialsFromEnv(environment, map);
            } else if (trimmed.startsWith("mysql://") || trimmed.startsWith("mysqls://")) {
                ParsedMysql parsed = parseMysqlConnectUrl(trimmed);
                if (parsed != null) {
                    map.put("spring.datasource.url", parsed.jdbcUrl());
                    map.put("spring.datasource.username", parsed.username());
                    map.put("spring.datasource.password", parsed.password());
                }
            }
        }

        if (map.isEmpty() && mysqlHostVariablesPresent(environment)) {
            buildFromMysqlVariables(environment, map);
        }

        if (!map.isEmpty()) {
            sources.addFirst(new MapPropertySource(SOURCE_NAME, map));
        }
    }

    private static boolean isLocalProfile(ConfigurableEnvironment env) {
        String prop = env.getProperty("spring.profiles.active", "");
        if (prop.toLowerCase().contains("local")) {
            return true;
        }
        String envProfiles = System.getenv("SPRING_PROFILES_ACTIVE");
        return envProfiles != null && envProfiles.toLowerCase().contains("local");
    }

    private static void putCredentialsFromEnv(ConfigurableEnvironment env, Map<String, Object> map) {
        String user = firstNonBlank(
                env.getProperty("DATABASE_USERNAME"),
                System.getenv("DATABASE_USERNAME"),
                env.getProperty("MYSQLUSER"),
                System.getenv("MYSQLUSER"));
        String pass = firstNonBlank(
                env.getProperty("DATABASE_PASSWORD"),
                System.getenv("DATABASE_PASSWORD"),
                env.getProperty("MYSQLPASSWORD"),
                System.getenv("MYSQLPASSWORD"));
        if (StringUtils.hasText(user)) {
            map.put("spring.datasource.username", user);
        }
        if (StringUtils.hasText(pass)) {
            map.put("spring.datasource.password", pass);
        }
    }

    private static boolean mysqlHostVariablesPresent(ConfigurableEnvironment env) {
        return StringUtils.hasText(firstNonBlank(
                env.getProperty("MYSQLHOST"),
                System.getenv("MYSQLHOST"),
                env.getProperty("MYSQL_HOST"),
                System.getenv("MYSQL_HOST")));
    }

    private static void buildFromMysqlVariables(ConfigurableEnvironment env, Map<String, Object> map) {
        String host = firstNonBlank(
                env.getProperty("MYSQLHOST"),
                System.getenv("MYSQLHOST"),
                env.getProperty("MYSQL_HOST"),
                System.getenv("MYSQL_HOST"));
        String port = firstNonBlank(
                env.getProperty("MYSQLPORT"),
                System.getenv("MYSQLPORT"),
                env.getProperty("MYSQL_PORT"),
                System.getenv("MYSQL_PORT"),
                "3306");
        String db = firstNonBlank(
                env.getProperty("MYSQLDATABASE"),
                System.getenv("MYSQLDATABASE"),
                env.getProperty("MYSQL_DATABASE"),
                System.getenv("MYSQL_DATABASE"),
                "railway");
        String user = firstNonBlank(
                env.getProperty("MYSQLUSER"),
                System.getenv("MYSQLUSER"),
                env.getProperty("DATABASE_USERNAME"),
                System.getenv("DATABASE_USERNAME"));
        String pass = firstNonBlank(
                env.getProperty("MYSQLPASSWORD"),
                System.getenv("MYSQLPASSWORD"),
                env.getProperty("DATABASE_PASSWORD"),
                System.getenv("DATABASE_PASSWORD"));
        String jdbc = "jdbc:mysql://" + host + ":" + port + "/" + db;
        jdbc = appendMysqlParamsIfMissing(jdbc, env);
        map.put("spring.datasource.url", jdbc);
        if (StringUtils.hasText(user)) {
            map.put("spring.datasource.username", user);
        }
        if (StringUtils.hasText(pass)) {
            map.put("spring.datasource.password", pass);
        }
    }

    private static ParsedMysql parseMysqlConnectUrl(String url) {
        boolean ssl = url.startsWith("mysqls://");
        String rest = url.replaceFirst("^mysqls?://", "");
        int at = rest.lastIndexOf('@');
        if (at < 0) {
            return null;
        }
        String userInfo = rest.substring(0, at);
        String hostPath = rest.substring(at + 1);
        String[] up = userInfo.split(":", 2);
        String username = urlDecode(up[0]);
        String password = up.length > 1 ? urlDecode(up[1]) : "";

        int slash = hostPath.indexOf('/');
        String hostPort = slash >= 0 ? hostPath.substring(0, slash) : hostPath;
        String dbAndQuery = slash >= 0 ? hostPath.substring(slash + 1) : "";

        String pathPart = StringUtils.hasText(dbAndQuery) ? dbAndQuery : "railway";
        String jdbc = "jdbc:mysql://" + hostPort + "/" + pathPart;
        if (ssl || looksLikeRailwayHost(hostPort)) {
            jdbc += (jdbc.contains("?") ? "&" : "?") + "sslMode=REQUIRED&allowPublicKeyRetrieval=true";
        } else {
            jdbc = appendMysqlParamsIfMissing(jdbc, null);
        }
        return new ParsedMysql(jdbc, username, password);
    }

    /**
     * Railway-managed MySQL usually requires TLS; {@code useSSL=false} causes connection failures.
     */
    private static String appendMysqlParamsIfMissing(String jdbcUrl, ConfigurableEnvironment env) {
        if (jdbcUrl.contains("useSSL=") || jdbcUrl.contains("sslMode=")) {
            return jdbcUrl;
        }
        boolean railway = looksLikeRailwayJdbcUrl(jdbcUrl)
                || (env != null && StringUtils.hasText(env.getProperty("RAILWAY_ENVIRONMENT")))
                || StringUtils.hasText(System.getenv("RAILWAY_ENVIRONMENT"));
        String params = railway
                ? "sslMode=REQUIRED&allowPublicKeyRetrieval=true"
                : "useSSL=false&allowPublicKeyRetrieval=true";
        return jdbcUrl + (jdbcUrl.contains("?") ? "&" : "?") + params;
    }

    private static boolean looksLikeRailwayHost(String hostPort) {
        if (hostPort == null) {
            return false;
        }
        String h = hostPort.toLowerCase();
        return h.contains("railway.app") || h.contains("railway.internal");
    }

    private static boolean looksLikeRailwayJdbcUrl(String jdbcUrl) {
        if (jdbcUrl == null) {
            return false;
        }
        String u = jdbcUrl.toLowerCase();
        return u.contains("railway.app") || u.contains("railway.internal");
    }

    private static String urlDecode(String s) {
        try {
            return URLDecoder.decode(s, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return s;
        }
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String v : values) {
            if (StringUtils.hasText(v)) {
                return v.trim();
            }
        }
        return null;
    }

    private record ParsedMysql(String jdbcUrl, String username, String password) {
    }
}
