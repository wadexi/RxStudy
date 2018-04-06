package example.seele.com.rxstudy.net;

import example.seele.com.rxstudy.entity.SimpleResponseEntity;
import example.seele.com.rxstudy.entity.SimpleResponseEntity2;
import example.seele.com.rxstudy.entity.SoResponseEntity;
import io.reactivex.Observable;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.Url;

/**
 * Created by SEELE on 2018/4/2.
 */

public interface RequestInterface {

//    @Headers("apikey:F1F8Ro6uKs3zc6IBq9bEr6c8CrTDprgybERUIXLkBuAYEaN0yDeeo9yB3btbSEI0")
    @POST("/nlp/segment/bitspaceman")
    @FormUrlEncoded
    Observable<SimpleResponseEntity> fenci(@Field("text") String text);

    @Headers("apikey:F1F8Ro6uKs3zc6IBq9bEr6c8CrTDprgybERUIXLkBuAYEaN0yDeeo9yB3btbSEI0")
    @POST("/nlp/segment/bitspaceman")
    @FormUrlEncoded
    Observable<SimpleResponseEntity2> fenci2(@Field("text") String text);


    /**
     * 360 新闻
     * @param url
     * @param apiKey
     * @param keyWord
     * @return
     */
    @GET()
//    @Headers("apikey:F1F8Ro6uKs3zc6IBq9bEr6c8CrTDprgybERUIXLkBuAYEaN0yDeeo9yB3btbSEI0")
    Observable<SoResponseEntity> so(@Url() String url, @Header("apikey") String apiKey, @Query("kw") String keyWord, @Query("site") String site);
}
