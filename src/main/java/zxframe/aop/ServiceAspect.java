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
package zxframe.aop;

import javax.annotation.Resource;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import zxframe.cache.transaction.CacheTransaction;
import zxframe.jpa.transaction.DataTransaction;
import zxframe.util.CServerUUID;

/**
 * 业务逻辑层切面，控制redis的事务，防止缓存错误
 */
@Aspect
@Component
public class ServiceAspect {
	public final static String THREADNAMESTARTS="zxframe";
	public static String[] requiredTx= {"add","put","save","init","create","insert","upd","del","rem","do","execute"};
	public static int rl=11;
	@Resource
	CacheTransaction ct;
	@Resource
	DataTransaction dt;
	
	@Pointcut("@within(org.springframework.stereotype.Service)")
	public void getAopPointcut() {
	}
	//声明该方法是一个前置通知：在目标方法开始之前执行
	@Before("getAopPointcut()")
	public void beforeMethod(JoinPoint joinPoint) {
		if(!Thread.currentThread().getName().startsWith(ServiceAspect.THREADNAMESTARTS)) {
			String transactionId= ServiceAspect.getJoinPointUUID(joinPoint)+"_"+CServerUUID.getSequenceId();
			Thread.currentThread().setName(transactionId);
//			if(ZxFrameConfig.showlog) {
//				logger.info("service aspect start:"+transactionId);
//			}
			ct.begin();
			if(transactionAopTread(joinPoint)) {
				dt.begin(joinPoint);
			}
		}
	}
	
	//返回通知：在目标方法正常结束执行后的通知  
    //返回通知是可以访问到目标方法的返回值的  
    @AfterReturning(value="getAopPointcut()"  
                    , returning = "result")  
    public void afterRunningMethod(JoinPoint joinPoint , Object result){ 
    	if(currentAopTreadName(joinPoint)) {
//    		if(ZxFrameConfig.showlog) {
//    			logger.info("service aspect commit:"+Thread.currentThread().getName());
//    		}
    		if(transactionAopTread(joinPoint)) {
    			dt.commit(joinPoint);
    		}
        	ct.commit();
    		clear(joinPoint);
    	}
    }
      
      
    //在目标方法出现异常时会执行的代码，  
    //可以访问到异常对象，且可以指定在出现特定异常时在执行通知代码  
    //如下面Exception ex，若是指定为NullpointerException ex就不会执行通知代码  
    @AfterThrowing(value="getAopPointcut()"  
                      , throwing="ex")  
    public void afterThrowingMethod(JoinPoint joinPoint,Throwable ex){
    	if(currentAopTreadName(joinPoint)) {
//			if(ZxFrameConfig.showlog) {
//				logger.info("service aspect rollback:"+Thread.currentThread().getName());
//			}
			if(transactionAopTread(joinPoint)) {
				dt.rollback(joinPoint);
			}
	    	ct.rollback();
			clear(joinPoint);
    	}
    } 
    private void clear(JoinPoint joinPoint) {
//  		if(ZxFrameConfig.showlog) {
//  			logger.info("service aspect clear:"+Thread.currentThread().getName());
//  		}
  		//清理无用数据
  		if(transactionAopTread(joinPoint)) {
  			dt.clear();
  		}
  		ct.clear();
  		//改变线程状态
  		Thread.currentThread().setName("clear:"+Thread.currentThread().getName());
    }
    /**
     * 获得切面唯一ID
     * @param joinPoint
     * @return
     */
    public static String getJoinPointUUID(JoinPoint joinPoint) {
		return ServiceAspect.THREADNAMESTARTS+"_"+joinPoint.getSignature().getDeclaringType().getName()+"_"+joinPoint.getSignature().getName();
    }
    /**
     * 是否是开启AOP的线程当前名
     * @param joinPoint
     * @return
     */
    private boolean currentAopTreadName(JoinPoint joinPoint) {
		if(Thread.currentThread().getName().startsWith(ServiceAspect.getJoinPointUUID(joinPoint))) {
			return true;
		}
		return false;
    }
    /**
	 * 是否符合传播式事务切面的方法名
	 * @param joinPoint
	 * @return
	 */
	private boolean transactionAopTread(JoinPoint joinPoint) {
		String name = joinPoint.getSignature().getName();
		for (int i = 0; i < rl; i++) {
			if(name.startsWith(requiredTx[i])) {
				return true;
			}
		}
		return false;
	}
}
