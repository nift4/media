/*
 * Copyright (C) 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package androidx.media3.datasource;

import androidx.media3.test.utils.DataSourceContractTest;
import androidx.media3.test.utils.HttpDataSourceTestEnv;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.common.collect.ImmutableList;
import java.util.List;
import org.junit.Rule;
import org.junit.runner.RunWith;

/** {@link DataSource} contract tests for {@link DefaultHttpDataSource}. */
@RunWith(AndroidJUnit4.class)
public class DefaultHttpDataSourceContractTest extends DataSourceContractTest {

  @Rule public HttpDataSourceTestEnv httpDataSourceTestEnv = new HttpDataSourceTestEnv();

  @Override
  protected DataSource createDataSource() {
    return new DefaultHttpDataSource.Factory()
        // Ensure that 'resource not found' tests fail fast (b/403179253).
        .setConnectTimeoutMs(400)
        .setReadTimeoutMs(400)
        .createDataSource();
  }

  @Override
  protected ImmutableList<TestResource> getTestResources() {
    return httpDataSourceTestEnv.getServedResources();
  }

  @Override
  protected List<TestResource> getNotFoundResources() {
    return httpDataSourceTestEnv.getNotFoundResources();
  }
}
