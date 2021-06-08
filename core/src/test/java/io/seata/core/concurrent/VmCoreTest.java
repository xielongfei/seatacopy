package io.seata.core.concurrent;

import org.openjdk.jol.info.ClassLayout;
import org.openjdk.jol.vm.VM;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.HashMap;

/**
 * @author: xielongfei
 * @date: 2021/05/14 11:47
 * @description:
 */
public class VmCoreTest {

    public static void main(String[] args) throws Exception {
        System.out.println(VM.current().details());
        System.out.println(ClassLayout.parseClass(VO.class).toPrintable());
        System.out.println("=================");
        Unsafe unsafe = getUnsafeInstance();
        VO vo = new VO();
        vo.a=2;
        vo.b=3;
        vo.d=new HashMap<>();
        //VO v1 = new VO();
        //v1.a = 3;
        long aoffset = unsafe.objectFieldOffset(VO.class.getDeclaredField("a"));
        System.out.println("aoffset="+aoffset);
        // 获取a的值
        //int va = unsafe.getInt(vo, aoffset);
        //int va1 = unsafe.getInt(v1, aoffset);
        boolean b = unsafe.compareAndSwapInt(vo, aoffset, 2, 55000000);
        System.out.println(b);
        long aoffset1 = unsafe.objectFieldOffset(VO.class.getDeclaredField("a"));
        System.out.println("aoffset="+aoffset1);
//        System.out.println("va="+va);
//        System.out.println("va="+va1);
    }

    public static Unsafe getUnsafeInstance() throws Exception {
        // 通过反射获取rt.jar下的Unsafe类
        Field theUnsafeInstance = Unsafe.class.getDeclaredField("theUnsafe");
        theUnsafeInstance.setAccessible(true);
        // return (Unsafe) theUnsafeInstance.get(null);是等价的
        return (Unsafe) theUnsafeInstance.get(Unsafe.class);
    }

    public static class VO {
        public int a = 0;
        public long b = 0;
        public String c= "123";
        public Object d= null;
        public int e = 100;
        public static int f= 0;
        public static String g= "";
        public Object h= null;
        public boolean i;
    }
}
