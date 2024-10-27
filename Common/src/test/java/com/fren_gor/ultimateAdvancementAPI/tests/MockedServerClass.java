package com.fren_gor.ultimateAdvancementAPI.tests;

import com.fren_gor.ultimateAdvancementAPI.tests.Utils.AbstractMockedServer;
import org.bukkit.craftbukkit.mocked0_0_R1.VersionedServerMock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface MockedServerClass {
    Class<? extends AbstractMockedServer> serverClass() default VersionedServerMock.class;
}
