/*
 *    Copyright 2020 Chitral Verma
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.github.chitralverma.schnapps.examples;

import com.github.chitralverma.schnapps.Server;
import com.github.chitralverma.schnapps.config.ConfigParser;
import com.github.chitralverma.schnapps.config.Configuration;
import com.github.chitralverma.schnapps.config.models.ExternalConfigModel;
import com.github.chitralverma.schnapps.examples.utils.EmbeddedDB;
import com.github.chitralverma.schnapps.extras.ExternalManager;
import scala.Option;
import scala.runtime.AbstractFunction1;

public class JavaAppServer {
    public static void main(String[] args) {
        Configuration configuration = ConfigParser.parse(args);


        Option<ExternalConfigModel> externalConfigOpt =
                configuration.externalConfigs().find(
                        new AbstractFunction1<ExternalConfigModel, Object>() {
                            @Override
                            public Boolean apply(ExternalConfigModel ecm) {
                                return ecm.name().matches("hsqldb_source");
                            }
                        });

        if (externalConfigOpt.isDefined()) {
            EmbeddedDB.start(externalConfigOpt.get().configs());
            ExternalManager.loadExternals(configuration);
        }

        Server.bootUp(configuration);
        Server.await();
    }
}
