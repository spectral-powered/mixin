public interface ITestClass {

    void method1();

    void method2();

    String getField1();

    default void imethod() {
        System.out.println("ITestClass");
    }
}
