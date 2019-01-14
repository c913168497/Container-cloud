package org.application.common.thread;

import lombok.Data;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@Data
public class RunnableWithRequestAttributes extends Thread {

    HttpServletRequest request = null ;

    public RunnableWithRequestAttributes(Runnable target){
        super(target,"RunnableWithRequestAttributes");
        Optional.ofNullable(RequestContextHolder.getRequestAttributes()).ifPresent( attrs -> {
        	ServletRequestAttributes  requestAttrs = (ServletRequestAttributes) attrs;
        	request = requestAttrs.getRequest();
        });
    }
   
  /*  
    public synchronized void start() {
    	System.out.println(getName());
    	super.start();
    }*/
   
   
}
