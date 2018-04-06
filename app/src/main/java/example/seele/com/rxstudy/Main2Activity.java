package example.seele.com.rxstudy;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import com.jakewharton.rxbinding2.widget.RxTextView;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function3;
import io.reactivex.plugins.RxJavaPlugins;

public class Main2Activity extends AppCompatActivity {


    private static final String TAG = "RX.Main2Activity";
    EditText name,age,job;
    Button list;


    /**
     * 联合判断多个事件
     *  1. 需求场景
     需要同时对多个事件进行联合判断

     如，填写表单时，需要表单里所有信息（姓名、年龄、职业等）都被填写后，才允许点击 "提交" 按钮
     *  2. 功能说明
     此处采用 填写表单 作为联合判断功能展示
     即，表单里所有信息（姓名、年龄、职业等）都被填写后，才允许点击 "提交" 按钮
     * */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);


         /**
         * 步骤1：设置控件变量 & 绑定
         **/
        name = (EditText) findViewById(R.id.name);
        age = (EditText) findViewById(R.id.age);
        job = (EditText) findViewById(R.id.job);
        list = (Button) findViewById(R.id.list);

        /**
         * 步骤2：为每个EditText设置被观察者，用于发送监听事件
         * 说明：
         * 1. 此处采用了RxBinding：RxTextView.textChanges(name) = 对对控件数据变更进行监听（功能类似TextWatcher），
         * 需要引入依赖：compile 'com.jakewharton.rxbinding2:rxbinding:2.0.0'
         * 2. 传入EditText控件，点击任1个EditText撰写时，都会发送数据事件 = Function3（）的返回值（下面会详细说明）
         * 3. 采用skip(1)原因：跳过 一开始EditText无任何输入时的空值
         **/

        Observable<CharSequence> nameObservable = RxTextView.textChanges(name).skip(1);
        Observable<CharSequence> ageObservable  = RxTextView.textChanges(age).skip(1);
        Observable<CharSequence> jobObservable  = RxTextView.textChanges(job).skip(1);


         /**
         * 步骤3：通过combineLatest（）合并事件 & 联合判断
         **/
         
         Observable.combineLatest(nameObservable, ageObservable, jobObservable, new Function3<CharSequence, CharSequence, CharSequence, Boolean>() {

             @Override
             public Boolean apply(CharSequence charSequence, CharSequence charSequence2, CharSequence charSequence3) throws Exception {

                    if(!TextUtils.isEmpty(charSequence) && !TextUtils.isEmpty(charSequence2) && TextUtils.isEmpty(charSequence3)){
                        return true;
                    }

                    return false;
             }
         }).subscribe(new Consumer<Boolean>() {
             @Override
             public void accept(Boolean aBoolean) throws Exception {

                 Log.e(TAG,"提交按钮是否可点击:" + aBoolean);
             }
         });




    }
}
