package edu.arizona.biosemantics.oto2.oto.server.log;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

import edu.arizona.biosemantics.oto2.oto.shared.log.LogLevel;

@Aspect
public class AccessLogging {

	@Before("within(edu.arizona.biosemantics.oto2.oto.server..*) && "
			+ "!within(edu.arizona.biosemantics.oto2.oto.server.log..*) && "
			+ "!within(edu.arizona.biosemantics.oto2.oto.server.db.Query) && "
			+ "execution(public * *(..))")
	public void trace(JoinPoint joinPoint) {
		Signature sig = joinPoint.getSignature();
		log(LogLevel.TRACE, "Call to " + sig.getDeclaringTypeName() + " " + sig.getName() + " with arguments: " + 
				createArgsString(joinPoint.getArgs()));
	}

	private String createArgsString(Object[] objects) {
		StringBuilder result = new StringBuilder();
		for(Object object : objects)
			try {
				result.append(object.toString() + "\n");
			} catch(Exception e) {
				//no big deal
			}
		String out = result.toString();
		if(out.length() == 0)
			return out;
		return out.substring(0, out.length() - 1);
	}

}