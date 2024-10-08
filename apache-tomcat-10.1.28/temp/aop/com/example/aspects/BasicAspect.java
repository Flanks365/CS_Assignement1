package com.example.aspects;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
 
@Aspect
public class BasicAspect {
 
	/* Read as -- do this *before* any *call* to the function
         * *java.io.PrintStream.println* that takes a *String*
         * and returns *void* and the function is not called
	 * within any class under the package net.andrewewhite.aspects
	 */
    @Before("   call(void java.io.PrintStream.println(String)) " +
            "&& !within(com.example.aspects..*)")
    public void beforePrintlnCall() {
        System.out.println("About to make call aspects");
    }
 
    @After("    call(void java.io.PrintStream.println(String)) " +
           "&&  !within(com.example.aspects..*)")
    public void afterPrintlnCall() {
        System.out.println("Aspects Complete!");
    }
}