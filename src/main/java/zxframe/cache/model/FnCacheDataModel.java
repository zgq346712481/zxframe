/**
 * ZxFrame Java Library
 * https://github.com/zhouxuanGithub/zxframe
 *
 * Copyright (c) 2019 zhouxuan
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/
package zxframe.cache.model;

import zxframe.config.ZxFrameConfig;
import zxframe.jpa.annotation.DataModelScanning;
import zxframe.jpa.model.DataModel;

@DataModelScanning
public class FnCacheDataModel {
	public static String zxframeFncacheDefault="zxframe-fncache-default";
	public DataModel initZxframeFdcacheDefault() {
		DataModel cm =new DataModel();
		cm.setGroup(zxframeFncacheDefault);
		cm.setLcCache(ZxFrameConfig.ropen==true?false:true);//如果远程缓存未开启，则默认开启本地缓存
		cm.setRcCache(true);
		return cm;
	}
}
