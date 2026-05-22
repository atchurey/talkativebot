package com.atchurey.tools.talkativebot.core.topic.interfaces;

import com.atchurey.tools.talkativebot.core.conversation.Conversation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Topic {

	String key() default "";
	String name() default "";
	String description() default "";
	boolean canReplay() default false;
	Class<?> conversation() default Conversation.class;
	int order() default 0;
	String next() default "";

}