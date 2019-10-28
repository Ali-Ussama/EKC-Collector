package connection;

import data.Surveyor;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

/**
 * Created by eslamelhoseiny on 11/1/17.
 */

interface CollectorAPI {

    @FormUrlEncoded
    @POST("GetAll")
    Call<Surveyor> login(@Field("UserName") String userName, @Field("Password") String password, @Field("DeviceId") String deviceId);

}
