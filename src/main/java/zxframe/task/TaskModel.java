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
package zxframe.task;



import java.util.concurrent.TimeUnit;

/**
 * 任务的每个实例
 * @author zhoux
 * @preserve
 */
public class TaskModel implements Runnable{
	/**
	 * 运行实例
	 */
	private TaskRunnable taskRunnable;
	/**
	 * 1为只运行一次,2为循环运行,3
	 */
	private int type;
	/**
	 * 下次运行的时间
	 */
	private long runTime;
	/**
	 * 间隔多少TimeUnit执行一次
	 */
	private long delay;
	/**
	 * 计算时间的方式
	 */
	private TimeUnit timeUnit;
	/**
	 * 最后一次运行时间
	 */
	private long lastRunTime=0;
	public void run() {
		try {
			taskRunnable.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * 计算运行时间
	 */
	public void calcTime(long value)
	{
		if(timeUnit==TimeUnit.DAYS)//天
		{
			value=1000*60*60*24*value;
		}
		else if(timeUnit==TimeUnit.HOURS)//小时
		{
			value=1000*60*60*value;
		}
		else if(timeUnit==TimeUnit.MINUTES)//分钟
		{
			value=1000*60*value;
		}else if(timeUnit==TimeUnit.SECONDS)//秒
		{ 
			value=1000*value;
		}else//毫秒
		{
			
		}
		if(lastRunTime==0) {
			lastRunTime=System.currentTimeMillis();
		}else if(runTime<=System.currentTimeMillis()){
			lastRunTime=runTime;
		}
		runTime=lastRunTime+value;
	}
	/**
	 * @preserve
	 */
	public void cancel() {
		type=1;
		ThreadResource.getTaskPool().getTasks().remove(this);
	}
	
	
	public TaskRunnable getTaskRunnable() {
		return taskRunnable;
	}
	public void setTaskRunnable(TaskRunnable taskRunnable) {
		this.taskRunnable = taskRunnable;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public long getRunTime() {
		return runTime;
	}
	public void setRunTime(long runTime) {
		this.runTime = runTime;
	}
	public long getDelay() {
		return delay;
	}
	public void setDelay(long delay) {
		this.delay = delay;
	}
	public TimeUnit getTimeUnit() {
		return timeUnit;
	}
	public void setTimeUnit(TimeUnit timeUnit) {
		this.timeUnit = timeUnit;
	}
}