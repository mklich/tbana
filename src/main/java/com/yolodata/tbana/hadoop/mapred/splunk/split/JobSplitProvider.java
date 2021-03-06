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

package com.yolodata.tbana.hadoop.mapred.splunk.split;

import com.splunk.Service;
import com.yolodata.tbana.hadoop.mapred.splunk.SplunkConf;
import com.yolodata.tbana.hadoop.mapred.splunk.SplunkJob;
import com.yolodata.tbana.hadoop.mapred.splunk.SplunkService;
import org.apache.hadoop.mapred.InputSplit;
import org.apache.hadoop.mapred.JobConf;

import java.io.IOException;

public class JobSplitProvider extends SplitProvider {

    @Override
    public InputSplit[] getSplits(JobConf conf, int numberOfSplits) throws IOException {

        SplunkConf splunkConf = new SplunkConf(conf);
        numberOfSplits = getNumberOfSplits(splunkConf,numberOfSplits);
        InputSplit[] splits = new InputSplit[numberOfSplits];

        Service service = SplunkService.connect(splunkConf);
        SplunkJob splunkJob = SplunkJob.createSplunkJob(service,splunkConf);

        long numberOfEvents = splunkJob.getNumberOfResultsFromJob(splunkConf);

        try {
            int resultsPerSplit = (int)numberOfEvents/numberOfSplits;

            boolean skipHeader;
            for(int i=0; i<numberOfSplits; i++) {
                int start = i * resultsPerSplit;
                int end = start + resultsPerSplit;

                // Skip header for all splits except first
                skipHeader = i>0;

                // Header is always present, so we always need to read an extra row.
                end++;

                if(i==numberOfSplits-1) {
                    long eventsLeft = numberOfEvents-(numberOfSplits*resultsPerSplit);
                    end += eventsLeft;
                }

                splits[i] = new SplunkSplit(splunkJob.getJob().getSid(), start, end, skipHeader);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return splits;
    }
}
