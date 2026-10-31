/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.objectweb.asm.ClassWriter
 *  org.objectweb.asm.FieldVisitor
 *  org.objectweb.asm.MethodVisitor
 */
package com.vicmatskiv.pointblank.event;

import com.vicmatskiv.pointblank.event.CustomEvent;
import com.vicmatskiv.pointblank.event.EventBus;
import com.vicmatskiv.pointblank.event.EventListener;
import com.vicmatskiv.pointblank.event.SubscribeEvent2;
import java.lang.reflect.Method;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;

public class EventRegistrationUtil {
    private final EventBus eventBus;

    public EventRegistrationUtil(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    public void register(Object instance) {
        Class<?> clazz = instance.getClass();
        for (Method method : clazz.getDeclaredMethods()) {
            if (!method.isAnnotationPresent(SubscribeEvent2.class)) continue;
            if (method.getParameterCount() != 1) {
                throw new IllegalArgumentException("Method " + method.getName() + " must have exactly one parameter.");
            }
            Class<?> eventType = method.getParameterTypes()[0];
            try {
                if (!CustomEvent.class.isAssignableFrom(eventType)) continue;
                EventListener listener = this.createListener(instance, method, eventType);
                this.eventBus.addListener(eventType, listener);
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private EventListener createListener(Object instance, Method method, Class<?> eventType) throws Exception {
        String className = instance.getClass().getName() + "$" + method.getName() + "EventListener";
        String interfaceName = EventListener.class.getName().replace('.', '/');
        String instanceFieldName = "instance";
        String eventTypeName = eventType.getName().replace('.', '/');
        String eventInterfaceName = CustomEvent.class.getName().replace('.', '/');
        ClassWriter cw = new ClassWriter(2);
        cw.visit(52, 1, className.replace('.', '/'), null, "java/lang/Object", new String[]{interfaceName});
        FieldVisitor fv = cw.visitField(18, instanceFieldName, "Ljava/lang/Object;", null, null);
        fv.visitEnd();
        MethodVisitor mv = cw.visitMethod(1, "<init>", "(Ljava/lang/Object;)V", null, null);
        mv.visitCode();
        mv.visitVarInsn(25, 0);
        mv.visitMethodInsn(183, "java/lang/Object", "<init>", "()V", false);
        mv.visitVarInsn(25, 0);
        mv.visitVarInsn(25, 1);
        mv.visitFieldInsn(181, className.replace('.', '/'), instanceFieldName, "Ljava/lang/Object;");
        mv.visitInsn(177);
        mv.visitMaxs(2, 2);
        mv.visitEnd();
        mv = cw.visitMethod(1, "handleEvent", "(L" + eventInterfaceName + ";)V", null, null);
        mv.visitCode();
        mv.visitVarInsn(25, 1);
        mv.visitTypeInsn(192, eventTypeName);
        mv.visitVarInsn(58, 2);
        mv.visitVarInsn(25, 0);
        mv.visitFieldInsn(180, className.replace('.', '/'), instanceFieldName, "Ljava/lang/Object;");
        mv.visitTypeInsn(192, instance.getClass().getName().replace('.', '/'));
        mv.visitVarInsn(25, 2);
        mv.visitMethodInsn(182, instance.getClass().getName().replace('.', '/'), method.getName(), "(L" + eventTypeName + ";)V", false);
        mv.visitInsn(177);
        mv.visitMaxs(2, 3);
        mv.visitEnd();
        cw.visitEnd();
        byte[] code = cw.toByteArray();
        Class<?> eventListenerClass = new ClassLoader(this, instance.getClass().getClassLoader()){

            public Class<?> defineClass(String name, byte[] b) {
                return this.defineClass(name, b, 0, b.length);
            }
        }.defineClass(className, code);
        return (EventListener)eventListenerClass.getConstructor(Object.class).newInstance(instance);
    }
}

