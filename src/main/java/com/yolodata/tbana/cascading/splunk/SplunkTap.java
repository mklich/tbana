/*
 * Copyright (c) 2013 Yolodata, LLC,  All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.yolodata.tbana.cascading.splunk;

import cascading.flow.FlowProcess;
import cascading.tap.Tap;
import cascading.tap.hadoop.io.HadoopTupleEntrySchemeIterator;
import cascading.tuple.TupleEntryCollector;
import cascading.tuple.TupleEntryIterator;
import com.yolodata.tbana.hadoop.mapred.splunk.SplunkConf;
import org.apache.commons.lang.NotImplementedException;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.RecordReader;

import java.io.IOException;
import java.util.Properties;
import java.util.UUID;

public class SplunkTap extends Tap<JobConf, RecordReader, OutputCollector> {

    private final String id = UUID.randomUUID().toString();
    private final Properties splunkLogin;
    private String confKey;

    public SplunkTap(Properties splunkLogin, SplunkScheme inputScheme) {
        super(inputScheme);
        this.splunkLogin = splunkLogin;
    }

    @Override
    public String getIdentifier() {
        return id;
    }

    @Override
    public void sourceConfInit(FlowProcess<JobConf> process, JobConf conf) {

        setConfKey(conf, SplunkConf.SPLUNK_HOST);
        setConfKey(conf, SplunkConf.SPLUNK_USERNAME);
        setConfKey(conf, SplunkConf.SPLUNK_PASSWORD);
        setConfKey(conf, SplunkConf.SPLUNK_PORT);

        SplunkConf splunkConf = new SplunkConf(conf);
        splunkConf.validateLoginConfiguration();
        super.sourceConfInit(process, conf);
    }

    @Override
    public TupleEntryIterator openForRead(FlowProcess<JobConf> flowProcess, RecordReader recordReader) throws IOException {
        return new HadoopTupleEntrySchemeIterator( flowProcess, this, recordReader );
    }

    @Override
    public TupleEntryCollector openForWrite(FlowProcess<JobConf> flowProcess, OutputCollector outputCollector) throws IOException {
        throw new NotImplementedException("Write to Splunk not implemented");
    }

    @Override
    public boolean createResource(JobConf conf) throws IOException {
        // Assume Splunk resource exists.
        return true;
    }

    @Override
    public boolean deleteResource(JobConf conf) throws IOException {
        // Delete nothing.
        return true;
    }

    @Override
    public boolean resourceExists(JobConf conf) throws IOException {
        return true;
    }

    @Override
    public long getModifiedTime(JobConf conf) throws IOException {
        // TODO: Not sure what to return here, last modified event?
        return System.currentTimeMillis();
    }

    public void setConfKey(JobConf conf, String key) {
        String value = splunkLogin.getProperty(key, null);
        if(value != null)
            conf.set(key, value);
    }
}
