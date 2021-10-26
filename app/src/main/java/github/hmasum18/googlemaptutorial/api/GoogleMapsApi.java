package github.hmasum18.googlemaptutorial.api;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class GoogleMapsApi{
    public static final GoogleMapsApi instance = new GoogleMapsApi();
    public PlacesService placesService;

    private GoogleMapsApi(){
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.level(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .followRedirects(true)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://maps.googleapis.com/maps/api/place/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

       placesService =  retrofit.create(PlacesService.class);
    }
}
