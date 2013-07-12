package com.yolodata.tbana.testutils;

import com.yolodata.tbana.cascading.splunk.SplunkDataQuery;
import com.yolodata.tbana.hadoop.mapred.shuttl.ShuttlInputFormatConstants;
import com.yolodata.tbana.hadoop.mapred.splunk.SplunkConf;
import org.apache.hadoop.conf.Configuration;

import java.util.Properties;


public class TestConfigurations {

    public static final String query = "search * sourcetype=\"mock\" | head 5 | table _raw";
    public static final String earliest_time = "-12mon";
    public static final String latest_time = "now";
    private static final String index_list = "*";

    public static Configuration getSplunkLoginConfig() {
        Configuration conf = new Configuration();

        conf.set(SplunkConf.SPLUNK_USERNAME, "admin");
        conf.set(SplunkConf.SPLUNK_PASSWORD, "changeIt");
        conf.set(SplunkConf.SPLUNK_HOST, "localhost");
        conf.set(SplunkConf.SPLUNK_PORT, "9050");

        return conf;
    }

    public static Configuration getConfigurationWithSplunkConfigured() {
        Configuration conf = getSplunkLoginConfig();
        conf.set(SplunkConf.SPLUNK_EARLIEST_TIME, earliest_time);
        conf.set(SplunkConf.SPLUNK_LATEST_TIME, latest_time);
        conf.set(SplunkConf.SPLUNK_SEARCH_QUERY, query);

        return conf;
    }

    public static Configuration getConfigurationWithShuttlSearch() {
        Configuration conf = getSplunkLoginConfig();
        conf.set(ShuttlInputFormatConstants.EARLIEST_TIME, "0");
        conf.set(ShuttlInputFormatConstants.LATEST_TIME, "now");
        conf.set(ShuttlInputFormatConstants.INDEX_LIST, index_list);

        return conf;
    }

    public static Properties getSplunkLoginAsProperties() {
        Properties properties = new Properties();

        properties.put(SplunkConf.SPLUNK_USERNAME, "admin");
        properties.put(SplunkConf.SPLUNK_PASSWORD, "changeIt");
        properties.put(SplunkConf.SPLUNK_HOST, "localhost");
        properties.put(SplunkConf.SPLUNK_PORT, "9050");

        return properties;
    }

    public static SplunkDataQuery getSplunkSearch() {
        return new SplunkDataQuery(earliest_time, latest_time);
    }

}
