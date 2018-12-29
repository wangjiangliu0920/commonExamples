package com.icecold.lib;

public class myClass {
    public static void main(String[] args) {
        myClass myClass = new myClass();
        Language language = new Language();
        Language java = new Java();

        //重载
        myClass.sayHi(java);
        myClass.sayHi(language);

        //重写
        java.sayHi();
        language.sayHi();
    }
    private void sayHi(Java java){
        System.out.println("Hi Im Java");
    }
    private void sayHi(Language language){
        System.out.println("Hi Im Language");
    }
}
