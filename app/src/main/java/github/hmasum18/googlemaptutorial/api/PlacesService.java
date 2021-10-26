package github.hmasum18.googlemaptutorial.api;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface PlacesService {
    @GET("nearbysearch/json")
    Call<ResponseBody> fetchNearByPlaces(@Query("location") String location,
                                         @Query("radius") int radius,
                                         @Query("type")String type,
                                         @Query("key")String key);
}
