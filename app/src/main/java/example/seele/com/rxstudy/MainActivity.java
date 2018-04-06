package example.seele.com.rxstudy;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import example.seele.com.rxstudy.entity.SimpleResponseEntity;
import example.seele.com.rxstudy.entity.SimpleResponseEntity2;
import example.seele.com.rxstudy.entity.SoResponseEntity;
import example.seele.com.rxstudy.net.RequestInterface;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

import static example.seele.com.rxstudy.BuildConfig.BaseUrl;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private String TAG = "Rx.MainActivity";
    Integer i = 10;

    // 定义Observable接口类型的网络请求对象
    Observable<SimpleResponseEntity> observable1;
    Observable<SimpleResponseEntity2> observable2;

    @Override
    public void onClick(View v) {
        startActivity(new Intent(MainActivity.this,Main2Activity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);






//        rxBasicUse();



//        可采用 Disposable.dispose() 切断观察者 与 被观察者 之间的连接
//        rxDisposeUse();

        //里边包含创建操作符
//        rxChainUse();

        //创建操作符实现网络请求轮询
//        pollNetRequest();

        //变化操作符 Map flatMap ConcatMap Buffer
//        transformOperators();

        //结合 Retrofit 与 RxJava的基本用法，即未用操作符前
//        normalnetNestCallBack();
        //变换操作符实现网络请求嵌套回调
//        netNestCallBack();

        //组合/合并 操作符
        combinationsOperators();

        //下面是组合/合并操作符的一些应用
        // 1.合并数据源 & 同时展示
//        combinatTogetherShow();

//        从磁盘 / 内存缓存中 获取缓存数据
//        combinatChoiceShow();


    }

    private void combinatChoiceShow() {
// 该2变量用于模拟内存缓存 & 磁盘缓存中的数据
        final String memoryCache = null;
        final String diskCache = "从磁盘缓存中获取数据";

        /*
         * 设置第1个Observable：检查内存缓存是否有该数据的缓存
         **/
        Observable<String> memory = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) throws Exception {

                // 先判断内存缓存有无数据
                if (memoryCache != null) {
                    // 若有该数据，则发送
                    emitter.onNext(memoryCache);
                } else {
                    // 若无该数据，则直接发送结束事件
                    emitter.onComplete();
                }

            }
        });

        /*
         * 设置第2个Observable：检查磁盘缓存是否有该数据的缓存
         **/
        Observable<String> disk = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) throws Exception {

                // 先判断磁盘缓存有无数据
                if (diskCache != null) {
                    // 若有该数据，则发送
                    emitter.onNext(diskCache);
                } else {
                    // 若无该数据，则直接发送结束事件
                    emitter.onComplete();
                }

            }
        });

        /*
         * 设置第3个Observable：通过网络获取数据
         **/
        Observable<String> network = Observable.just("从网络中获取数据");
        // 此处仅作网络请求的模拟


        /*
         * 通过concat（） 和 firstElement（）操作符实现缓存功能
         **/

        // 1. 通过concat（）合并memory、disk、network 3个被观察者的事件（即检查内存缓存、磁盘缓存 & 发送网络请求）
        //    并将它们按顺序串联成队列
        Observable.concat(memory, disk, network)
                // 2. 通过firstElement()，从串联队列中取出并发送第1个有效事件（Next事件），即依次判断检查memory、disk、network
                .firstElement()
                // 即本例的逻辑为：
                // a. firstElement()取出第1个事件 = memory，即先判断内存缓存中有无数据缓存；由于memoryCache = null，即内存缓存中无数据，所以发送结束事件（视为无效事件）
                // b. firstElement()继续取出第2个事件 = disk，即判断磁盘缓存中有无数据缓存：由于diskCache ≠ null，即磁盘缓存中有数据，所以发送Next事件（有效事件）
                // c. 即firstElement()已发出第1个有效事件（disk事件），所以停止判断。

                // 3. 观察者订阅
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        Log.d(TAG, "最终获取的数据来源 =  " + s);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Log.d(TAG, "throwable:" + throwable.getMessage());
                    }
                }, new Action() {
                    @Override
                    public void run() throws Exception {
                        Log.d(TAG,"使用完成");
                    }
                });


    }

    /**
     * 即，同时向2个数据源获取数据 -> 合并数据 -> 统一展示到客户端
     * 比如说从内存中的读取的图片数据 和 磁盘读取的数据合并后再展示
     */
    private void combinatTogetherShow() {
        /**merge 的使用*/
//        merge();

       /** 功能说明
        在该例中，我将结合结合 Retrofit 与RxJava，实现：

        从不同数据源（2个服务器）获取数据，即 合并网络请求的发送
        统一显示结果*/

       zip();

    }

    private void zip() {


        // 步骤1：创建Retrofit对象
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BaseUrl) // 设置 网络请求 Url
                .addConverterFactory(GsonConverterFactory.create()) //设置使用Gson解析(记得加入依赖)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create()) // 支持RxJava
                .build();

        // 步骤2：创建 网络请求接口 的实例
        final RequestInterface request = retrofit.create(RequestInterface.class);

        // 步骤3：采用Observable<...>形式 对 2个网络请求 进行封装
        Observable<SimpleResponseEntity> observable1 = request.fenci("i am alan").subscribeOn(Schedulers.io()); // 新开线程进行网络请求1
        Observable<SoResponseEntity> observable2 = request.so("http://120.76.205.241:8000/news/qihoo",
            "F1F8Ro6uKs3zc6IBq9bEr6c8CrTDprgybERUIXLkBuAYEaN0yDeeo9yB3btbSEI0",
            "比特币","baidu.com")
                .subscribeOn(Schedulers.io());


        // 即2个网络请求异步 & 同时发送

//        // 步骤4：通过使用Zip（）对两个网络请求进行合并再发送
        Observable.zip(observable1, observable2,
                new BiFunction<SimpleResponseEntity, SoResponseEntity, String>() {
                    // 注：创建BiFunction对象传入的第3个参数 = 合并后数据的数据类型
                    @Override
                    public String apply(SimpleResponseEntity simpleResponseEntity,
                                        SoResponseEntity soResponseEntity) throws Exception {
                        return simpleResponseEntity.toString() + "\n\n" + soResponseEntity.toString();
                    }
                }).observeOn(AndroidSchedulers.mainThread()) // 在主线程接收 & 处理数据
                .subscribe(new Consumer<String>() {
                    // 成功返回数据时调用
                    @Override
                    public void accept(String combine_infro) throws Exception {
                        // 结合显示2个网络请求的数据结果
                        Log.d(TAG, "最终接收到的数据是：" + combine_infro);
                    }
                }, new Consumer<Throwable>() {
                    // 网络请求错误时调用
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Log.d(TAG, "请求失败");
                        throwable.printStackTrace();
                    }
                });

    }

    private void merge() {

        Observable<String> network = Observable.just("网络");
        Observable<String> local = Observable.just("本地");

        Observable.merge(network,local).subscribe(new Observer<String>() {

            String result = "";
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(String s) {
                Log.d(TAG, "数据源有： "+ s  );
                result += s + "+";
            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG, "对Error事件作出响应");
            }

            @Override
            public void onComplete() {
                Log.d(TAG, "获取数据完成");
                Log.d(TAG,  result  );
            }
        });
    }

    /**
     * 组合/合并 操作符
     * 该类型的操作符的作用 = 组合多个被观察者
     */
    private void combinationsOperators() {


//        3.1组合多个被观察者
//                该类型的操作符的作用 = 组合多个被观察者
//
//        concat（） / concatArray（）
//        作用
//        组合多个被观察者一起发送数据，合并后 按发送顺序串行执行
//        二者区别：组合被观察者的数量，即concat（）组合被观察者数量≤4个，而concatArray（）则可＞4个

    // concat（）：组合多个被观察者（≤4个）一起发送数据
        // 注：串行执行
//        Observable.concat(Observable.just(1, 2, 3),
//                Observable.just(4, 5, 6),
//                Observable.just(7, 8, 9),
//                Observable.just(10, 11, 12))
//                .subscribe(new Observer<Integer>() {
//                    @Override
//                    public void onSubscribe(Disposable d) {
//
//                    }
//
//                    @Override
//                    public void onNext(Integer value) {
//                        Log.d(TAG, "接收到了事件"+ value  );
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        Log.d(TAG, "对Error事件作出响应");
//                    }
//
//                    @Override
//                    public void onComplete() {
//                        Log.d(TAG, "对Complete事件作出响应");
//                    }
//                });

        // concatArray（）：组合多个被观察者一起发送数据（可＞4个）
        // 注：串行执行
//        Observable.concatArray(Observable.just(1, 2, 3),
//                Observable.just(4, 5, 6),
//                Observable.just(7, 8, 9),
//                Observable.just(10, 11, 12),
//                Observable.just(13, 14, 15))
//                .subscribe(new Observer<Integer>() {
//                    @Override
//                    public void onSubscribe(Disposable d) {
//
//                    }
//
//                    @Override
//                    public void onNext(Integer value) {
//                        Log.d(TAG, "接收到了事件"+ value  );
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        Log.d(TAG, "对Error事件作出响应");
//                    }
//
//                    @Override
//                    public void onComplete() {
//                        Log.d(TAG, "对Complete事件作出响应");
//                    }
//                });



//        merge（） / mergeArray（）
//        作用
//        组合多个被观察者一起发送数据，合并后 按时间线并行执行
//        二者区别：组合被观察者的数量，即merge（）组合被观察者数量≤4个，而mergeArray（）则可＞4个
//        区别上述concat（）操作符：同样是组合多个被观察者一起发送数据，但concat（）操作符合并后是按发送顺序串行执行

        // merge（）：组合多个被观察者（＜4个）一起发送数据
        // 注：合并后按照时间线并行执行
//        Observable.merge(
//                Observable.intervalRange(0, 3, 1, 1, TimeUnit.SECONDS), // 从0开始发送、共发送3个数据、第1次事件延迟发送时间 = 1s、间隔时间 = 1s
//                Observable.intervalRange(2, 3, 1, 1, TimeUnit.SECONDS)) // 从2开始发送、共发送3个数据、第1次事件延迟发送时间 = 1s、间隔时间 = 1s
//                .subscribe(new Observer<Long>() {
//                    @Override
//                    public void onSubscribe(Disposable d) {
//
//                    }
//
//                    @Override
//                    public void onNext(Long value) {
//                        Log.d(TAG, "接收到了事件"+ value  );
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        Log.d(TAG, "对Error事件作出响应");
//                    }
//
//                    @Override
//                    public void onComplete() {
//                        Log.d(TAG, "对Complete事件作出响应");
//                    }
//                });

// mergeArray（） = 组合4个以上的被观察者一起发送数据，此处不作过多演示，类似concatArray（）




//        concatDelayError（） / mergeDelayError（）

        //使用concat（）和merge操作符时，若其中1个观察者发出onError事件，则会马上终止其他被观察者继续发送事件
        //怎么解决？
        //若希望onError事件推迟到其他被观察者发送事件结束后才出发
        //即需要使用对应的concatDelayError() 或mergeDelayError（） 操作符


//        无使用concatDelayError（）的情况
//        Observable.concat(
//                Observable.create(new ObservableOnSubscribe<Integer>() {
//                    @Override
//                    public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
//
//                        emitter.onNext(1);
//                        emitter.onNext(2);
//                        emitter.onNext(3);
//                        emitter.onError(new NullPointerException()); // 发送Error事件，因为无使用concatDelayError，所以第2个Observable将不会发送事件
//                        emitter.onComplete();
//                    }
//                }),
//                Observable.just(4, 5, 6))
//                .subscribe(new Observer<Integer>() {
//                    @Override
//                    public void onSubscribe(Disposable d) {
//
//                    }
//                    @Override
//                    public void onNext(Integer value) {
//                        Log.d(TAG, "接收到了事件"+ value  );
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        Log.d(TAG, "对Error事件作出响应");
//                    }
//
//                    @Override
//                    public void onComplete() {
//                        Log.d(TAG, "对Complete事件作出响应");
//                    }
//                });

//        <-- 使用了concatDelayError（）的情况 -->
//                Observable.concatArrayDelayError(
//                        Observable.create(new ObservableOnSubscribe<Integer>() {
//                            @Override
//                            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
//
//                                emitter.onNext(1);
//                                emitter.onNext(2);
//                                emitter.onNext(3);
//                                emitter.onError(new NullPointerException()); // 发送Error事件，因为使用了concatDelayError，所以第2个Observable将会发送事件，等发送完毕后，再发送错误事件
//                                emitter.onComplete();
//                            }
//                        }),
//                        Observable.just(4, 5, 6))
//                        .subscribe(new Observer<Integer>() {
//                            @Override
//                            public void onSubscribe(Disposable d) {
//
//                            }
//                            @Override
//                            public void onNext(Integer value) {
//                                Log.d(TAG, "接收到了事件"+ value  );
//                            }
//
//                            @Override
//                            public void onError(Throwable e) {
//                                Log.d(TAG, "对Error事件作出响应");
//                            }
//
//                            @Override
//                            public void onComplete() {
//                                Log.d(TAG, "对Complete事件作出响应");
//                            }
//                        });


//        mergeDelayError（）操作符同理，此处不作过多演示


//        3.2 合并多个事件
//        该类型的操作符主要是对多个被观察者中的事件进行合并处理。


//        Zip（）
//        作用
//        合并 多个被观察者（Observable）发送的事件，生成一个新的事件序列（即组合过后的事件序列），并最终发送


//        事件组合方式 = 严格按照原先事件序列 进行对位合并
//                最终合并的事件数量 = 多个被观察者（Observable）中数量最少的数量

//        <-- 创建第1个被观察者 -->
            Observable<Integer> observable1 = Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                Log.d(TAG, "被观察者1发送了事件1");
                emitter.onNext(1);
                // 为了方便展示效果，所以在发送事件后加入2s的延迟
                Thread.sleep(1000);

                Log.d(TAG, "被观察者1发送了事件2");
                emitter.onNext(2);
                Thread.sleep(1000);

                Log.d(TAG, "被观察者1发送了事件3");
                emitter.onNext(3);
                Thread.sleep(1000);

                emitter.onComplete();
            }
        }).subscribeOn(Schedulers.io()); // 设置被观察者1在工作线程1中工作


//        <-- 创建第2个被观察者 -->
                Observable<String> observable2 = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                Log.d(TAG, "被观察者2发送了事件A");
                emitter.onNext("A");
                Thread.sleep(1000);

                Log.d(TAG, "被观察者2发送了事件B");
                emitter.onNext("B");
                Thread.sleep(1000);

                Log.d(TAG, "被观察者2发送了事件C");
                emitter.onNext("C");
                Thread.sleep(1000);

                Log.d(TAG, "被观察者2发送了事件D");
                emitter.onNext("D");
                Thread.sleep(1000);

                emitter.onComplete();
            }
        }).subscribeOn(Schedulers.newThread());// 设置被观察者2在工作线程2中工作
    // 假设不作线程控制，则该两个被观察者会在同一个线程中工作，即发送事件存在先后顺序，而不是同时发送


//        <-- 使用zip变换操作符进行事件合并 -->
        // 注：创建BiFunction对象传入的第3个参数 = 合并后数据的数据类型
//                Observable.zip(observable1, observable2, new BiFunction<Integer, String, String>() {
//                    @Override
//                    public String apply(Integer integer, String string) throws Exception {
//                        return  integer + string;
//                    }
//                }).subscribe(new Observer<String>() {
//                    @Override
//                    public void onSubscribe(Disposable d) {
//                        Log.d(TAG, "onSubscribe");
//                    }
//
//                    @Override
//                    public void onNext(String value) {
//                        Log.d(TAG, "最终接收到的事件 =  " + value);
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        Log.d(TAG, "onError");
//                    }
//
//                    @Override
//                    public void onComplete() {
//                        Log.d(TAG, "onComplete");
//                    }
//                });

//        特别注意：
//        尽管被观察者2的事件D没有事件与其合并，但还是会继续发送
//        若在被观察者1 & 被观察者2的事件序列最后发送onComplete()事件，则被观察者2的事件D也不会发送，测试结果如下


//        combineLatest（）
//        作用
//        当两个Observables中的任何一个发送了数据后，将先发送了数据的Observables 的最新（最后）一个数据 与 另外一个Observable发送的每个数据结合，最终基于该函数的结果发送数据
//        与Zip（）的区别：Zip（） = 按个数合并，即1对1合并；CombineLatest（） = 按时间合并，即在同一个时间点上合并
        Observable.combineLatest(
//                Observable.just(1L, 2L, 3L), // 第1个发送数据事件的Observable
                // 第1个发送数据事件的Observable,放在第一个位置就是第一个Observable 无论延时多长时间
                Observable.intervalRange(5, 3, 2200, 1200, TimeUnit.MILLISECONDS),
                Observable.intervalRange(0, 3, 1, 1, TimeUnit.SECONDS), // 第2个发送数据事件的Observable：从0开始发送、共发送3个数据、第1次事件延迟发送时间 = 1s、间隔时间 = 1s
                new BiFunction<Long, Long, Long>() {
                    @Override
                    public Long apply(Long o1, Long o2) throws Exception {
                        // o1 = 第1个Observable发送的最新（最后）1个数据
                        // o2 = 第2个Observable发送的每1个数据
                        Log.e(TAG, "合并的数据是： "+ o1 + " "+ o2);
                        return o1 + o2;
                        // 合并的逻辑 = 相加
                        // 即第1个Observable发送的最后1个数据 与 第2个Observable发送的每1个数据进行相加
                        //todo 看文字看不清楚，看assests里边的combineLatest原理图

                    }
                }).subscribe(new Consumer<Long>() {
            @Override
            public void accept(Long s) throws Exception {
                Log.e(TAG, "合并的结果是： "+s);
            }
        });


//        combineLatestDelayError（）
//        作用类似于concatDelayError（） / mergeDelayError（） ，即错误处理，此处不作过多描述






//        reduce（）
//        作用
//        把被观察者需要发送的事件聚合成1个事件 & 发送
//        聚合的逻辑根据需求撰写，但本质都是前2个数据聚合，然后与后1个数据继续进行聚合，依次类推

        Observable.just(1,2,3,4)
                .reduce(new BiFunction<Integer, Integer, Integer>() {
                    // 在该复写方法中复写聚合的逻辑
                    @Override
                    public Integer apply(@NonNull Integer s1, @NonNull Integer s2) throws Exception {
                        Log.e(TAG, "本次计算的数据是： "+s1 +" 乘 "+ s2);
                        return s1 * s2;
                        // 本次聚合的逻辑是：全部数据相乘起来
                        // 原理：第1次取前2个数据相乘，之后每次获取到的数据 = 返回的数据x原始下1个数据每
                    }
                }).subscribe(new Consumer<Integer>() {
            @Override
            public void accept(@NonNull Integer s) throws Exception {
                Log.e(TAG, "最终计算的结果是： "+s);
//                本次计算的数据是： 1 乘 2
//                本次计算的数据是： 2 乘 3
//                本次计算的数据是： 6 乘 4
//                最终计算的结果是： 24

            }
        });



//        collect（）
//        作用
//                将被观察者Observable发送的数据事件收集到一个数据结构里
        Observable.just(1, 2, 3 ,4, 5, 6)
                .collect(
                        // 1. 创建数据结构（容器），用于收集被观察者发送的数据
                        new Callable<ArrayList<Integer>>() {
                            @Override
                            public ArrayList<Integer> call() throws Exception {
                                return new ArrayList<>();
                            }
                            // 2. 对发送的数据进行收集
                        }, new BiConsumer<ArrayList<Integer>, Integer>() {
                            @Override
                            public void accept(ArrayList<Integer> list, Integer integer)
                                    throws Exception {
                                // 参数说明：list = 容器，integer = 后者数据
                                list.add(integer);
                                // 对发送的数据进行收集
                            }
                        }).subscribe(new Consumer<ArrayList<Integer>>() {
            @Override
            public void accept(@NonNull ArrayList<Integer> s) throws Exception {
                Log.e(TAG, "本次发送的数据是： "+s);

            }
        });




//        3.3 发送事件前追加发送事件


//        startWith（） / startWithArray（）
//        作用
//        在一个被观察者发送事件前，追加发送一些数据 / 一个新的被观察者

//        <-- 在一个被观察者发送事件前，追加发送一些数据 -->
                // 注：追加数据顺序 = 后调用先追加
// 运行结果：      接收到了事件1
//                        接收到了事件2
//                接收到了事件3
//                        接收到了事件0
//                接收到了事件4
//                        接收到了事件5
//                接收到了事件6
//                对Complete事件作出响应
                Observable.just(4, 5, 6)
                        .startWith(0)  // 追加单个数据 = startWith()
                        .startWithArray(1, 2, 3) // 追加多个数据 = startWithArray()
                        .subscribe(new Observer<Integer>() {
                            @Override
                            public void onSubscribe(Disposable d) {
                            }

                            @Override
                            public void onNext(Integer value) {
                                Log.d(TAG, "接收到了事件"+ value  );
                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.d(TAG, "对Error事件作出响应");
                            }

                            @Override
                            public void onComplete() {
                                Log.d(TAG, "对Complete事件作出响应");
                            }
                        });



//                <-- 在一个被观察者发送事件前，追加发送被观察者 & 发送数据 -->
                // 注：追加数据顺序 = 后调用先追加
                Observable.just(4, 5, 6)
                        .startWith(Observable.just(1, 2, 3))
                        .subscribe(new Observer<Integer>() {
                            @Override
                            public void onSubscribe(Disposable d) {

                            }

                            @Override
                            public void onNext(Integer value) {
                                Log.d(TAG, "接收到了事件"+ value  );
                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.d(TAG, "对Error事件作出响应");
                            }

                            @Override
                            public void onComplete() {
                                Log.d(TAG, "对Complete事件作出响应");
                            }
                        });





//        3.4 统计发送事件数量

//        count（）
//        作用
//                统计被观察者发送事件的数量

// 注：返回结果 = Long类型
        Observable.just(1, 2, 3, 4)
                .count()
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        Log.e(TAG, "发送的事件数量 =  "+aLong);

                    }
                });



//        4. 实际开发需求案例
//        下面，我将讲解组合 / 合并操作符的常见实际需求：
//
//        从缓存（磁盘、内存）中获取缓存数据
//                合并数据源
//        联合判断
//        下面，我将对每个应用场景进行实例Demo演示讲解。
//        4.1 获取缓存数据
//        即从缓存中（磁盘缓存 & 内存缓存）获取数据；若缓存中无数据，才通过网络请求获取数据
//        具体请看文章：Android RxJava 实际应用讲解：从磁盘 / 内存缓存中 获取缓存数据
//        4.2 合并数据源 & 同时展示
//        即，数据源 来自不同地方（如网络 + 本地），需要从不同的地方获取数据 & 同时展示
//        具体请看文章：Android RxJava 实际应用讲解：合并数据源
//        4.3 联合判断
//        即，同时对多个事件进行联合判断
//        如，填写表单时，需要表单里所有信息（姓名、年龄、职业等）都被填写后，才允许点击 "提交" 按钮


    }

    /**
     * 注册成功紧接着调用登录
     */
    private void normalnetNestCallBack() {
//        // 发送注册网络请求的函数方法
//        private void register() {
//            api.register(new RegisterRequest())
//                    .subscribeOn(Schedulers.io())               //在IO线程进行网络请求
//                    .observeOn(AndroidSchedulers.mainThread())  //回到主线程去处理请求结果
//                    .subscribe(new Consumer<RegisterResponse>() {
//                        @Override
//                        public void accept(RegisterResponse registerResponse) throws Exception {
//                            Toast.makeText(MainActivity.this, "注册成功", Toast.LENGTH_SHORT).show();
//                            login();   //注册成功, 调用登录的方法
//                        }
//                    }, new Consumer<Throwable>() {
//                        @Override
//                        public void accept(Throwable throwable) throws Exception {
//                            Toast.makeText(MainActivity.this, "注册失败", Toast.LENGTH_SHORT).show();
//                        }
//                    });
//        }
//
//
//        // 发送登录网络请求的函数方法
//        private void login() {
//            api.login(new LoginRequest())
//                    .subscribeOn(Schedulers.io())               //在IO线程进行网络请求
//                    .observeOn(AndroidSchedulers.mainThread())  //回到主线程去处理请求结果
//                    .subscribe(new Consumer<LoginResponse>() {
//                        @Override
//                        public void accept(LoginResponse loginResponse) throws Exception {
//                            Toast.makeText(MainActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
//                        }
//                    }, new Consumer<Throwable>() {
//                        @Override
//                        public void accept(Throwable throwable) throws Exception {
//                            Toast.makeText(MainActivity.this, "登录失败", Toast.LENGTH_SHORT).show();
//                        }
//                    });
//        }

    }

    /**
     * 变换操作符实现网络请求嵌套回调
     * 例如：注册成功紧接着调用登录
     */
    private void netNestCallBack() {
// 步骤1：创建Retrofit对象
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BaseUrl) // 设置 网络请求 Url
                .addConverterFactory(GsonConverterFactory.create()) //设置使用Gson解析(记得加入依赖)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create()) // 支持RxJava
                .build();

        // 步骤2：创建 网络请求接口 的实例
        final RequestInterface request = retrofit.create(RequestInterface.class);

        // 步骤3：采用Observable<...>形式 对 2个网络请求 进行封装
        observable1 = request.fenci("i am alan");
        observable2 = request.fenci2("i am alan2");


        observable1.subscribeOn(Schedulers.io())               // （初始被观察者）切换到IO线程进行网络请求1
                .observeOn(AndroidSchedulers.mainThread())  // （新观察者）切换到主线程 处理网络请求1的结果
                .doOnNext(new Consumer<SimpleResponseEntity>() {
                    @Override
                    public void accept(SimpleResponseEntity result) throws Exception {
                        Log.d(TAG, "第1次网络请求成功");
                        Log.d(TAG, result.toString());
                        // 对第1次网络请求返回的结果进行操作 =
                    }
                })
                .doOnError(new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        try{
                            Log.d(TAG, "第1次网络请求失败");
                        }catch (Exception e){
                            e.printStackTrace();
                        }
//
                    }
                })

                .observeOn(Schedulers.io())
                // （新被观察者，同时也是新观察者）切换到IO线程去发起登录请求
                // 特别注意：因为flatMap是对初始被观察者作变换，所以对于旧被观察者，它是新观察者，所以通过observeOn切换线程
                // 但对于初始观察者，它则是新的被观察者
                .flatMap(new Function<SimpleResponseEntity, ObservableSource<SimpleResponseEntity2>>() { // 作变换，即作嵌套网络请求
                    @Override
                    public ObservableSource<SimpleResponseEntity2> apply(SimpleResponseEntity result) throws InterruptedException {
                        //todo 这里故意判读错误
                        //接口返回的appCode是bitspaceman (只是判断一下接口是否成功)
                        //故意判断错误 认为返回的是错误code和错误信息（比如接口返回提示少传了个参数）
                        if(request == null || !"bitspaceman2".equals(result.getAppCode())){
                            Log.d(TAG,"接口返回提示少传了个参数");
                            //快速创建后，仅发送Complete事件
                            return Observable.empty();
                        }else {
                            // 将网络请求1转换成网络请求2，即发送网络请求2
                            Log.d(TAG, "将网络请求1转换成网络请求2，即发送网络请求2");
                            //todo 开放接口 请求间隔有要求
                            Thread.sleep(1000);
                            return observable2;
                        }
                    }
                })

                .observeOn(AndroidSchedulers.mainThread())  // （初始观察者）切换到主线程 处理网络请求2的结果
                .subscribe(new Consumer<SimpleResponseEntity2>() {
                    @Override
                    public void accept(SimpleResponseEntity2 result) throws Exception {
                        Log.d(TAG, "第2次网络请求成功");
                        Log.d(TAG, result.toString());
                        // 对第2次网络请求返回的结果进行操作 =
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Log.d(TAG, "第2次网络请求失败");
                    }
                }, new Action() {
                    @Override
                    public void run() throws Exception {
                        Log.d(TAG, "第2次网络请求完成");
                    }
                });
//                .subscribe(new Observer<SimpleResponseEntity2>() {
//                    @Override
//                    public void onSubscribe(Disposable d) {
//                        Log.d(TAG, "开始采用subscribe连接");
//                    }
//
//                    @Override
//                    public void onNext(SimpleResponseEntity2 simpleResponseEntity2) {
//                        Log.d(TAG, "第2次网络请求成功");
//                        Log.d(TAG, simpleResponseEntity2.toString());
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        Log.d(TAG, "第二次网络请求失败");
//                        e.printStackTrace();
//                    }
//
//                    @Override
//                    public void onComplete() {
//                        Log.d(TAG, "第二次网络请求完成");
//                    }
//                });


    }


    /**
     * 变化操作符
     * 对事件序列中的事件 / 整个事件序列 进行加工处理（即变换），使得其转变成不同的事件 / 整个事件序列
     *  实际开发需求案例
     变换操作符的主要开发需求场景 = 嵌套回调（Callback hell）
     */
    private void transformOperators() {
//        作用
//        对事件序列中的事件 / 整个事件序列 进行加工处理（即变换），使得其转变成不同的事件 / 整个事件序列

//        Map()
//        作用
//        对 被观察者发送的每1个事件都通过 指定的函数 处理，从而变换成另外一种事件
//        即， 将被观察者发送的事件转换为任意的类型事件。

//        应用场景
//                数据类型转换
//        具体使用
//        下面以将 使用Map（） 将事件的参数从 整型 变换成 字符串类型 为例子说明


        // 采用RxJava基于事件流的链式操作
        Observable.create(new ObservableOnSubscribe<Integer>() {

            // 1. 被观察者发送事件 = 参数为整型 = 1、2、3
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                emitter.onNext(1);
                emitter.onNext(2);
                emitter.onNext(3);

            }
            // 2. 使用Map变换操作符中的Function函数对被观察者发送的事件进行统一变换：整型变换成字符串类型
//            * @param <T> the input value type
//            * @param <R> the output value type
//            从上面可以看出，map() 将参数中的 Integer 类型对象转换成一个 String类型 对象后返回
//
//            同时，事件的参数类型也由 Integer 类型变成了 String 类型
        }).map(new Function<Integer, String>() {
            @Override
            public String apply(Integer integer) throws Exception {
                return "使用 Map变换操作符 将事件" + integer +"的参数从 整型"+integer + " 变换成 字符串类型" + integer ;
            }
        }).subscribe(new Consumer<String>() {

            // 3. 观察者接收事件时，是接收到变换后的事件 = 字符串类型
            @Override
            public void accept(String s) throws Exception {
                Log.d(TAG, s);
            }
        });




//        FlatMap（）
//        作用：将被观察者发送的事件序列进行 拆分 & 单独转换，再合并成一个新的事件序列，最后再进行发送
//
//                原理
//
//        为事件序列中每个事件都创建一个 Observable 对象；
//        将对每个 原始事件 转换后的 新事件 都放入到对应 Observable对象；
//        将新建的每个Observable 都合并到一个 新建的、总的Observable 对象；
//        新建的、总的Observable 对象 将 新合并的事件序列 发送给观察者（Observer）

//        应用场景
//                无序的将被观察者发送的整个事件序列进行变换

        // 采用RxJava基于事件流的链式操作
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                emitter.onNext(1);
                emitter.onNext(2);
                emitter.onNext(3);
            }

            // 采用flatMap（）变换操作符
        }).flatMap(new Function<Integer, ObservableSource<String>>() {
            @Override
            public ObservableSource<String> apply(Integer integer) throws Exception {
                final List<String> list = new ArrayList<>();
                for (int i = 0; i < 300; i++) {
                    list.add("我是事件 " + integer + "拆分后的子事件" + i);
                    // 通过flatMap中将被观察者生产的事件序列先进行拆分，再将每个事件转换为一个新的发送三个String事件
                    // 最终合并，再发送给被观察者
                }
                return Observable.fromIterable(list);
            }
        }).subscribe(new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {
                Log.d(TAG, s);
//                我是事件 1拆分后的子事件0
//                我是事件 1拆分后的子事件1
//                我是事件 1拆分后的子事件2
//                我是事件 2拆分后的子事件0
//                我是事件 2拆分后的子事件1
//                我是事件 2拆分后的子事件2
//                我是事件 3拆分后的子事件0
//                我是事件 3拆分后的子事件1
//                我是事件 3拆分后的子事件2


            }
        });





//        ConcatMap（）
//        作用：类似FlatMap（）操作符
//
//        与FlatMap（）的 区别在于：拆分 & 重新合并生成的事件序列 的顺序 = 被观察者旧序列生产的顺序
//        应用场景
//                有序的将被观察者发送的整个事件序列进行变换

        // 采用RxJava基于事件流的链式操作
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                emitter.onNext(1);
                emitter.onNext(2);
                emitter.onNext(3);
            }

            // 采用concatMap（）变换操作符
        }).concatMap(new Function<Integer, ObservableSource<String>>() {
            @Override
            public ObservableSource<String> apply(Integer integer) throws Exception {
                final List<String> list = new ArrayList<>();
                for (int i = 0; i < 3; i++) {
                    list.add("我是事件 " + integer + "拆分后的子事件" + i);
                    // 通过concatMap中将被观察者生产的事件序列先进行拆分，再将每个事件转换为一个新的发送三个String事件
                    // 最终合并，再发送给被观察者
                }
                return Observable.fromIterable(list);
            }
        }).subscribe(new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {
                Log.d(TAG, s);
//                注：新合并生成的事件序列顺序是有序的，即 严格按照旧序列发送事件的顺序
            }
        });



//        Buffer（）
//        作用
//        定期从 被观察者（Obervable）需要发送的事件中 获取一定数量的事件 & 放到缓存区中，最终发送
//        应用场景
//                缓存被观察者发送的事件

        // 被观察者 需要发送5个数字
        Observable.just(1, 2, 3, 4, 5)
                .buffer(3, 1) // 设置缓存区大小 & 步长
                // 缓存区大小 = 每次从被观察者中获取的事件数量
                // 步长 = 每次获取新事件的数量
                .subscribe(new Observer<List<Integer>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }
                    @Override
                    public void onNext(List<Integer> stringList) {
                        //
                        Log.d(TAG, " 缓存区里的事件数量 = " +  stringList.size());
                        for (Integer value : stringList) {
                            Log.d(TAG, " 事件 = " + value);
                        }
//                        缓存区里的事件数量 = 3
//                        事件 = 1
//                        事件 = 2
//                        事件 = 3
//                        缓存区里的事件数量 = 3
//                        事件 = 2
//                        事件 = 3
//                        事件 = 4
//                        缓存区里的事件数量 = 3
//                        事件 = 3
//                        事件 = 4
//                        事件 = 5
//                        缓存区里的事件数量 = 2
//                        事件 = 4
//                        事件 = 5
//                        缓存区里的事件数量 = 1
//                        事件 = 5
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "对Error事件作出响应" );
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "对Complete事件作出响应");
                    }
                });



    }


    /**
     * 无条件网络请求轮询
     */
    private void pollNetRequest() {

        Observable.interval(2,5,TimeUnit.SECONDS)
                // 参数说明：
                // 参数1 = 第1次延迟时间；
                // 参数2 = 间隔时间数字；
                // 参数3 = 时间单位；
                // 该例子发送的事件特点：延迟2s后发送事件，每隔1秒产生1个数字（从0开始递增1，无限个）

                 /*
                  * 步骤2：每次发送数字前发送1次网络请求（doOnNext（）在执行Next事件前调用）
                  * 即每隔1秒产生1个数字前，就发送1次网络请求，从而实现轮询需求
                  **/

                .doOnNext(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        Log.d(TAG, "第 " + aLong + " 次轮询" );
                        Retrofit retrofit = new Retrofit.Builder()
                                .baseUrl(BaseUrl)
                                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                                .addConverterFactory(GsonConverterFactory.create())
                                .build();

                        RequestInterface requestInterface = retrofit.create(RequestInterface.class);

                        Observable<SimpleResponseEntity> observable = requestInterface.fenci("i am alan");

                        observable.subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Observer<SimpleResponseEntity>() {
                                    @Override
                                    public void onSubscribe(Disposable d) {
                                        Log.d(TAG, "网络请求开始采用subscribe连接");
                                    }

                                    @Override
                                    public void onNext(SimpleResponseEntity simpleResponseEntity) {
                                        Log.d(TAG, "simpleResponseEntity：" + simpleResponseEntity.toString());
                                    }

                                    @Override
                                    public void onError(Throwable e) {
                                        Log.d(TAG, "网络请求error:" + e.getMessage());
                                    }

                                    @Override
                                    public void onComplete() {
                                        Log.d(TAG, "网络请求完成:" );
                                    }
                                });




                    }
                }).subscribe(new Observer<Long>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        Log.d(TAG, "开始采用subscribe连接");
                    }

                    @Override
                    public void onNext(Long aLong) {
                        Log.d(TAG, "along:" + aLong);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "对Error事件作出响应");
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "对Complete事件作出响应");
                    }
                });


    }

    private void rxDisposeUse() {

        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                emitter.onNext(1);
                emitter.onNext(2);
                emitter.onNext(3);
                emitter.onComplete();

            }
        }).subscribe(new Observer<Integer>() {

            private Disposable d;
            @Override
            public void onSubscribe(Disposable d) {
                Log.d(TAG, "开始采用subscribe连接");
                this.d = d;
            }

            @Override
            public void onNext(Integer integer) {
                Log.d(TAG, "对Next事件"+ integer +"作出响应"  );
                if(integer == 2){
                    d.dispose();
                }
                Log.d(TAG, "已经切断了连接：" + d.isDisposed());
            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG, "对Error事件作出响应");
            }

            @Override
            public void onComplete() {
                Log.d(TAG, "对Complete事件作出响应");
            }
        });


    }

    /**
     * 链式使用rx
     */
    private void rxChainUse() {
        //        完整的创建被观察者对象 create()
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {


                emitter.onNext(1);
                emitter.onNext(2);
                emitter.onNext(3);
                emitter.onComplete();
            }
        }).subscribe(new Observer<Integer>() {
            @Override
            public void onSubscribe(Disposable d) {
                Log.d(TAG, "开始采用subscribe连接");
            }

            @Override
            public void onNext(Integer integer) {
                Log.d(TAG, "对Next事件"+ integer +"作出响应"  );
            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG, "对Error事件作出响应");
            }

            @Override
            public void onComplete() {
                Log.d(TAG, "对Complete事件作出响应");
            }
        });


        // 1. 创建时传入整型1、2、3、4
        // 在创建后就会发送这些对象，相当于执行了onNext(1)、onNext(2)、onNext(3)、onNext(4)
        Observable.just(1, 2, 3,4)
                // 至此，一个Observable对象创建完毕，以下步骤仅为展示一个完整demo，可以忽略
                // 2. 通过通过订阅（subscribe）连接观察者和被观察者
                // 3. 创建观察者 & 定义响应事件的行为
                .subscribe(new Observer<Integer>() {

                    @Override
                    public void onSubscribe(Disposable d) {
                        Log.d(TAG, "开始采用subscribe连接");
                    }
                    // 默认最先调用复写的 onSubscribe（）

                    @Override
                    public void onNext(Integer value) {
                        Log.d(TAG, "接收到了事件"+ value  );
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "对Error事件作出响应");
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "对Complete事件作出响应");
                    }

                });

//        fromArray（）
//        作用
//        快速创建1个被观察者对象（Observable）
//        发送事件的特点：直接发送 传入的数组数据
//        会将数组中的数据转换为Observable对象
        // 注：
// 可发送10个以上参数
// 若直接传递一个list集合进去，会直接把list当做一个数据元素发送,所以要传入数组

        // 1. 设置需要传入的数组
        Integer[] items = { 0, 1, 2, 3, 4 };

        // 2. 创建被观察者对象（Observable）时传入数组
        // 在创建后就会将该数组转换成Observable & 发送该对象中的所有数据
        Observable.fromArray(items)
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        Log.d(TAG, "开始采用subscribe连接");
                    }

                    @Override
                    public void onNext(Integer value) {
                        Log.d(TAG, "接收到了事件"+ value  );
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "对Error事件作出响应");
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "对Complete事件作出响应");
                    }

                });



//        fromIterable（）
//        作用
//        快速创建1个被观察者对象（Observable）
//        发送事件的特点：直接发送 传入的集合List数据
                /*
         * 快速发送集合
         **/
        // 1. 设置一个集合
                List<Integer> list = new ArrayList<>();
                list.add(1);
                list.add(2);
                list.add(3);

        // 2. 通过fromIterable()将集合中的对象 / 数据发送出去
        Observable.fromIterable(list)
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        Log.d(TAG, "开始采用subscribe连接");
                    }

                    @Override
                    public void onNext(Integer value) {
                        Log.d(TAG, "接收到了事件"+ value  );
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "对Error事件作出响应");
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "对Complete事件作出响应");
                    }
                });


        // 下列方法一般用于测试使用

        //<-- empty()  -->
        // 该方法创建的被观察者对象发送事件的特点：仅发送Complete事件，直接通知完成
                        Observable observable1=Observable.empty();
        // 即观察者接收后会直接调用onCompleted（）

        //<-- error()  -->
        // 该方法创建的被观察者对象发送事件的特点：仅发送Error事件，直接通知异常
        // 可自定义异常
                        Observable observable2=Observable.error(new RuntimeException());
        // 即观察者接收后会直接调用onError（）

        //                <-- never()  -->
        // 该方法创建的被观察者对象发送事件的特点：不发送任何事件
                        Observable observable3=Observable.never();
        // 即观察者接收后什么都不调用


        System.out.println("Observable 延时创建");
        //需求场景
//        定时操作：在经过了x秒后，需要自动执行y操作
//        周期性操作：每隔x秒后，需要自动执行y操作



//        defer（）
//        作用
//        直到有观察者（Observer ）订阅时，才动态创建被观察者对象（Observable） & 发送事件
//        通过 Observable工厂方法创建被观察者对象（Observable）
//        每次订阅后，都会得到一个刚创建的最新的Observable对象，这可以确保Observable对象里的数据是最新的
//                应用场景
//        动态创建被观察者对象（Observable） & 获取最新的Observable对象数据


//        <-- 1. 第1次对i赋值 ->>
        i = 10;

        // 2. 通过defer 定义被观察者对象
        // 注：此时被观察者对象还没创建
        Observable<Integer> observable = Observable.defer(new Callable<ObservableSource<? extends Integer>>() {
            @Override
            public ObservableSource<? extends Integer> call() throws Exception {
                return Observable.just(i);
            }
        });

        i = 15;
        observable.subscribe(new Observer<Integer>() {
            @Override
            public void onSubscribe(Disposable d) {
                Log.d(TAG, "开始采用subscribe连接");
            }

            @Override
            public void onNext(Integer value) {
                //输出15
                Log.d(TAG, "接收到的整数是"+ value  );
            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG, "对Error事件作出响应");
            }

            @Override
            public void onComplete() {
                Log.d(TAG, "对Complete事件作出响应");
            }

        });


//        timer（）
//        作用
//        快速创建1个被观察者对象（Observable）
//        发送事件的特点：延迟指定时间后，发送1个数值0（Long类型）
//        本质 = 延迟指定时间后，调用一次 onNext(0)
//
//        应用场景
//        延迟指定事件，发送一个0，一般用于检测

        // 该例子 = 延迟2s后，发送一个long类型数值

        Observable.timer(2, TimeUnit.SECONDS)
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        Log.d(TAG, "开始采用subscribe连接");
                    }

                    @Override
                    public void onNext(Long value) {
                        Log.d(TAG, "接收到了事件"+ value  );
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "对Error事件作出响应");
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "对Complete事件作出响应");
                    }

                });

        // 注：timer操作符默认运行在一个新线程上
        // 也可自定义线程调度器（第3个参数）：timer(long,TimeUnit,Scheduler)






//        interval（）
//        作用
//        快速创建1个被观察者对象（Observable）
//        发送事件的特点：每隔指定时间 就发送 事件
//                发送的事件序列 = 从0开始、无限递增1的的整数序列


        // 参数说明：
        // 参数1 = 第1次延迟时间；
        // 参数2 = 间隔时间数字；
        // 参数3 = 时间单位；
//        Observable.interval(3,1,TimeUnit.SECONDS)
//                // 该例子发送的事件序列特点：延迟3s后发送事件，每隔1秒产生1个数字（从0开始递增1，无限个）
//                .subscribe(new Observer<Long>() {
//                    @Override
//                    public void onSubscribe(Disposable d) {
//                        Log.d(TAG, "开始采用subscribe连接");
//                    }
//                    // 默认最先调用复写的 onSubscribe（）
//
//                    @Override
//                    public void onNext(Long value) {
//                        Log.d(TAG, "接收到了事件"+ value  );
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        Log.d(TAG, "对Error事件作出响应");
//                    }
//
//                    @Override
//                    public void onComplete() {
//                        Log.d(TAG, "对Complete事件作出响应");
//                    }
//
//                });

        // 注：interval默认在computation调度器上执行
        // 也可自定义指定线程调度器（第3个参数）：interval(long,TimeUnit,Scheduler)



//        intervalRange（）
//        作用
//        快速创建1个被观察者对象（Observable）
//        发送事件的特点：每隔指定时间 就发送 事件，可指定发送的数据的数量
//        a. 发送的事件序列 = 从0开始、无限递增1的的整数序列
//        b. 作用类似于interval（），但可指定发送的数据的数量

        // 参数说明：
        // 参数1 = 事件序列起始点；
        // 参数2 = 事件数量；
        // 参数3 = 第1次事件延迟发送时间；
        // 参数4 = 间隔时间数字；
        // 参数5 = 时间单位
//        Observable.intervalRange(3,10,2, 1, TimeUnit.SECONDS)
//                // 该例子发送的事件序列特点：
//                // 1. 从3开始，一共发送10个事件；
//                // 2. 第1次延迟2s发送，之后每隔2秒产生1个数字（从0开始递增1，无限个）
//                .subscribe(new Observer<Long>() {
//                    @Override
//                    public void onSubscribe(Disposable d) {
//                        Log.d(TAG, "开始采用subscribe连接");
//                    }
//                    // 默认最先调用复写的 onSubscribe（）
//
//                    @Override
//                    public void onNext(Long value) {
//                        Log.d(TAG, "接收到了事件"+ value  );
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        Log.d(TAG, "对Error事件作出响应");
//                    }
//
//                    @Override
//                    public void onComplete() {
//                        Log.d(TAG, "对Complete事件作出响应");
//                    }
//
//                });


//        range（）
//        作用
//        快速创建1个被观察者对象（Observable）
//        发送事件的特点：连续发送 1个事件序列，可指定范围
//        a. 发送的事件序列 = 从0开始、无限递增1的的整数序列
//        b. 作用类似于intervalRange（），但区别在于：无延迟发送事件

        // 参数说明：
        // 参数1 = 事件序列起始点；
        // 参数2 = 事件数量；
        // 注：若设置为负数，则会抛出异常
        Observable.range(3,10)
                // 该例子发送的事件序列特点：从3开始发送，每次发送事件递增1，一共发送10个事件
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        Log.d(TAG, "开始采用subscribe连接");
                    }
                    // 默认最先调用复写的 onSubscribe（）

                    @Override
                    public void onNext(Integer value) {
                        Log.d(TAG, "接收到了事件"+ value  );
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "对Error事件作出响应");
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "对Complete事件作出响应");
                    }

                });


    }

    private void rxBasicUse() {
//        完整的创建被观察者对象 create()
        Observable<Integer> observable = Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                emitter.onNext(1);
                emitter.onNext(2);
                emitter.onNext(3);
                emitter.onComplete();
            }
        });

        Observer<Integer> observer = new Observer<Integer>() {
            @Override
            public void onSubscribe(Disposable d) {
                Log.d(TAG, "开始采用subscribe连接");
            }

            @Override
            public void onNext(Integer integer) {
                Log.d(TAG, "对Next事件"+ integer +"作出响应"  );
            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG, "对Error事件作出响应");
            }

            @Override
            public void onComplete() {
                Log.d(TAG, "对Complete事件作出响应");
            }
        };

        observable.subscribe(observer);

        //表示观察者不对被观察者发送的事件作出任何响应（但被观察者还是可以继续发送事件）
        //observable.subscribe();

        //表示观察者只对被观察者发送的Next事件作出响应
        //Disposable subscribe(Consumer<? super T> onNext)

        // 表示观察者只对被观察者发送的Next事件 & Error事件作出响应
        //Disposable subscribe(Consumer<? super T> onNext, Consumer<? super Throwable> onError)

        // 表示观察者只对被观察者发送的Next事件、Error事件 & Complete事件作出响应
//        public final Disposable subscribe(Consumer<? super T> onNext, Consumer<? super Throwable> onError, Action onComplete) {}

        // 表示观察者只对被观察者发送的Next事件、Error事件 、Complete事件 & onSubscribe事件作出响应
//        public final Disposable subscribe(Consumer<? super T> onNext, Consumer<? super Throwable> onError, Action onComplete, Consumer<? super Disposable> onSubscribe) {}

        // 表示观察者对被观察者发送的任何事件都作出响应
//        public final void subscribe(Observer<? super T> observer) {}

    }
}
