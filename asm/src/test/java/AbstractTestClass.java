public abstract class AbstractTestClass implements ITestClass {

    private String field1;
    private static String field2 = "Field2";

    public AbstractTestClass() {
        this.field1 = "Field1";
    }

    @Override
    public void method1() {
        System.out.println("AbstractTestClass");
    }

    @Override
    public abstract void method2();


    @Override
    public String getField1() {
        return this.field1;
    }

    public void method3() {
        System.out.println("AbstractTestClass");
        imethod();
        method2();
    }
}
