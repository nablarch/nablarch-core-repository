package nablarch.core.repository.di.staticprop;

public class Foo {

    private static Bar bar;

    public static Bar getBar() {
        return bar;
    }

    public static void setBar(Bar bar) {
        Foo.bar = bar;
    }
}