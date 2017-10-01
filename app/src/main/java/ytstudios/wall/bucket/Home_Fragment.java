package ytstudios.wall.bucket;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Yugansh Tyagi on 10-09-2017.
 */

public class Home_Fragment extends Fragment {

    ArrayList<WallpapersModel> wallpapersModelArrayList;
    RecyclerView recyclerView;
    HomeFragmentCustomAdapter homeFragmentCustomAdapter;
    ImageView noNetImage;
    TextView noNetText;
    Button connectBtn;

    GridLayoutManager gridLayoutManager;

    boolean isNetworkConnected;

    private static int pageCount = 2;

    protected Handler handler;

    public static int spanCount = 2;

    public static String API_KEY;

    private static int wallpaperNumber = 1;

    private String Yo;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.home_fragment, null);

        API_KEY = getResources().getString(R.string.API_KEY);

        noNetImage = view.findViewById(R.id.noNet);
        noNetText = view.findViewById(R.id.noNetText);
        connectBtn = view.findViewById(R.id.connectBtn);
        connectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(Settings.ACTION_WIFI_SETTINGS), 0);
            }
        });


        wallpapersModelArrayList = new ArrayList<>();
        handler = new Handler();

        initData();

        gridLayoutManager = new GridLayoutManager(getContext(), 2);
        recyclerView = view.findViewById(R.id.homeFragment_rv);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(gridLayoutManager);

        homeFragmentCustomAdapter = new HomeFragmentCustomAdapter(wallpapersModelArrayList, getContext(), recyclerView);
        homeFragmentCustomAdapter.setOnLoadMoreListener(new onLoadMoreListener() {
            @Override
            public void onLoadMore() {
                //add null , so the adapter will check view_type and show progress bar at bottom
                wallpapersModelArrayList.add(null);
                Log.i("INSERTED", "NULL");
                homeFragmentCustomAdapter.notifyItemInserted(wallpapersModelArrayList.size() - 1);
                //Log.i("SIZE ", String.valueOf(wallpapersModelArrayList.size()));

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        wallpapersModelArrayList.remove(wallpapersModelArrayList.size() - 1);
                        homeFragmentCustomAdapter.notifyItemRemoved(wallpapersModelArrayList.size());
                        Log.i("REMOVED", "NULL");
                        //add items one by one
                        Log.i("INIT", "DATA");
                        new loadMore().execute("http://papers.co/android/page/" + pageCount + "/");
                        homeFragmentCustomAdapter.setLoaded();
                        Log.i("INIT", "FINISHED");
                    }
                }, 900);
            }
        });

        recyclerView.setAdapter(homeFragmentCustomAdapter);
        recyclerView.addItemDecoration(new RecyclerItemDecoration(2));

        setHasOptionsMenu(true);

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 0) {
            boolean n = isNetworkAvailable();
            if (n) {
                initData();
            }
        }
    }

    class loadMore extends AsyncTask<String, Integer, String> {

        List list;
        List id;

        @Override
        protected String doInBackground(String... params) {
            pageCount++;
            try {
                Document document = Jsoup.connect(params[0]).get();
                Element wall = document.select("ul.postul").first();
                //Log.i("LIST ", wall.toString());
                Elements url = wall.getElementsByAttribute("src");
                list = url.eachAttr("src");

                for (int i = 0; i < list.size(); i++) {
                    wallpaperNumber++;
                    String string = list.get(i).toString();
                    String sep[] = string.split("http://");
                    sep[1] = sep[1].replace("android/wp-content/uploads", "wallpaper");
                    sep[1] = sep[1].replace("-250x400.jpg", ".jpg?download=true");
                    sep[1] = "http://"+sep[1];
                    Log.i("String ", sep[1]);
                    Log.i("URL", string);
                    wallpapersModelArrayList.add(wallpapersModelArrayList.size()-1, new WallpapersModel(
                            string,///
                            sep[1],
                            "jpg",
                            wallpaperNumber
                    ));
                }
            } catch (Exception e) {
                Log.i("ERROR", e.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            //Log.i("COUNT ", String.valueOf(count));
            homeFragmentCustomAdapter.notifyItemInserted(wallpapersModelArrayList.size());
        }
    }

    private void initData() {
        isNetworkConnected = isNetworkAvailable();
        if (isNetworkConnected) {
            noNetImage.setVisibility(View.INVISIBLE);
            noNetText.setVisibility(View.INVISIBLE);
            connectBtn.setVisibility(View.GONE);
//            for (int i=1;i<2;i++)
//            {
//                if(i < 2){
//                    loadFromInternet("https://wallpaperscraft.com/all/ratings/1080x1920");
//                }
//                else
//                    loadFromInternet("https://wallpaperscraft.com/all/ratings/1080x1920/page" + i);
//            }
            //https://wall.alphacoders.com/api2.0/get.php?auth=" + API_KEY + "&method=highest_rated&page=10&info_level=2&page=1
            loadFromInternet("http://papers.co/android/");
            //http://wallpaperscraft.com/all/1080x1920
        } else {

            noNetImage.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.nonetwork));
            noNetImage.setVisibility(View.VISIBLE);
            noNetText.setVisibility(View.VISIBLE);
            connectBtn.setVisibility(View.VISIBLE);
            Toast.makeText(getContext(), "No Internet Connected!", Toast.LENGTH_SHORT).show();

        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.home_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings:
                Intent settings = new Intent(getContext(), SettingsActivity.class);
                startActivity(settings);
                return true;
            case R.id.menu_about:
                Toast.makeText(getContext(), "About", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.grid_two:
                spanCount = 2;
                gridLayoutManager.setSpanCount(spanCount);
                recyclerView.setLayoutManager(gridLayoutManager);
                return true;
            case R.id.grid_three:
                spanCount = 3;
                gridLayoutManager.setSpanCount(spanCount);
                recyclerView.setLayoutManager(gridLayoutManager);
                return true;
            case R.id.refresh:
                pageCount = 2;
                initData();
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    public void loadFromInternet(final String url) {

        if (isNetworkConnected) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //https://wall.alphacoders.com/api2.0/get.php?auth="+API_KEY+"&method=highest_rated&page=10&info_level=2
                    wallpapersModelArrayList.clear();
                    //new ReadJSON().execute(url);
                    new ReadHTML().execute(url);
                }
            });
        }
    }

    class ReadHTML extends AsyncTask<String, Integer, String> {

        List list;

        @Override
        protected String doInBackground(String... params) {
            try {

                Document document = Jsoup.connect(params[0]).get();
                Element wall = document.select("ul.postul").first();
                //Log.i("LIST ", wall.toString());
                Elements url = wall.getElementsByAttribute("src");
                list = url.eachAttr("src");

                for (int i = 0; i < list.size(); i++) {
                    wallpaperNumber++;
                    String string = list.get(i).toString();
                    String sep[] = string.split("http://");
                    sep[1] = sep[1].replace("android/wp-content/uploads", "wallpaper");
                    //sep[1] = sep[1].replace("-6-", "-6-");
                    sep[1] = sep[1].replace("-250x400.jpg", ".jpg?download=true");
                    sep[1] = "http://"+sep[1];
                    Log.i("String ", sep[1]);
                    Log.i("URL", string);
                    wallpapersModelArrayList.add(new WallpapersModel(
                            string,///
                            sep[1],
                            "jpg",
                            wallpaperNumber
                    ));
                }


//                // Connect to the web site
//                Document document = Jsoup.connect(params[0]).get();
//                Element wall = document.select("div.wallpapers").first();
//                //Log.i("WALL  ", wall.toString());
//                Elements url = wall.getElementsByAttribute("src");
//                //Log.i("ELEMENTS   ", url.toString());
//                list = url.eachAttr("src");
//
//                for(int i = 0; i < list.size(); i++){
//                    String string = list.get(i).toString();
//                    String[] sep = string.split("wallpaperscraft.com");
//                    ///Log.i("URL ", sep[1]);
//                    wallpapersModelArrayList.add(new WallpapersModel(
//                            "https:/www.wallpaperscraft.com"+sep[1].replace("168x300", "320x480"),///
//                            "https:/www.wallpaperscraft.com"+sep[1].replace("168x300", "1080x1920"),
//                            "jpg",
//                            1
//                    ));
//                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            Toast.makeText(getContext(), "Loading Wallpapers!", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            //Log.i("COUNT ", String.valueOf(count));
            homeFragmentCustomAdapter.notifyDataSetChanged();
        }
    }

    class ReadJSON extends AsyncTask<String, Integer, String> {


        @Override
        protected String doInBackground(String... params) {
            return readURL(params[0]);
        }

        @Override
        protected void onPreExecute() {
            Toast.makeText(getContext(), "Loading Wallpapers!", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onPostExecute(String content) {
            try {
                JSONObject jsonObject = new JSONObject(content);
                JSONArray jsonArray = jsonObject.getJSONArray("wallpapers");

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject wallpaperObject = jsonArray.getJSONObject(i);
//                    if(wallpaperObject.getString("success").length() <= 4){
                    wallpapersModelArrayList.add(new WallpapersModel(
                            wallpaperObject.getString("url_thumb"),
                            wallpaperObject.getString("url_image"),
                            wallpaperObject.getString("file_type"),
                            wallpaperObject.getInt("id")
                    ));
//                    }
//                    else {
//                        imageView.setVisibility(View.VISIBLE);
//                        return;
//                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            homeFragmentCustomAdapter.notifyDataSetChanged();
        }
    }

    private static String readURL(String theUrl) {
        StringBuilder content = new StringBuilder();
        try {
            // create a url object
            URL url = new URL(theUrl);
            // create a urlconnection object
            URLConnection urlConnection = url.openConnection();
            // wrap the urlconnection in a bufferedreader
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            String line;
            // read from the urlconnection via the bufferedreader
            while ((line = bufferedReader.readLine()) != null) {
                content.append(line + "\n");
            }
            bufferedReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return content.toString();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}