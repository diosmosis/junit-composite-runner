package flarestar.junit.composite.runner.javassist;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.*;
import org.junit.runners.ParentRunner;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Method;

/**
 * TODO
 */
public abstract class BaseClassExtender {

    protected void copyAnnotations(ClassPool pool, Class<?> runnerClass, CtClass newRunnerCtClass) {
        ClassFile classFile = newRunnerCtClass.getClassFile();
        classFile.addAttribute(makeAnnotationAttribute(pool, classFile.getConstPool(), runnerClass));
    }

    protected AnnotationsAttribute makeAnnotationAttribute(ClassPool classPool, ConstPool pool, Class<?> klass) {
        AnnotationsAttribute result = new AnnotationsAttribute(pool, AnnotationsAttribute.visibleTag);
        for (Annotation annotation : klass.getAnnotations()) {
            result.addAnnotation(makeBytecodeAnnotation(classPool, pool, annotation));
        }
        return result;
    }

    protected javassist.bytecode.annotation.Annotation makeBytecodeAnnotation(ClassPool classPool, ConstPool pool,
                                                                            Annotation annotation) {
        javassist.bytecode.annotation.Annotation byteCodeAnnotation =
            new javassist.bytecode.annotation.Annotation(annotation.annotationType().getName(), pool);

        for (Method method : annotation.annotationType().getDeclaredMethods()) {
            Object value;
            try {
                value = method.invoke(annotation);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e); // should never happen
            }

            byteCodeAnnotation.addMemberValue(method.getName(), makeMemberValue(classPool, pool, value));
        }

        return byteCodeAnnotation;
    }

    protected MemberValue makeMemberValue(ClassPool classPool, ConstPool pool, Object value) {
        if (value instanceof Annotation) {
            return new AnnotationMemberValue(makeBytecodeAnnotation(classPool, pool, (Annotation)value), pool);
        } else if (value.getClass().isArray()) {
            ArrayMemberValue result = new ArrayMemberValue(
                makeMemberValue(classPool, pool, value.getClass().getComponentType()), pool);

            MemberValue[] elements = new MemberValue[Array.getLength(value)];
            for (int i = 0; i != elements.length; ++i) {
                elements[i] = makeMemberValue(classPool, pool, Array.get(value, i));
            }

            result.setValue(elements);
            return result;
        } else if (value instanceof Boolean) {
            return new BooleanMemberValue((Boolean)value, pool);
        } else if (value instanceof Byte) {
            return new ByteMemberValue((Byte)value, pool);
        } else if (value instanceof Character) {
            return new CharMemberValue((Character) value, pool);
        } else if (value instanceof Double) {
            return new DoubleMemberValue((Double) value, pool);
        } else if (value instanceof Enum) {
            EnumMemberValue result = new EnumMemberValue(pool);
            result.setType(value.getClass().getName());
            result.setValue(((Enum)value).name());
            return result;
        } else if (value instanceof Float) {
            return new FloatMemberValue((Float)value, pool);
        } else if (value instanceof Integer) {
            return new IntegerMemberValue(pool, (Integer)value);
        } else if (value instanceof Long) {
            return new LongMemberValue((Long)value, pool);
        } else if (value instanceof Short) {
            return new ShortMemberValue((Short)value, pool);
        } else if (value instanceof String) {
            return new StringMemberValue((String) value, pool);
        } else if (value instanceof Class) {
            return new ClassMemberValue(((Class) value).getName(), pool);
        } else {
            throw new IllegalArgumentException("Don't know how to convert " + value.getClass().getName()
                + " into an annotation MemberValue.");
        }
    }
}
