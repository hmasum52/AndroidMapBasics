package github.hmasum18.googlemaptutorial.api;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import github.hmasum18.googlemaptutorial.model.places.NearByPlace;

public class NearbyPlacesApiResponse {
    @SerializedName("html_attributions")
    @Expose
    private List<Object> htmlAttributions = null;

    @SerializedName("next_page_token")
    @Expose
    private String nextPageToken;

    @SerializedName("results")
    @Expose
    private List<NearByPlace> results = new ArrayList<>();

    public List<Object> getHtmlAttributions() {
        return htmlAttributions;
    }

    public String getNextPageToken() {
        return nextPageToken;
    }

    public List<NearByPlace> getResults() {
        return results;
    }

    public void setHtmlAttributions(List<Object> htmlAttributions) {
        this.htmlAttributions = htmlAttributions;
    }

    public void setNextPageToken(String nextPageToken) {
        this.nextPageToken = nextPageToken;
    }

    public void setResults(List<NearByPlace> results) {
        this.results = results;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
