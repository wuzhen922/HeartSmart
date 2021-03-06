package com.xqlh.heartsmart.ui.welcome;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.text.TextUtils;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.xqlh.heartsmart.MainActivity;
import com.xqlh.heartsmart.R;
import com.xqlh.heartsmart.api.RetrofitHelper;
import com.xqlh.heartsmart.api.base.BaseObserval;
import com.xqlh.heartsmart.base.BaseActivity;
import com.xqlh.heartsmart.bean.EntityWelcome;
import com.xqlh.heartsmart.ui.login.ui.LoginActivity;
import com.xqlh.heartsmart.utils.Constants;
import com.xqlh.heartsmart.utils.ContextUtils;
import com.xqlh.heartsmart.utils.ImageLoaderUtil;
import com.xqlh.heartsmart.utils.SharedPreferencesHelper;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class WelcomeActivity extends BaseActivity {
    @BindView(R.id.welcome_iv)
    ImageView welcome_iv;
    CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    private boolean isFirst;
    private boolean isLogin;
    SharedPreferencesHelper sp;
    ValueAnimator alpha;

    @Override
    public int setContent() {
        return R.layout.activity_welcome;
    }

    @Override
    public boolean setFullScreen() {
        return false;
    }

    @Override
    public void init() {
        sp = new SharedPreferencesHelper(ContextUtils.getContext(), Constants.CHECKINFOR);

        isFirst = (boolean) sp.getSharedPreference(Constants.IS_FIRST, false);

        //首次安装走引导页面
//        if (!isFirst) {
//            finish();
//            sp.put(Constants.IS_FIRST, true);
//            Intent intent = new Intent(this, GuideActivity.class);
//            startActivity(intent);
//            return;
//        }

        isLogin = (boolean) sp.getSharedPreference(Constants.IS_LOGIN, false);

        final int countTime = 2;
        getWelcome();
        mCompositeDisposable.add(Observable.interval(0, 1, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Function<Long, Integer>() {
                    @Override
                    public Integer apply(Long aLong) throws Exception {
                        return countTime - aLong.intValue();
                    }
                }).take(countTime + 1) //设置循环的次数
                .doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(Disposable disposable) throws Exception {

                    }
                }).subscribeWith(new DisposableObserver<Integer>() {
                    @Override
                    public void onNext(Integer integer) {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {
                        if (mCompositeDisposable != null) {
                            mCompositeDisposable.dispose();
                        }
                        //如果登录过了
                        if (isLogin && !TextUtils.isEmpty(sp.getSharedPreference(Constants.LOGIN_TOKEN, "").toString().trim())) {
                            Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
                            startActivity(intent);

                        } else {
                            Intent intent1 = new Intent(WelcomeActivity.this, LoginActivity.class);
                            startActivity(intent1);
                        }
                        finish();

//                        initAnima();
                    }
                })
        );
    }

    protected void initAnima() {
        //淡入动画的设置
        //Fade animation sets
        alpha = ObjectAnimator.ofFloat(welcome_iv, "alpha", 1.0f, 0.5f,0.1f);
        //设置动画的持续时间
        //Set the Duration of the Animation
        alpha.setDuration(2000);
        //动画设置监听
        //Animation set upListener
        alpha.addListener(new AnimatorListenerAdapter() {
            //跳转到主页面
            //jump to main page
            @Override
            public void onAnimationEnd(Animator animation) {

            }
        });
        //启动动画
        //Start Animation
        alpha.start();
    }

    public void getWelcome() {
//        JSONObject object = new JSONObject();
//        try {
//            object.put("AppType", 1);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        RequestBody jsonBody = RequestBody.create(JSON, object.toString());

        RetrofitHelper.getApiService()
                .getWelcome(1)
                .subscribeOn(Schedulers.io())
                .compose(this.<EntityWelcome>bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserval<EntityWelcome>() {
                    @Override
                    public void onSuccess(EntityWelcome response) {
                        if (response.getCode() == 1) {
                            ImageLoaderUtil.LoadImage(WelcomeActivity.this, response.getResult().getPicURL(), welcome_iv);
                        }
                    }
                });
    }


    @Override
    protected void onDestroy() {
        Glide.clear(welcome_iv);
        super.onDestroy();
    }
}
