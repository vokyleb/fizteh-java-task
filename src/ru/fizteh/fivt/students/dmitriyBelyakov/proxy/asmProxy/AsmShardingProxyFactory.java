package ru.fizteh.fivt.students.dmitriyBelyakov.proxy.asmProxy;

import org.objectweb.asm.*;
import org.objectweb.asm.commons.GeneratorAdapter;
import ru.fizteh.fivt.proxy.ShardingProxyFactory;
import ru.fizteh.fivt.students.dmitriyBelyakov.proxy.ProxyUtils;

import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public class AsmShardingProxyFactory implements ShardingProxyFactory {
    @Override
    public Object createProxy(Object[] targets, Class[] interfaces) {
        ProxyUtils.throwExceptionIsArgumentsIsIncorrect(targets, interfaces);
        ClassWriter cw = newClassWriter();
        String[] interfacesName = new String[interfaces.length];
        for (int i = 0; i < interfaces.length; ++i) {
            interfacesName[i] = Type.getInternalName(interfaces[i]);
        }
        cw.visit(Opcodes.V1_7, Opcodes.ACC_PUBLIC, "Proxy", null, "java/lang/Object", interfacesName);
        cw.visitField(Opcodes.ACC_PRIVATE, "targets", Type.getDescriptor(List.class), null, null).visitEnd();
        generateMethod(cw, Opcodes.ACC_PUBLIC, "<init>", "()V",
                new Function1V<GeneratorAdapter>() {
                    @Override
                    public void apply(GeneratorAdapter ga) {
                        ga.loadThis();
                        ga.dup();
                        ga.invokeConstructor(
                                Type.getType("java/lang/Object"),
                                new org.objectweb.asm.commons.Method("<init>", "()V")
                        );
                        Type printStreamType = Type.getType(PrintStream.class);
                        ga.getStatic(Type.getType(System.class), "out", printStreamType);
                        ga.push("Hello, World!");
                        ga.invokeVirtual(printStreamType, new org.objectweb.asm.commons.Method("println", "(Ljava/lang/String;)V"));
                        ga.returnValue();
                    }
                });
        generateMethod(cw, Opcodes.ACC_PUBLIC, "loadTargets", "(" + Type.getDescriptor(List.class) + ")V",
                new Function1V<GeneratorAdapter>() {
                    @Override
                    public void apply(GeneratorAdapter ga) {
                        ga.loadThis();
                        ga.dup();
                        Type printStreamType = Type.getType(PrintStream.class);
                        ga.getStatic(Type.getType(System.class), "out", printStreamType);
                        ga.push("Hello, World!!!");
                        ga.invokeVirtual(printStreamType, new org.objectweb.asm.commons.Method("println", "(Ljava/lang/String;)V"));
                        ga.loadArg(0);
                        ga.putField(Type.getType("Proxy"), "targets", Type.getType(List.class));
                        ga.returnValue();
                    }
                });
        /*for (Class interfc : interfaces) {
            Method[] methods = interfc.getDeclaredMethods();
            for (Method method : methods) {
                method.setAccessible(true);
                String descriptor = Type.getMethodDescriptor(method);
                generateMethod(cw, Opcodes.ACC_PUBLIC, method.getName(), "()V", // TODO
                        new Function1V<GeneratorAdapter>() {
                            @Override
                            public void apply(GeneratorAdapter ga) {
                                Type printStreamType = Type.getType(PrintStream.class);
                                ga.getStatic(Type.getType(System.class), "out", printStreamType);
                                ga.push("Hello, World!");
                                ga.invokeVirtual(printStreamType, new org.objectweb.asm.commons.Method("println", "(Ljava/lang/String;)V"));
                                ga.returnValue();
                            }
                        });
            }
        }*/
        cw.visitEnd();
        try {
            Class clazz = loadClass(cw.toByteArray());
            Object obj = clazz.newInstance();
            System.out.println(obj);
            //System.out.println("||| " + clazz.getDeclaredMethod("loadTargets", Object[].class));
            //clazz.getDeclaredMethod("loadTargets", Object[].class).invoke(clazz.cast(obj), targets);
            Method method = clazz.getDeclaredMethod("loadTargets", List.class);
            System.out.println(method);
            method.invoke(obj, Arrays.asList(targets));
            return obj;
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return null;
    }

    private ClassWriter newClassWriter() {
        int flags = ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS;
        return new ClassWriter(flags);
    }

    private Class loadClass(byte[] bytes) {
        class LocalClassLoader extends ClassLoader {
            public Class defineClass(byte[] bytes) {
                return super.defineClass(null, bytes, 0, bytes.length);
            }
        }
        return new LocalClassLoader().defineClass(bytes);
    }

    private static void generateMethod(ClassWriter cw, int access, String name, String descriptor,
                                       Function1V<GeneratorAdapter> f) {
        MethodVisitor mv = cw.visitMethod(access, name, descriptor, null, null);
        GeneratorAdapter ga = new GeneratorAdapter(mv, access, name, descriptor);
        ga.visitCode();
        f.apply(ga);
        ga.endMethod();
    }

    private interface Function1V<T> {
        void apply(T value);
    }
}