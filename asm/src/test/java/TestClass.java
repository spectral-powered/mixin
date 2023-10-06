public class TestClass extends AbstractTestClass {

    private final String field1;
    private static String staticField;

    static {
        System.out.println("Static init");
        staticField = "My Static field";
        for(int i = 0; i < 10; i++) {
            staticField += " - " + i;
        }
    }

    public TestClass() {
        this.field1 = "Updated Field1";
    }

    public void imethod() {
        System.out.println("TestClass");
        super.imethod();
    }

    @Override
    public void method2() {
        System.out.println("TestClass");
        imethod();
        method3();
    }

    public static void method() {
        System.out.println("TestClass");
    }

    @Override
    public String getField1() {
        return this.field1;
    }

    public String getStaticField() {
        return staticField;
    }
}
