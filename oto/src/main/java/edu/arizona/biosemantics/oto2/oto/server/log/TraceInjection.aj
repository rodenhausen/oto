package edu.arizona.biosemantics.oto2.oto.server.log;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.common.log.ObjectStringifier;

@Aspect
public class TraceInjection {

	@Before("within(edu.arizona.biosemantics.oto2.oto.server..*) && "
			+ "!within(edu.arizona.biosemantics.oto2.oto.server.log..*) && "
			+ "!within(edu.arizona.biosemantics.oto2.oto.server.db.Query) && "
			+ "execution(public * *(..))")
	public void trace(JoinPoint joinPoint) {
		Signature sig = joinPoint.getSignature();
		log(LogLevel.TRACE, "Call: " + sig.getDeclaringTypeName() + " " + sig.getName() + " Arguments: " + 
				ObjectStringifier.stringify(joinPoint.getArgs()));
	}

}
