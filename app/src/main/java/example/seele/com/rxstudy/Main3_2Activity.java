package example.seele.com.rxstudy;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;


import example.seele.com.rxstudy.entity.SimpleResponseEntity;
import example.seele.com.rxstudy.net.RequestInterface;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

import static example.seele.com.rxstudy.BuildConfig.BaseUrl;
import static io.reactivex.disposables.CompositeDisposable.*;

/**
 * 功能操作符之 线程控制(切换/调度)的细说
 *
 * RxJava线程控制（调度 / 切换）的作用是什么？
    指定 被观察者 （Observable） / 观察者（Observer） 的工作线程类型。


     2.1 背景
         在 RxJava模型中，被观察者 （Observable） / 观察者（Observer）的工作线程 = 创建自身的线程
         即，若被观察者 （Observable） / 观察者（Observer）在主线程被创建，那么他们的工作（生产事件 / 接收& 响应事件）就会发生在主线程

         因为创建被观察者 （Observable） / 观察者（Observer）的线程 = 主线程
         所以生产事件 / 接收& 响应事件都发生在主线程

     2.2 冲突
         对于一般的需求场景，需要在子线程中实现耗时的操作；然后回到主线程实现 UI操作
         应用到 RxJava模型中，可理解为：
         被观察者 （Observable） 在 子线程 中生产事件（如实现耗时操作等等）
         观察者（Observer）在 主线程 接收 & 响应事件（即实现UI操作）

     2.3 解决方案
        所以，为了解决上述冲突，即实现 真正的异步操作，我们需要对RxJava进行 线程控制（也称为调度 / 切换）


     3. 实现方式
        采用 RxJava内置的线程调度器（ Scheduler ），即通过 功能性操作符subscribeOn（） & observeOn（）实现

     3.1 功能性操作符subscribeOn（） & observeOn（）简介
         作用
         线程控制，即指定 被观察者 （Observable） / 观察者（Observer） 的工作线程类型

       线程类型
         在 RxJava中，内置了多种用于调度的线程类型
         类型	含义	                                                应用场景
        Schedulers.immediate()	        当前线程 = 不指定线程	        默认
         AndroidSchedulers.mainThread()	Android主线程	            操作UI
         Schedulers.newThread()	        常规新线程	                耗时等操作
         Schedulers.io()	            io操作线程	                网络请求、读写文件等io密集型操作
        Schedulers.computation()	    CPU计算操作线程	            大量计算操作
        注：RxJava内部使用 线程池 来维护这些线程，所以线程的调度效率非常高。


 */
public class Main3_2Activity extends AppCompatActivity {

    private static final String TAG = "RX.Main3_2Activity";

    CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3_2);

        //原理讲解
//        howWork();

//        实例运用
        useIt();


    }

    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }

    private void useIt() {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BaseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();

        RequestInterface requestInterface = retrofit.create(RequestInterface.class);

        Observable<SimpleResponseEntity> observable = requestInterface.fenci("i am alan");

        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<SimpleResponseEntity>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        compositeDisposable.add(d);
                    }

                    @Override
                    public void onNext(SimpleResponseEntity simpleResponseEntity) {
                        Log.d(TAG,"请求成功：" + simpleResponseEntity.toString());
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG,"请求失败：" );
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG,"请求完成：" );
                    }
                });

//        5.2 应用程序崩溃问题
//        背景：在发送网络请求时 退出当前Activity
//        冲突：此时如果回到主线程更新 UI，App会崩溃
//        解决方案：当 Activity退出时，调用 Disposable.dispose()切断观察者和被观察者的连接，使得观察者无法收到事件 & 响应事件
//        当出现多个Disposable时，可采用RxJava内置容器CompositeDisposable进行统一管理
//
//// 添加Disposable到CompositeDisposable容器
//        CompositeDisposable.add()
//
//// 清空CompositeDisposable容器
//        CompositeDisposable.clear()
    }

    private void howWork() {

        Observable observable = Observable.just(1);
        Observer observer = new Observer<Integer>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(Integer o) {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        };

//        <-- 使用说明 -->
//         Observable.subscribeOn（Schedulers.Thread）：指定被观察者 发送事件的线程（传入RxJava内置的线程类型）
//         Observable.observeOn（Schedulers.Thread）：指定观察者 接收 & 响应事件的线程（传入RxJava内置的线程类型）

//<-- 实例使用 -->
// 步骤3：通过订阅（subscribe）连接观察者和被观察者
        observable.subscribeOn(Schedulers.newThread()) // 1. 指定被观察者 生产事件的线程
                .observeOn(AndroidSchedulers.mainThread())  // 2. 指定观察者 接收 & 响应事件的线程
                .subscribe(observer); // 3. 最后再通过订阅（subscribe）连接观察者和被观察者



//        特别注意
//
//        1. 若Observable.subscribeOn（）多次指定被观察者 生产事件的线程，则只有第一次指定有效，其余的指定线程无效

        // 步骤3：通过订阅（subscribe）连接观察者和被观察者
        observable.subscribeOn(Schedulers.newThread()) // 第一次指定被观察者线程 = 新线程
                .subscribeOn(AndroidSchedulers.mainThread()) // 第二次指定被观察者线程 = 主线程
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);

//        测试结果：被观察者的线程 = 第一次指定的线程 = 新的工作线程，第二次指定的线程（主线程）无效

//        2. 若Observable.observeOn（）多次指定观察者 接收 & 响应事件的线程，则每次指定均有效，即每指定一次，就会进行一次线程的切换
// 步骤3：通过订阅（subscribe）连接观察者和被观察者
        observable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread()) // 第一次指定观察者线程 = 主线程
                .doOnNext(new Consumer<Integer>() { // 生产事件
                    @Override
                    public void accept(Integer integer) throws Exception {
                        Log.d(TAG, "第一次观察者Observer的工作线程是： " + Thread.currentThread().getName());
                    }
                })
                .observeOn(Schedulers.newThread()) // 第二次指定观察者线程 = 新的工作线程
                .subscribe(observer); // 生产事件


        // 注：
        // 1. 整体方法调用顺序：观察者.onSubscribe（）> 被观察者.subscribe（）> 观察者.doOnNext（）>观察者.onNext（）>观察者.onComplete()
        // 2. 观察者.onSubscribe（）固定在主线程进行
    }
}
